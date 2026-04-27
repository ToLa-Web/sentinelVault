package com.tola.sentinelvault.identity.application;

import com.tola.sentinelvault.identity.application.dto.LoginResponse;
import com.tola.sentinelvault.identity.domain.model.Email;
import com.tola.sentinelvault.identity.domain.model.User;
import com.tola.sentinelvault.identity.domain.repository.UserRepository;
import com.tola.sentinelvault.identity.infrastructure.security.JwtProvider;
import com.tola.sentinelvault.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional(readOnly = true)
    public LoginResponse execute(LoginCommand Command) {

        Email email = Email.of(Command.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(BadCredentialsException::new);

        if (!user.isEnabled()) {
            throw new AccountDisabledException();
        }

        if (!passwordEncoder.matches(Command.rawPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException();
        }

        String token = jwtProvider.generate(user);
        return new LoginResponse(token, user.getId(), user.getEmail().value(), user.getRole());
    }

    public static class BadCredentialsException extends DomainException {
        public BadCredentialsException() {
            super("Invalid email or password");
        }
    }

    public static class AccountDisabledException extends DomainException {
        public AccountDisabledException() {
            super("Account is disabled");
        }
    }
}
