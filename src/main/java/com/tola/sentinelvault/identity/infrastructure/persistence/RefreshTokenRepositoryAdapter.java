package com.tola.sentinelvault.identity.infrastructure.persistence;

import com.tola.sentinelvault.identity.domain.model.*;
import com.tola.sentinelvault.identity.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;
    private final UserRepositoryAdapter userRepositoryAdapter;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        JpaRefreshTokenEntity entity = toEntity(refreshToken);
        springDataRefreshTokenRepository.save(entity);
        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findById(TokenId tokenId) {
        return springDataRefreshTokenRepository.findById(tokenId.value())
                .map(this::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByTokenValue(String tokenValue) {
        return springDataRefreshTokenRepository.findByTokenValue(tokenValue)
                .map(this::toDomain);
    }

    public List<RefreshToken> findByUserId(UUID userId) {
        return springDataRefreshTokenRepository.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<RefreshToken> findValidByUserId(UUID userId) {
        return springDataRefreshTokenRepository.findValidTokensByUserId(userId, Instant.now())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(TokenId tokenId) {
        springDataRefreshTokenRepository.deleteById(tokenId.value());
    }

    @Override
    public boolean isValidToken(TokenValue tokenValue) {
        return springDataRefreshTokenRepository.findValidToken(tokenValue).isPresent();
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        springDataRefreshTokenRepository.revokeAllByUserId(userId, Instant.now());
    }

    private JpaRefreshTokenEntity toEntity(RefreshToken refreshToken) {
        return JpaRefreshTokenEntity.builder()
                .id(refreshToken.getTokenId().value())
                .tokenValue(refreshToken.getTokenValue().value())
                .userId(refreshToken.getUserId().getId())
                .expiryDate(refreshToken.getExpiryDate().value())
                .revoked(refreshToken.isRevoked())
                .revokedAt(refreshToken.getRevokedAt())
                .used(refreshToken.isUsed())
                .usedAt(refreshToken.getUsedAt())
                .createdAt(refreshToken.getCreatedAt())
                .updatedAt(refreshToken.getUpdatedAt())
                .build();
    }

    private RefreshToken toDomain(JpaRefreshTokenEntity entity) {
        Optional<User> user = userRepositoryAdapter.findById(entity.getUserId());
        if (user.isEmpty()) {
            throw new IllegalStateException("User not found for refresh token: " + entity.getId());
        }

        return RefreshToken.reconstitute(
                TokenId.of(entity.getId()),
                new TokenValue(entity.getTokenValue()),
                user.get(),
                TokenExpiry.of(entity.getExpiryDate()),
                entity.isRevoked(),
                entity.getRevokedAt(),
                entity.isUsed(),
                entity.getUsedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
