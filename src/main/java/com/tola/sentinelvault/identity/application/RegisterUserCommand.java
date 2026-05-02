package com.tola.sentinelvault.identity.application;

import com.tola.sentinelvault.identity.domain.model.Role;
/**
 * Command carrying everything needed to register a new user.
 * Validated at the controller layer before reaching the use case.
 */
public record RegisterUserCommand(String email, String rawPassword, Role role) {}
