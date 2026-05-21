package com.tola.sentinelvault.identity.application.usecase;

import com.tola.sentinelvault.identity.application.command.RefreshTokenCommand;
import com.tola.sentinelvault.identity.application.dto.RefreshTokenResponse;
import com.tola.sentinelvault.identity.domain.model.RefreshToken;
import com.tola.sentinelvault.identity.domain.model.User;
import com.tola.sentinelvault.identity.domain.repository.RefreshTokenRepository;
import com.tola.sentinelvault.identity.domain.repository.UserRepository;
import com.tola.sentinelvault.identity.infrastructure.security.JwtProvider;
import com.tola.sentinelvault.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshAccessTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    /**     * Exchange a refresh token for a new access token     */
    @Transactional
    public RefreshTokenResponse execute(RefreshTokenCommand command) {

        String tokenValue = command.refreshToken();

        if (!jwtProvider.isValid(tokenValue)) throw new InvalidRefreshTokenException();
        if (!"refresh".equals(jwtProvider.getTokenType(tokenValue))) throw new InvalidTokenTypeException();

        RefreshToken refreshToken = refreshTokenRepository.findByTokenValue(tokenValue)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (!refreshToken.isValid()) {
            if (refreshToken.isRevoked()) throw new TokenRevokedException();
            if (refreshToken.isUsed()) {
                // Reuse of an invalidated token — kill entire user session family
                refreshTokenRepository.revokeAllByUserId(refreshToken.getUserId().getId());
                log.warn("Refresh token reuse detected, session family revoked for user: {}",
                        refreshToken.getUserId().getId());
                throw new TokenAlreadyUsedException();
            }
            throw new TokenExpiredException();
        }

        User user = refreshToken.getUserId(); // already loaded — no second DB call needed
        if (user == null) throw new UserNotFoundException();
        if (!user.isEnabled()) throw new AccountDisabledException();

        refreshToken.markAsUsed();
        refreshTokenRepository.save(refreshToken);

        String newAccessToken  = jwtProvider.generate(user);
        String newRefreshToken = jwtProvider.generateRefreshToken(user);

        refreshTokenRepository.save(RefreshToken.issue(user, newRefreshToken));

        log.info("Rotated refresh token for user: {}", user.getId());
        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    public static class InvalidRefreshTokenException extends DomainException {
        public InvalidRefreshTokenException() {
            super("Invalid refresh token format");
        }
    }

    public static class InvalidTokenTypeException extends DomainException {
        public InvalidTokenTypeException() {
            super("Token is not a refresh token");
        }
    }

    public static class RefreshTokenNotFoundException extends DomainException {
        public RefreshTokenNotFoundException() {
            super("Refresh token not found in database");
        }
    }

    public static class TokenRevokedException extends DomainException {
        public TokenRevokedException() {
            super("Refresh token has been revoked");
        }
    }

    public static class TokenAlreadyUsedException extends DomainException {
        public TokenAlreadyUsedException() {
            super("Refresh token has already been used — possible replay attack detected");
        }
    }

    public static class TokenExpiredException extends DomainException {
        public TokenExpiredException() {
            super("Refresh token has expired");
        }
    }

    public static class UserNotFoundException extends DomainException {
        public UserNotFoundException() {
            super("User associated with refresh token not found");
        }
    }

    public static class AccountDisabledException extends DomainException {
        public AccountDisabledException() {
            super("Account is disabled");
        }
    }
}
