package com.tola.sentinelvault.identity.infrastructure.web;

import com.tola.sentinelvault.identity.application.command.LoginCommand;
import com.tola.sentinelvault.identity.application.usecase.LoginUseCase;
import com.tola.sentinelvault.identity.application.command.RegisterUserCommand;
import com.tola.sentinelvault.identity.application.usecase.RegisterUserUseCase;
import com.tola.sentinelvault.identity.application.dto.LoginRequest;
import com.tola.sentinelvault.identity.application.dto.LoginResponse;
import com.tola.sentinelvault.identity.application.dto.RegisterRequest;
import com.tola.sentinelvault.identity.application.dto.RegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        //Role role = request.role() != null ? Role.valueOf(request.role().toUpperCase()) : Role.MEMBER;
        RegisterUserCommand cmd = new RegisterUserCommand(request.email(), request.password());
        RegisterResponse response = registerUserUseCase.execute(cmd);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand cmd = new LoginCommand(request.email(), request.password());
        return ResponseEntity.ok(loginUseCase.execute(cmd));
    }
}
