package com.tola.sentinelvault.platform.exception;

import com.tola.sentinelvault.identity.application.LoginUseCase;
import com.tola.sentinelvault.identity.application.RegisterUserUseCase;
import com.tola.sentinelvault.identity.domain.model.InvalidEmailException;
import com.tola.sentinelvault.identity.domain.service.PasswordPolicyService;
import com.tola.sentinelvault.shared.domain.base.EntityNotFoundException;
import com.tola.sentinelvault.shared.domain.exception.DomainException;
import com.tola.sentinelvault.vault.domain.service.SecretAccessPolicy;
import com.tola.sentinelvault.vault.infrastructure.crypto.AesEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps domain and infrastructure exceptions to HTTP responses.
 *
 * Order of handlers matters — more specific exceptions first.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 400 Validation ───────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (a, b) -> a
                ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",    400);
        body.put("error",     "Validation failed");
        body.put("fields",    fieldErrors);
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity.badRequest().body(body);
    }

    // ── 400 Bad Request ──────────────────────────────────────────────

    @ExceptionHandler({
            InvalidEmailException.class,
            PasswordPolicyService.WeakPasswordException.class,
            AesEncryptionService.EncryptionException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── 401 Unauthorized ─────────────────────────────────────────────

    @ExceptionHandler(LoginUseCase.BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(DomainException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // ── 403 Forbidden ────────────────────────────────────────────────

    @ExceptionHandler({
            LoginUseCase.AccountDisabledException.class,
            SecretAccessPolicy.SecretAccessDeniedException.class
    })
    public ResponseEntity<Map<String, Object>> handleForbidden(DomainException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // ── 404 Not Found ────────────────────────────────────────────────

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409 Conflict ─────────────────────────────────────────────────

    @ExceptionHandler(RegisterUserUseCase.EmailAlreadyRegisteredException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DomainException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 500 Fallback ─────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // ── Helper ───────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",    status.value());
        body.put("error",     message);
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(status).body(body);
    }
}
