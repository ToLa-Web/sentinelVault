package com.tola.sentinelvault.identity.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tola.sentinelvault.identity.domain.model.Role;

import java.util.UUID;

public record LoginResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonIgnore
        String refreshToken,
        UUID userId,
        String email,
        Role role) {
}
