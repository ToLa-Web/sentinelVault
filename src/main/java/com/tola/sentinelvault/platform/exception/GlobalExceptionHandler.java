package com.tola.sentinelvault.platform.exception;

import com.tola.sentinelvault.identity.application.usecase.LoginUseCase;
import com.tola.sentinelvault.identity.application.usecase.RefreshAccessTokenUseCase;
import com.tola.sentinelvault.identity.application.usecase.RegisterUserUseCase;
import com.tola.sentinelvault.identity.domain.model.InvalidEmailException;
import com.tola.sentinelvault.identity.domain.service.PasswordPolicyService;
import com.tola.sentinelvault.platform.dto.ApiResponse;
import com.tola.sentinelvault.shared.domain.base.EntityNotFoundException;
import com.tola.sentinelvault.shared.domain.exception.DomainException;
import com.tola.sentinelvault.vault.domain.service.SecretAccessPolicy;
import com.tola.sentinelvault.vault.infrastructure.crypto.AesEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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
        body.put("success", false);
        body.put("message", "Validation failed");
        body.put("data", fieldErrors);
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
    public <T> ResponseEntity<ApiResponse<T>> handleBadRequest(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ApiResponse.badRequest(ex.getMessage());
    }

    // ── 401 Unauthorized ─────────────────────────────────────────────

    @ExceptionHandler({
            LoginUseCase.BadCredentialsException.class,
            RefreshAccessTokenUseCase.InvalidRefreshTokenException.class,
            RefreshAccessTokenUseCase.InvalidTokenTypeException.class
    })
    public <T> ResponseEntity<ApiResponse<T>> handleUnauthorized(DomainException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return ApiResponse.unauthorized(ex.getMessage());
    }

    // ── 403 Forbidden ────────────────────────────────────────────────

    @ExceptionHandler({
            LoginUseCase.AccountDisabledException.class,
            RefreshAccessTokenUseCase.AccountDisabledException.class,
            SecretAccessPolicy.SecretAccessDeniedException.class
    })
    public <T> ResponseEntity<ApiResponse<T>> handleForbidden(DomainException ex) {
        log.warn("Forbidden: {}", ex.getMessage());
        return ApiResponse.forbidden(ex.getMessage());
    }

    // ── 404 Not Found ────────────────────────────────────────────────

    @ExceptionHandler({
            EntityNotFoundException.class,
            RefreshAccessTokenUseCase.RefreshTokenNotFoundException.class
    })
    public <T> ResponseEntity<ApiResponse<T>> handleNotFound(DomainException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return ApiResponse.notFound(ex.getMessage());
    }

    // ── 409 Conflict ─────────────────────────────────────────────────

    @ExceptionHandler({
            RegisterUserUseCase.EmailAlreadyRegisteredException.class,
            RefreshAccessTokenUseCase.TokenRevokedException.class,
            RefreshAccessTokenUseCase.TokenAlreadyUsedException.class,
            RefreshAccessTokenUseCase.TokenExpiredException.class
    })
    public <T> ResponseEntity<ApiResponse<T>> handleConflict(DomainException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ApiResponse.conflict(ex.getMessage());
    }

    // ── 500 Fallback ─────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public <T> ResponseEntity<ApiResponse<T>> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ApiResponse.internalError("An unexpected error occurred");
    }
}
