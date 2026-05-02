package com.tola.sentinelvault.vault.application.command;

import java.util.UUID;

public record CreateSecretCommand(
        String name,
        String plaintext,
        UUID ownerId
) {
}
