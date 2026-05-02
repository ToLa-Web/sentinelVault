package com.tola.sentinelvault.vault.application.dto;

import java.time.Instant;
import java.util.UUID;

public record SecretDetailResponse(
        UUID id,
        String name,
        String plaintext,
        UUID ownerId,
        Instant createdAt,
        Instant updateAt
) {
}
