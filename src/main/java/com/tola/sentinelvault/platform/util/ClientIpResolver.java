package com.tola.sentinelvault.platform.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Extracts the real client IP from an incoming HTTP request.
 * Security notes
 * ──────────────
 * X-Forwarded-For and similar headers are trivially spoofed by any client
 * that is NOT behind a trusted reverse proxy.  Rules applied here:
 *  1. We only trust headers when the request actually arrives through a known
 *     proxy subnet (TRUSTED_PROXIES).  If the immediate TCP peer is not a
 *     trusted proxy we ignore forwarding headers entirely.
 *  2. In X-Forwarded-For we take the LAST value added by our own
 *     infrastructure (rightmost non-trusted-proxy IP), not the first value
 *     (which an attacker controls).
 *  3. Every extracted IP is validated against a strict regex before use so
 *     that injected values like "127.0.0.1, evil-string\r\nHeader: x" cannot
 *     propagate into logs or Redis keys.
 * Adjust TRUSTED_PROXIES and TRUSTED_HEADERS to match your deployment.
 */
@Slf4j
@Component
public class ClientIpResolver {

    private static final List<String> TRUSTED_PROXY_PREFIXES = List.of(
            "10.",       // RFC-1918 private
            "172.16.",
            "172.17.",
            "172.18.",
            "172.31.",
            "192.168.",
            "127."       // loopback (local dev / k8s pod networking)
    );

    private static final List<String> FORWARDING_HEADERS = List.of(
            "X-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP"   // Cloudflare
    );

    // Accepts dotted-decimal IPv4 and bracketed / plain IPv6.
    private static final Pattern VALID_IP = Pattern.compile(
            "^(\\d{1,3}\\.){3}\\d{1,3}$"                       // IPv4
                    + "|^[0-9a-fA-F:]+$"                       // IPv6 plain
                    + "|^\\[([0-9a-fA-F:]+)]$"                 // IPv6 bracketed
    );

    private static final String UNKNOWN = "unknown";

    /**
     * Returns the best-effort real client IP.
     *
     * @param request the current HTTP request
     * @return a validated IPv4/IPv6 string, never null
     */
    public String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        if (!isTrustedProxy(remoteAddr)) {
            // The TCP peer is not a known proxy – trust only the socket address.
            log.trace("Untrusted peer {}, ignoring forwarding headers", remoteAddr);
            return remoteAddr;
        }

        for (String header : FORWARDING_HEADERS) {
            String ip = extractFromHeader(request, header);
            if (ip != null) {
                log.trace("Resolved client IP {} from header {}", ip, header);
                return ip;
            }
        }
        return remoteAddr;
    }

    // Private helpers
    private boolean isTrustedProxy(String addr) {
        if (addr == null) return false;
        for (String prefix : TRUSTED_PROXY_PREFIXES) {
            if (addr.startsWith(prefix)) return true;
        }
        return false;
    }

    /**
     * Parses a forwarding header value.
     *
     * X-Forwarded-For is a comma-delimited list of IPs appended in left-to-right
     * traversal order.  The leftmost value is the one the client sent (attacker-
     * controlled when spoofing); the rightmost value was added by the nearest
     * trusted proxy and is therefore the most reliable.
     *
     * We walk right-to-left and return the first non-trusted-proxy, non-unknown IP.
     */
    private String extractFromHeader(HttpServletRequest request, String header) {
        String value = request.getHeader(header);
        if (value == null || value.isBlank()) return null;

        String[] parts = value.split(",");
        for (int i = parts.length - 1; i >= 0; i--) {
            String candidate = parts[i].trim();
            if (candidate.isEmpty() || UNKNOWN.equalsIgnoreCase(candidate)) continue;
            // Strip port suffix (e.g. "203.0.113.1:54321")
            candidate = stripPort(candidate);
            if (!isValidIp(candidate)) {
                log.warn("Ignoring invalid IP '{}' in header {}", candidate, header);
                continue;
            }
            if (isTrustedProxy(candidate)) continue;  // skip infrastructure hops
            return candidate;
        }
        return null;
    }

    private String stripPort(String ip) {
        // IPv4 with port: "1.2.3.4:5678" → "1.2.3.4"
        if (!ip.startsWith("[") && ip.contains(":")) {
            int lastColon = ip.lastIndexOf(':');
            String withoutPort = ip.substring(0, lastColon);
            if (isValidIp(withoutPort)) return withoutPort;
        }
        return ip;
    }

    private boolean isValidIp(String candidate) {
        return candidate != null && VALID_IP.matcher(candidate).matches();
    }
}