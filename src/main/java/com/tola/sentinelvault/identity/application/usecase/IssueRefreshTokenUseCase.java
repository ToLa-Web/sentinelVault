package com.tola.sentinelvault.identity.application.usecase;

import com.tola.sentinelvault.identity.domain.model.RefreshToken;
import com.tola.sentinelvault.identity.domain.model.User;
import com.tola.sentinelvault.identity.domain.repository.RefreshTokenRepository;
import com.tola.sentinelvault.identity.infrastructure.security.JwtProvider;
import com.tola.sentinelvault.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueRefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public String execute(User user) {
        if (!user.isEnabled()) {
            throw new AccountDisabledException();
        }

        String refreshTokenString = jwtProvider.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.issue(user, refreshTokenString);
        refreshTokenRepository.save(refreshToken);

        return refreshTokenString;
    }

    public static class AccountDisabledException extends DomainException {
        public AccountDisabledException() {
            super("Account is disabled");
        }
    }
}
