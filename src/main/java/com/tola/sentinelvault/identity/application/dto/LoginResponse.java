package com.tola.sentinelvault.identity.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tola.sentinelvault.identity.domain.model.Role;

import java.util.UUID;

public record LoginResponse(
        @JsonProperty("access_token")
        String token,
        UUID userId,
        String email,
        Role role) {
}
