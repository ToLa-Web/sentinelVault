package com.tola.sentinelvault.identity.application.usecase;

import com.tola.sentinelvault.identity.application.command.ChangePasswordCommand;
import com.tola.sentinelvault.identity.domain.model.User;
import com.tola.sentinelvault.identity.domain.repository.RefreshTokenRepository;
import com.tola.sentinelvault.identity.domain.repository.UserRepository;
import com.tola.sentinelvault.identity.domain.service.PasswordPolicyService;
import com.tola.sentinelvault.shared.domain.base.DomainEventPublisher;
import com.tola.sentinelvault.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordPolicyService passwordPolicyService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public void execute(ChangePasswordCommand cmd) {
        User user = userRepository.findById(cmd.userId())
                .orElseThrow(UserNotFoundException::new);

        verifyCurrentPassword(cmd.currentRawPassword(), user.getPasswordHash());
        verifyNewPasswordIsDifferent(cmd.newRawPassword(), user.getPasswordHash());
        passwordPolicyService.validate(cmd.newRawPassword());

        user.changePassword(passwordEncoder.encode(cmd.newRawPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(user.getId());
        eventPublisher.publishAll(user.pullEvents());

        log.info("[ChangePassword] Password changed - userId: {}", user.getId());
    }

    private void verifyCurrentPassword(String rawPassword, String storedHash) {
        if (!passwordEncoder.matches(rawPassword, storedHash)) {
            throw new CurrentPasswordMismatchException();
        }
    }

    private void verifyNewPasswordIsDifferent(String newRawPassword, String currentHash) {
        if (passwordEncoder.matches(newRawPassword, currentHash)) {
            throw new SamePasswordException();
        }
    }

    public static class UserNotFoundException extends DomainException {
        public UserNotFoundException() { super("User not found"); }
    }

    public static class CurrentPasswordMismatchException extends DomainException {
        public CurrentPasswordMismatchException() { super("Current password is incorrect"); }
    }

    public static class SamePasswordException extends DomainException {
        public SamePasswordException() { super("New password must be different from the current password"); }
    }
}