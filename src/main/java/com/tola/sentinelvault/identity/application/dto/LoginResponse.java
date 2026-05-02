package com.tola.sentinelvault.identity.application.dto;

import com.tola.sentinelvault.identity.domain.model.Role;

import java.util.UUID;

public record LoginResponse(String token, UUID userId, String email, Role role) {
}
