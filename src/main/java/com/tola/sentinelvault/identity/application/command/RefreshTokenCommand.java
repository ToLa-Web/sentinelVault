package com.tola.sentinelvault.identity.application.command;

public record RefreshTokenCommand(
        String refreshToken
) {
}
