    package com.tola.sentinelvault.identity.infrastructure.web;

    import com.tola.sentinelvault.audit.application.command.RecordAuditEventCommand;
    import com.tola.sentinelvault.audit.domain.model.AuditActions;
    import com.tola.sentinelvault.audit.domain.model.AuditLog;
    import com.tola.sentinelvault.audit.domain.port.AuditEventPublisher;
    import com.tola.sentinelvault.identity.application.command.LoginCommand;
    import com.tola.sentinelvault.identity.application.command.RefreshTokenCommand;
    import com.tola.sentinelvault.identity.application.dto.*;
    import com.tola.sentinelvault.identity.application.usecase.LoginUseCase;
    import com.tola.sentinelvault.identity.application.command.RegisterUserCommand;
    import com.tola.sentinelvault.identity.application.usecase.RefreshAccessTokenUseCase;
    import com.tola.sentinelvault.identity.application.usecase.RegisterUserUseCase;
    import com.tola.sentinelvault.identity.application.usecase.LogoutUseCase;
    import com.tola.sentinelvault.identity.infrastructure.security.CustomUserPrincipal;
    import com.tola.sentinelvault.platform.dto.ApiResponse;
    import com.tola.sentinelvault.platform.ratelimit.RateLimitService;
    import com.tola.sentinelvault.platform.util.ClientIpResolver;
    import jakarta.validation.Valid;
    import jakarta.servlet.http.HttpServletRequest;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.web.bind.annotation.*;

    import java.time.Instant;
    import java.util.UUID;

    @RestController
    @RequestMapping("/api/v1/auth")
    @RequiredArgsConstructor
    public class AuthController {

        private final RegisterUserUseCase registerUserUseCase;
        private final LoginUseCase loginUseCase;
        private final RefreshAccessTokenUseCase refreshAccessTokenUseCase;
        private final LogoutUseCase logoutUseCase;
        private final RefreshTokenCookieFactory refreshTokenCookieFactory;
        private final RateLimitService rateLimitService;
        private final ClientIpResolver clientIpResolver;
        private final AuditEventPublisher auditEventPublisher;

        @PostMapping("/register")
        public ResponseEntity<ApiResponse<RegisterResponse>> register(
                @Valid @RequestBody RegisterRequest request,
                HttpServletRequest httpRequest) {
            String clientIp = clientIpResolver.resolve(httpRequest);
            String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
            rateLimitService.checkRateLimit(clientIp, "register");
            rateLimitService.checkRateLimit(request.email(), "register");
            try {
                RegisterUserCommand cmd = new RegisterUserCommand(request.email(), request.password());
                RegisterResponse response = registerUserUseCase.execute(cmd);
                auditEventPublisher.publish(new RecordAuditEventCommand(UUID.randomUUID(), response.userId(),
                        AuditActions.REGISTER_SUCCESS, "User", response.userId(), AuditLog.Outcome.SUCCESS,
                        clientIp, userAgent, "User registered: " + request.email(), Instant.now()
                ));
                return ApiResponse.ok(response, "Registration successful");
            } catch (Exception e) {
                auditEventPublisher.publish(new RecordAuditEventCommand(UUID.randomUUID(), null,
                        AuditActions.REGISTER_FAILURE, "User", null, AuditLog.Outcome.FAILURE,
                        clientIp, userAgent, "Registration failed: " + e.getMessage(), Instant.now()
                ));
                throw e;
            }
        }

        @PostMapping("/login")
        public ResponseEntity<ApiResponse<LoginResponse>> login(
                @Valid @RequestBody LoginRequest request,
                HttpServletRequest httpRequest) {

            String clientIp = clientIpResolver.resolve(httpRequest);
            String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
            rateLimitService.checkRateLimit(clientIp, "login");
            rateLimitService.checkRateLimit(request.email(), "login");

            try {
                LoginCommand cmd = new LoginCommand(request.email(), request.password());
                LoginResponse response = loginUseCase.execute(cmd);

                rateLimitService.reset(clientIp, "login");
                rateLimitService.reset(request.email(), "login");

                auditEventPublisher.publish(new RecordAuditEventCommand(UUID.randomUUID(), response.userId(),
                        AuditActions.LOGIN_SUCCESS, "User", response.userId(), AuditLog.Outcome.SUCCESS,
                        clientIp, userAgent, "Login successful: " + request.email(), Instant.now()
                ));

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE,
                                refreshTokenCookieFactory.buildTokenCookie(response.refreshToken()).toString())
                        .body(ApiResponse.success(response, "Login successful"));

            } catch (Exception e) {
                auditEventPublisher.publish(new RecordAuditEventCommand(UUID.randomUUID(), null,
                        AuditActions.LOGIN_FAILURE, "User", null, AuditLog.Outcome.FAILURE,
                        clientIp, userAgent, "Login failed: " + request.email(), Instant.now()
                ));
                throw e;
            }
        }

        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(
                HttpServletRequest httpRequest) {

            String clientIp = clientIpResolver.resolve(httpRequest);
            String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
            rateLimitService.checkRateLimit(clientIp, "refresh");

            try {
                String refreshTokenValue = refreshTokenCookieFactory.extractToken(httpRequest)
                        .orElseThrow(RefreshAccessTokenUseCase.InvalidRefreshTokenException::new);

                RefreshTokenCommand cmd = new RefreshTokenCommand(refreshTokenValue);
                RefreshTokenResponse response = refreshAccessTokenUseCase.execute(cmd);

                rateLimitService.reset(clientIp, "refresh");

                auditEventPublisher.publish(new RecordAuditEventCommand(UUID.randomUUID(), response.userId(),
                        AuditActions.TOKEN_REFRESH_OK, "Token", null, AuditLog.Outcome.SUCCESS,
                        clientIp, userAgent, "Token refreshed", Instant.now()
                ));

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE,
                                refreshTokenCookieFactory.buildTokenCookie(response.refreshToken()).toString())
                        .body(ApiResponse.success(response, "Token refreshed"));

            } catch (Exception e) {
                auditEventPublisher.publish(new RecordAuditEventCommand(UUID.randomUUID(), null,
                        AuditActions.TOKEN_REFRESH_FAIL, "Token", null, AuditLog.Outcome.FAILURE,
                        clientIp, userAgent, "Token refresh failed: " + e.getMessage(),     Instant.now()
                ));
                throw e;
            }
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(
                @AuthenticationPrincipal CustomUserPrincipal currentUser,
                HttpServletRequest httpRequest) {

            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated"));
            }
            String clientIp = clientIpResolver.resolve(httpRequest);
            String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
            rateLimitService.checkRateLimit(clientIp, "logout");
            logoutUseCase.execute(currentUser.getId());
            auditEventPublisher.publish(new RecordAuditEventCommand(UUID.randomUUID(), currentUser.getId(),
                    AuditActions.LOGOUT, "User", currentUser.getId(), AuditLog.Outcome.SUCCESS,
                    clientIp, userAgent, "User logged out", Instant.now()
            ));
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.clearCookie().toString())
                    .body(ApiResponse.success("Logout successful"));
        }
    }
