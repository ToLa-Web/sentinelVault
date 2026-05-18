package com.tola.sentinelvault.identity.domain.model;

import com.tola.sentinelvault.shared.domain.base.ValueObject;

import java.util.UUID;

public record TokenId(UUID value) implements ValueObject {

    public TokenId {
        if (value == null) throw new IllegalArgumentException("TokenId cannot be null");
    }

    public static TokenId generate() {
        return new TokenId(UUID.randomUUID());
    }

    public static TokenId of(UUID value) {
        return new TokenId(value);
    }

    public static TokenId of(String value) {
        return new TokenId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
