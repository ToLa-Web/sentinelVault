package com.tola.sentinelvault.identity.infrastructure.security;

import com.tola.sentinelvault.identity.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey key;
    private final Long accessTokenExpirationMs;
    private final Long refreshTokenExpirationMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-expiration-ms:604800000}") long refreshTokenExpirationMs
    ) {
            this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            this.accessTokenExpirationMs = accessTokenExpirationMs;
            this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generate(User user) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("email", user.getEmail().value())
            .claim("role",  user.getRole().name())
            .claim("type", "access")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    public String generateRefreshToken(User user) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail().value())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

    public String getTokenType(String token) {
        try {
            Claims claims = parse(token);
            return claims.get("type", String.class);
        } catch (Exception ex) {
            log.debug("Could not extract token type: {}", ex.getMessage());
            return null;
        }
    }
}
