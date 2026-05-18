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

        String refreshTokenValue = command.refreshToken();
        // Validate the refresh token format
        if (!jwtProvider.isValid(refreshTokenValue)) {
            throw new InvalidRefreshTokenException();
        }
        String tokenType = jwtProvider.getTokenType(refreshTokenValue);
        if (!"refresh".equals(tokenType)) {
            throw new InvalidTokenTypeException();
        }
        // Find the refresh token in database and validate it
        RefreshToken refreshToken = refreshTokenRepository.findByTokenValue(refreshTokenValue)
                .orElseThrow(RefreshTokenNotFoundException::new);

        // Check if token is valid (not revoked and not expired)
        if (!refreshToken.isValid()) {
            // Determine the reason for invalidity
            if (refreshToken.isRevoked()) {
                throw new TokenRevokedException();
            } else if (refreshToken.isUsed()) {
                throw new TokenAlreadyUsedException();
            } else {
                throw new TokenExpiredException();
            }
        }

        User user = userRepository.findById(refreshToken.getUserId().getId())
                .orElseThrow(UserNotFoundException::new);

        if (!user.isEnabled()) {
            throw new AccountDisabledException();
        }
        refreshToken.markAsUsed();
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtProvider.generate(user);

        log.info("Issued new access token for user: {}", user.getId());
        return new RefreshTokenResponse(newAccessToken);
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
