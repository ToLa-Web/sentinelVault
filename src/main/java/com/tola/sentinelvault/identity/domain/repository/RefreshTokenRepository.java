package com.tola.sentinelvault.identity.domain.repository;

import com.tola.sentinelvault.identity.domain.model.RefreshToken;
import com.tola.sentinelvault.identity.domain.model.TokenId;
import com.tola.sentinelvault.identity.domain.model.TokenValue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findById(TokenId tokenId);
    Optional<RefreshToken> findByTokenValue(String tokenValue);
    List<RefreshToken> findByUserId(UUID userId);
    List<RefreshToken> findValidByUserId(UUID userId);
    void deleteById(TokenId tokenId);
    boolean isValidToken(String tokenValue);
    void revokeAllByUserId(UUID userId);

}
