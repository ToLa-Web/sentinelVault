package com.tola.sentinelvault.platform.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed rate limiter using an atomic Lua script.
 *
 * Strategy: sliding-window counter per (identifier × action) key.
 * The Lua script guarantees increment + TTL-set are one atomic operation,
 * so there is no race between the two calls.
 */
@Slf4j
@Service
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitProperties properties;

    /**
     * Atomically increments the counter and sets the TTL on first write.
     * Returns the new counter value.
     */
    private static final String INCREMENT_SCRIPT =
            "local c = redis.call('incr', KEYS[1]); " +
                    "if c == 1 then " +
                    "    redis.call('expire', KEYS[1], ARGV[1]); " +
                    "end " +
                    "return c;";

    // Pre-compiled scripts – reused across calls
    private final DefaultRedisScript<Long> incrementScript;

    public RateLimitService(RedisTemplate<String, String> redisTemplate,
                            RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties    = properties;

        this.incrementScript = new DefaultRedisScript<>();
        this.incrementScript.setScriptText(INCREMENT_SCRIPT);
        this.incrementScript.setResultType(Long.class);
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Checks whether the caller has exceeded the configured rate limit.
     *
     * @param identifier  a stable caller token (IP address, user ID, …)
     * @param action      logical action name used to namespace the key
     *                    (e.g. "login", "register", "password-reset")
     * @throws RateLimitExceededException if the limit has been breached
     */
    public void checkRateLimit(String identifier, String action) {
        validateInputs(identifier, action);

        String key      = buildKey(identifier, action);
        long   attempts = executeIncrement(key);

        if (attempts > properties.getMaxAttempts()) {
            long ttl = resolveTtl(key);
            log.warn("Rate limit exceeded – identifier={} action={} attempts={}/{}",
                    identifier, action, attempts, properties.getMaxAttempts());
            throw new RateLimitExceededException(
                    "Too many attempts. Please try again in " + ttl + " seconds.", ttl);
        }

        log.debug("Rate limit OK – identifier={} action={} attempts={}/{}",
                identifier, action, attempts, properties.getMaxAttempts());
    }

    /**
     * Resets the counter for the given (identifier, action) pair.
     * Useful after a successful authentication to clear the failed-login window.
     */
    public void reset(String identifier, String action) {
        validateInputs(identifier, action);
        String key = buildKey(identifier, action);
        Boolean deleted = redisTemplate.delete(key);
        log.debug("Rate limit reset – identifier={} action={} deleted={}", identifier, action, deleted);
    }

    /**
     * Returns the remaining attempts in the current window, or 0 if exhausted.
     */
    public long remainingAttempts(String identifier, String action) {
        validateInputs(identifier, action);
        String value = redisTemplate.opsForValue().get(buildKey(identifier, action));
        if (value == null) {
            return properties.getMaxAttempts();
        }
        long used = parseLong(value);
        return Math.max(0, properties.getMaxAttempts() - used);
    }

    // -----------------------------------------------------------------------
    // Internals
    // -----------------------------------------------------------------------

    private long executeIncrement(String key) {
        Long result = redisTemplate.execute(
                incrementScript,
                Collections.singletonList(key),
                String.valueOf(properties.getWindowSeconds())
        );
        // Null can happen only if Redis is completely unreachable.
        // Fail-open: let the request through and alert via logs.
        if (result == null) {
            log.error("Redis returned null for key '{}'. Failing open.", key);
            return 0L;
        }
        return result;
    }

    /**
     * Resolves the TTL to show to the caller.
     * Falls back to the full window when Redis TTL is undetermined (-1 or -2),
     * which can happen if the key expires between the incr and the getExpire calls.
     */
    private long resolveTtl(String key) {
        long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl > 0 ? ttl : properties.getWindowSeconds();
    }

    /** Namespaced key: ratelimit:{action}:{identifier} */
    private String buildKey(String identifier, String action) {
        return "ratelimit:" + sanitize(action) + ":" + sanitize(identifier);
    }

    /**
     * Prevents key injection via colons or other special characters.
     * Adjust the replacement character to your preference.
     */
    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._@-]", "_");
    }

    private void validateInputs(String identifier, String action) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Rate-limit identifier must not be blank");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Rate-limit action must not be blank");
        }
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("Unexpected non-numeric value in Redis counter: '{}'", value);
            return 0L;
        }
    }

    // -----------------------------------------------------------------------
    // Exception
    // -----------------------------------------------------------------------

    public static class RateLimitExceededException extends RuntimeException {

        private final long retryAfterSeconds;

        public RateLimitExceededException(String message, long retryAfterSeconds) {
            super(message);
            this.retryAfterSeconds = retryAfterSeconds;
        }

        /** Seconds the caller should wait before retrying (for Retry-After header). */
        public long getRetryAfterSeconds() {
            return retryAfterSeconds;
        }
    }
}