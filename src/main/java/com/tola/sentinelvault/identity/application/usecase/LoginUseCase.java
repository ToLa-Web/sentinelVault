    package com.tola.sentinelvault.identity.application.usecase;

    import com.tola.sentinelvault.identity.application.command.LoginCommand;
    import com.tola.sentinelvault.identity.application.dto.LoginResponse;
    import com.tola.sentinelvault.identity.domain.model.Email;
    import com.tola.sentinelvault.identity.domain.model.InvalidEmailException;
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
        private final IssueRefreshTokenUseCase issueRefreshTokenUseCase;

        private static final String DUMMY_HASH = "$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M6Q0w8J8M5I4e7Y9K8K1L2M3N4O";

        @Transactional
        public LoginResponse execute(LoginCommand command) {

            Email email;
            try {
                email = Email.of(command.email());
            } catch (InvalidEmailException e) {
                throw new BadCredentialsException();
            }

            User user = userRepository.findByEmail(email).orElse(null);

            String hash = (user != null)
                    ? user.getPasswordHash()
                    : DUMMY_HASH;

            boolean validPassword = passwordEncoder.matches(command.rawPassword(), hash);
            if (user == null || !validPassword) {
                throw new BadCredentialsException();
            }
            if (!user.isEnabled()) {
                throw new AccountDisabledException();
            }

            String accessToken = jwtProvider.generate(user);
            String refreshToken = issueRefreshTokenUseCase.execute(user);
            return new LoginResponse(accessToken, refreshToken, user.getId(), user.getEmail().value(), user.getRole());
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
