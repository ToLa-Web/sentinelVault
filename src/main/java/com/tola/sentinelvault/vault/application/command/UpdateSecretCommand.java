package com.tola.sentinelvault.vault.application.command;

import java.util.UUID;

public record UpdateSecretCommand(
        UUID secretId,
        String newName,
        String newPlaintext,
        UUID requestingUserId
) {
}
