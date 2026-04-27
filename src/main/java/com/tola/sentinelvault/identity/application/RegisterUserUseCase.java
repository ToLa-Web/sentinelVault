package com.tola.sentinelvault.identity.application;

import com.tola.sentinelvault.identity.application.dto.RegisterResponse;
import com.tola.sentinelvault.identity.domain.model.Email;
import com.tola.sentinelvault.identity.domain.model.User;
import com.tola.sentinelvault.identity.domain.repository.UserRepository;
import com.tola.sentinelvault.identity.domain.service.PasswordPolicyService;
import com.tola.sentinelvault.shared.domain.base.DomainEventPublisher;
import com.tola.sentinelvault.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordPolicyService passwordPolicyService;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public RegisterResponse execute(RegisterUserCommand command) {

        passwordPolicyService.validate(command.rawPassword());
        Email email = new Email(command.email());

        if(userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email.value());
        }

        String hash = passwordEncoder.encode(command.rawPassword());
        User user = User.register(email, hash, command.role());

        userRepository.save(user);

        eventPublisher.publishAll(user.pullEvents());
        return new RegisterResponse(user.getId(), user.getEmail().value());
    }

    public static class EmailAlreadyRegisteredException extends DomainException {
        public EmailAlreadyRegisteredException(String email) {
            super("Email already registered: " + email);
        }
    }
}
