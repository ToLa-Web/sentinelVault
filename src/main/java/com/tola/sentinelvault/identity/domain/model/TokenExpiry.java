package com.tola.sentinelvault.identity.domain.model;

import com.tola.sentinelvault.shared.domain.base.ValueObject;
import com.tola.sentinelvault.shared.domain.exception.DomainException;

import java.time.Instant;

public record TokenExpiry(Instant value) implements ValueObject {

    public TokenExpiry {
        if (value == null) {
            throw new TokenExpiryException("Token expiry date cannot be null");
        }
    }
    // Default duration: 7 days for refresh tokens
    public static TokenExpiry defaultDuration() {
        return new TokenExpiry(Instant.now().plusSeconds(7 * 24 * 60 * 60));
    }
    // Create a token expiry from an Instant
    public static TokenExpiry of(Instant instant) {
        return new TokenExpiry(instant);
    }

    // Custom duration factory (useful if different clients need different lifespans)
    public static TokenExpiry fromMillis(long millis) {
        return new TokenExpiry(Instant.now().plusMillis(millis));
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.value);
    }
    //Get remaining seconds until expiry
    public long getRemainingSeconds() {
        long seconds = value.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, seconds);
    }

    // Checks if the current time has passed the expiry time.
    public static class TokenExpiryException extends DomainException {
        public TokenExpiryException(String message) {
            super(message);
        }
    }
}
