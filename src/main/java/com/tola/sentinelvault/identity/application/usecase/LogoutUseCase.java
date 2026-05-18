package com.tola.sentinelvault.identity.application.usecase;
import com.tola.sentinelvault.identity.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutUseCase {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void execute(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("User {} logged out — all refresh tokens revoked", userId);
    }
}