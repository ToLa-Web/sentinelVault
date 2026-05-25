package com.tola.sentinelvault.platform.exception;

import com.tola.sentinelvault.identity.application.usecase.ChangePasswordUseCase;
import com.tola.sentinelvault.identity.application.usecase.LoginUseCase;
import com.tola.sentinelvault.identity.application.usecase.RefreshAccessTokenUseCase;
import com.tola.sentinelvault.identity.application.usecase.RegisterUserUseCase;
import com.tola.sentinelvault.identity.domain.model.InvalidEmailException;
import com.tola.sentinelvault.identity.domain.service.PasswordPolicyService;
import com.tola.sentinelvault.platform.dto.ApiResponse;
import com.tola.sentinelvault.platform.ratelimit.RateLimitService;
import com.tola.sentinelvault.shared.domain.base.EntityNotFoundException;
import com.tola.sentinelvault.shared.domain.exception.DomainException;
import com.tola.sentinelvault.vault.domain.service.SecretAccessPolicy;
import com.tola.sentinelvault.vault.infrastructure.crypto.AesEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps domain and infrastructure exceptions to HTTP responses.
 * Order of handlers matters — more specific exceptions first.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitService.RateLimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleRateLimitExceeded(RateLimitService.RateLimitExceededException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
        detail.setTitle("Rate limit exceeded");
        detail.setDetail(ex.getMessage());
        detail.setProperty("retryAfterSeconds", ex.getRetryAfterSeconds());
        detail.setProperty("timestamp", Instant.now().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()));

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).headers(headers).body(detail);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public <T> ResponseEntity<ApiResponse<T>> handleSpringAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied filter violation: {}", ex.getMessage());
        return ApiResponse.forbidden("Access denied — insufficient privileges or missing credentials");
    }

    @ExceptionHandler(AuthenticationException.class)
    public <T> ResponseEntity<ApiResponse<T>> handleSecurityFilterException(AuthenticationException ex) {
        log.warn("Security filter blocked request: {}", ex.getMessage());
        return ApiResponse.unauthorized("Unauthorized — valid JWT required");
    }

    // ── 400 Validation

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (a, b) -> a
                ));

        // Leverage your uniform DTO structure directly
        return ResponseEntity.badRequest().body(
                ApiResponse.error("Validation failed", fieldErrors)
        );
    }

    // ── 400 Bad Request

    @ExceptionHandler({
            InvalidEmailException.class,
            PasswordPolicyService.WeakPasswordException.class,
            AesEncryptionService.EncryptionException.class,
            IllegalArgumentException.class,
            ChangePasswordUseCase.CurrentPasswordMismatchException.class,
            ChangePasswordUseCase.SamePasswordException.class,
    })
    public <T> ResponseEntity<ApiResponse<T>> handleBadRequest(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ApiResponse.badRequest(ex.getMessage());
    }

    // ── 401 Unauthorized

    @ExceptionHandler({
            LoginUseCase.BadCredentialsException.class,
            RefreshAccessTokenUseCase.InvalidRefreshTokenException.class,
            RefreshAccessTokenUseCase.InvalidTokenTypeException.class
    })
    public <T> ResponseEntity<ApiResponse<T>> handleUnauthorized(DomainException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return ApiResponse.unauthorized(ex.getMessage());
    }

    // ── 403 Forbidden

    @ExceptionHandler({
            LoginUseCase.AccountDisabledException.class,
            RefreshAccessTokenUseCase.AccountDisabledException.class,
            SecretAccessPolicy.SecretAccessDeniedException.class
    })
    public <T> ResponseEntity<ApiResponse<T>> handleForbidden(DomainException ex) {
        log.warn("Forbidden: {}", ex.getMessage());
        return ApiResponse.forbidden(ex.getMessage());
    }

    // ── 404 Not Found

    @ExceptionHandler({
            EntityNotFoundException.class,
            RefreshAccessTokenUseCase.RefreshTokenNotFoundException.class,
            ChangePasswordUseCase.UserNotFoundException.class,
    })
    public <T> ResponseEntity<ApiResponse<T>> handleNotFound(DomainException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return ApiResponse.notFound(ex.getMessage());
    }

    // ── 409 Conflict

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

    // ── 500 Fallback

    @ExceptionHandler(Exception.class)
    public <T> ResponseEntity<ApiResponse<T>> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ApiResponse.internalError("An unexpected error occurred");
    }
}
