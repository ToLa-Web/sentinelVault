    package com.tola.sentinelvault.identity.infrastructure.web;

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

        @PostMapping("/register")
        public ResponseEntity<ApiResponse<RegisterResponse>> register(
                @Valid @RequestBody RegisterRequest request,
                HttpServletRequest httpRequest) {
            String clientIp = clientIpResolver.resolve(httpRequest);
            rateLimitService.checkRateLimit(clientIp, "register");
            rateLimitService.checkRateLimit(request.email(), "register");

            RegisterUserCommand cmd = new RegisterUserCommand(request.email(), request.password());
            RegisterResponse response = registerUserUseCase.execute(cmd);

            return ApiResponse.ok(response, "Registration successful");
        }

        @PostMapping("/login")
        public ResponseEntity<ApiResponse<LoginResponse>> login(
                @Valid @RequestBody LoginRequest request,
                HttpServletRequest httpRequest) {

            String clientIp = clientIpResolver.resolve(httpRequest);
            rateLimitService.checkRateLimit(clientIp, "login");
            rateLimitService.checkRateLimit(request.email(), "login");

            LoginCommand cmd = new LoginCommand(request.email(), request.password());
            LoginResponse response = loginUseCase.execute(cmd);

            rateLimitService.reset(clientIp, "login");
            rateLimitService.reset(request.email(), "login");

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.buildTokenCookie(response.refreshToken()).toString())
                    .body(ApiResponse.success(response, "Login successful"));
        }

        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(
                HttpServletRequest httpRequest) {

            String clientIp = clientIpResolver.resolve(httpRequest);
            rateLimitService.checkRateLimit(clientIp, "refresh");

            String refreshTokenValue = refreshTokenCookieFactory.extractToken(httpRequest)
                    .orElseThrow(RefreshAccessTokenUseCase.InvalidRefreshTokenException::new);
            RefreshTokenCommand cmd = new RefreshTokenCommand(refreshTokenValue);
            RefreshTokenResponse response = refreshAccessTokenUseCase.execute(cmd);

            rateLimitService.reset(clientIp, "refresh");

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE,
                            refreshTokenCookieFactory.buildTokenCookie(response.refreshToken()).toString())
                    .body(ApiResponse.success(response, "Token refreshed"));
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(
                @AuthenticationPrincipal CustomUserPrincipal currentUser,
                HttpServletRequest httpRequest) {

            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated"));
            }

            String clientIp = clientIpResolver.resolve(httpRequest);
            rateLimitService.checkRateLimit(clientIp, "logout");
            logoutUseCase.execute(currentUser.getId());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.clearCookie().toString())
                    .body(ApiResponse.success("Logout successful"));
        }
    }
