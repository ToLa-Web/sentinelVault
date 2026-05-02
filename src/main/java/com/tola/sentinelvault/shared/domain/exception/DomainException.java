package com.tola.sentinelvault.shared.domain.exception;
/**
 * Base exception for all domain rule violations.
 *
 * Throw a subclass whenever an invariant or business rule is broken
 * inside the domain layer. The GlobalExceptionHandler maps these
 * to appropriate HTTP responses (typically 400 or 409).
 *
 * Example subclass:
 *
 *   public class SecretNotFoundException extends DomainException {
 *       public SecretNotFoundException(UUID secretId) {
 *           super("Secret not found: " + secretId);
 *       }
 *   }
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
