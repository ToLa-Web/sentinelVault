package com.tola.sentinelvault.vault.domain.model;

import com.tola.sentinelvault.shared.domain.base.ValueObject;

import java.util.UUID;

public record SecretId(UUID value) implements ValueObject {

    public SecretId {
        if (value == null) throw new IllegalArgumentException("SecretId cannot be null");
    }

    public static SecretId generate() {
        return new SecretId(UUID.randomUUID());
    }

    public static SecretId of(UUID value) {
        return new SecretId(value);
    }

    public static SecretId of(String value) {
        return new SecretId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
