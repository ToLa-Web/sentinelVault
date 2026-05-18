package com.tola.sentinelvault.identity.domain.model;

import com.tola.sentinelvault.shared.domain.base.ValueObject;
import com.tola.sentinelvault.shared.domain.exception.DomainException;

public record TokenValue(String value) implements ValueObject {

    public TokenValue{
        if (value == null || value.isBlank()) {
            throw new InvalidTokenException("Refresh token value cannot be empty");
        }
        if (value.length() < 32 ) {
            throw new InvalidTokenException("Token value is too short for security standards");
        }
    }

    @Override
    public String toString() {
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    public static class InvalidTokenException extends DomainException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }
}
