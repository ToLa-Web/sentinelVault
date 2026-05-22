package com.tola.sentinelvault.identity.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record RefreshTokenResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonIgnore
        String refreshToken,
        @JsonIgnore
        UUID userId
) {
}
