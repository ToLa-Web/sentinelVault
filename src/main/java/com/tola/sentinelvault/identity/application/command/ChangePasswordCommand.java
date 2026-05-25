package com.tola.sentinelvault.identity.application.command;

import java.util.UUID;

public record ChangePasswordCommand(
        UUID userId,
        String currentRawPassword,
        String newRawPassword
) {
}
