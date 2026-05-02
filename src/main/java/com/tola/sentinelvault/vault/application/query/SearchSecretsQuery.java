package com.tola.sentinelvault.vault.application.query;

import java.util.UUID;

public record SearchSecretsQuery(
        UUID ownerId,
        String nameFragment
) {
}
