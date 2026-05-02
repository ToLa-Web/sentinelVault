package com.tola.sentinelvault.identity.application;
/**
 * Command carrying credentials for a login attempt.
 */
public record LoginCommand(String email, String rawPassword) {
}
