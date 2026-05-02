package com.tola.sentinelvault.identity.application.dto;

import java.util.UUID;

public record RegisterResponse(UUID userId, String email) {
}
