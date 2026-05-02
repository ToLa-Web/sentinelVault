package com.tola.sentinelvault.vault.application.dto;

import com.tola.sentinelvault.vault.domain.model.Secret;

import java.time.Instant;
import java.util.UUID;

public record SecretResponse(
        UUID id,
        String name,
        UUID ownerId,
        Instant createdAt,
        Instant updateAt
) {
    public static SecretResponse from(Secret secret) {
        return new SecretResponse(
                secret.getSecretId().value(),
                secret.getName(),
                secret.getOwnerId(),
                secret.getCreatedAt(),
                secret.getUpdatedAt()
        );
    }
}
