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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterUserCommand cmd = new RegisterUserCommand(request.email(), request.password());
        RegisterResponse response = registerUserUseCase.execute(cmd);
        return ApiResponse.ok(response, "Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand cmd = new LoginCommand(request.email(), request.password());
        LoginResponse response = loginUseCase.execute(cmd);
        return ApiResponse.ok(response, "Login successful");
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenCommand cmd = new RefreshTokenCommand(request.refreshToken());
        RefreshTokenResponse response = refreshAccessTokenUseCase.execute(cmd);
        return ApiResponse.ok(response, "Token refreshed");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserPrincipal currentUser) {
        logoutUseCase.execute(currentUser.getId());
        return ApiResponse.ok(null, "Logout successful");
    }
}
