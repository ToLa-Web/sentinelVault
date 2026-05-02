package com.tola.sentinelvault.vault.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSecretRequest(
        @NotBlank(message = "Secret name is required")
        @Size(max = 100, message = "Secret name must be 100 characters or fewer")
        String name,

        @NotBlank(message = "Secret value is required")
        String value
) {
}
