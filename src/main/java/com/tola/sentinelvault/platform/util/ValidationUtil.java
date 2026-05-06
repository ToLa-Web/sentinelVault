package com.tola.sentinelvault.platform.util;

import java.util.UUID;

/**
 * Stateless validation utility methods shared across bounded contexts.
 */
public final class ValidationUtil {

    private ValidationUtil() {}

    public static boolean isValidUUID(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (isNullOrBlank(value)) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}