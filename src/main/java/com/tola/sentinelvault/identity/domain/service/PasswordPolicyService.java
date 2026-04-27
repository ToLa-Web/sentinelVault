package com.tola.sentinelvault.identity.domain.service;

import com.tola.sentinelvault.shared.domain.exception.DomainException;
import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {

    private static final int MIN_LENGTH = 8;

    public void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            throw new WeakPasswordException("Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (!rawPassword.chars().anyMatch(Character::isUpperCase)) {
            throw new WeakPasswordException("Password must contain at least one uppercase letter");
        }
        if (!rawPassword.chars().anyMatch(c -> "!@#$%^&*()-_=+[]{}|;:',.<>?/`~".indexOf(c) >= 0)) {
            throw new WeakPasswordException(
                    "Password must contain at least one special character");
        }
    }

    public static class WeakPasswordException extends DomainException {
        public WeakPasswordException(String message) {
            super(message);
        }
    }
}
