package com.tola.sentinelvault.identity.application.command;
/**
 * Command carrying credentials for a login attempt.
 */
public record LoginCommand(String email, String rawPassword) {
}
