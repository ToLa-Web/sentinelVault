package com.tola.sentinelvault.identity.domain.model;

import com.tola.sentinelvault.shared.domain.base.AggregateRoot;

import java.time.Instant;

public class RefreshToken extends AggregateRoot {

    private final TokenId id;
    private final TokenValue tokenValue;
    private final User userId;
    private final TokenExpiry expiryDate;
    private boolean revoked;
    private Instant revokedAt;
    private boolean used;
    private Instant usedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private RefreshToken(TokenId id, TokenValue tokenValue, User userId, TokenExpiry expiryDate, boolean revoked, Instant revokedAt, boolean used, Instant usedAt, Instant createdAt, Instant updatedAt) {
        super(id.value());
        this.id = id;
        this.tokenValue = tokenValue;
        this.userId = userId;
        this.expiryDate = expiryDate;
        this.revoked = revoked;
        this.revokedAt = revokedAt;
        this.used = used;
        this.usedAt = usedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RefreshToken issue(User userId, String tokenString) {
        Instant now = Instant.now();
        return new RefreshToken(
                TokenId.generate(),
                new TokenValue(tokenString),
                userId,
                TokenExpiry.defaultDuration(),
                false,
                null,
                false,
                null,
                now,
                now
        );
    }

    public static RefreshToken reconstitute(TokenId id, TokenValue tokenValue, User userId, TokenExpiry expiryDate, boolean revoked, Instant revokedAt, boolean used, Instant usedAt, Instant createdAt, Instant updatedAt) {
        return new RefreshToken(id, tokenValue, userId, expiryDate, revoked, revokedAt, used, usedAt, createdAt, updatedAt);
    }

    public boolean isValid() {
        return !revoked && !used && ! expiryDate.isExpired();
    }

    public void markAsUsed() {
        if (this.used) {
            throw new TokenAlreadyUsedException();
        }
        if (this.revoked) {
            throw new TokenAlreadyRevokedException();
        }
        this.used = true;
        this.usedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void revoke() {
        this.revoked = true;
        this.revokedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public TokenId getTokenId() {
        return id;
    }

    public TokenValue getTokenValue() {
        return tokenValue;
    }

    public User getUserId() {
        return userId;
    }

    public TokenExpiry getExpiryDate() {
        return expiryDate;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public boolean isUsed() {
        return used;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static class TokenAlreadyUsedException extends RuntimeException {
        public TokenAlreadyUsedException() {
            super("Refresh token has already been used");
        }
    }

    public static class TokenAlreadyRevokedException extends RuntimeException {
        public TokenAlreadyRevokedException() {
            super("Refresh token has already been revoked");
        }
    }
}
