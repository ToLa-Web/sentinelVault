package com.tola.sentinelvault.vault.domain.model;

import com.tola.sentinelvault.shared.domain.base.ValueObject;

public record SecretContent(String plainText) implements ValueObject {

    public SecretContent {
        if (plainText == null || plainText.isBlank()) {
            throw new IllegalArgumentException("plainText must not be null or blank");
        }
    }

    public static SecretContent of(String plainText) {
        return new SecretContent(plainText);
    }

     @Override
    public String toString() {
        return "SecretContent[REDACTED]";
     }
}
