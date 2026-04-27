package com.tola.sentinelvault.identity.domain.model;

import com.tola.sentinelvault.shared.domain.base.ValueObject;

import java.util.regex.Pattern;

/**
 * Value Object representing a validated email address.
 * Immutable — constructed once, always valid.
 */
public record Email(String value) implements ValueObject {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new InvalidEmailException("Email cannot be blank");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException("Invalid email format: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value.trim().toLowerCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
