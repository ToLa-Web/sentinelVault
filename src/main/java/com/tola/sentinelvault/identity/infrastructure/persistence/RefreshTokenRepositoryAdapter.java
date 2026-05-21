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
                .map(entity -> {
                    User user = userRepositoryAdapter.findById(entity.getUserId())
                            .orElseThrow(() -> new IllegalStateException("User not found: " + entity.getUserId()));
                    return toDomain(entity, user);
                });
    }

    @Override
    public Optional<RefreshToken> findByTokenValue(String tokenValue) {
        return springDataRefreshTokenRepository.findByTokenValue(tokenValue)
                .map(entity -> {
                    User user = userRepositoryAdapter.findById(entity.getUserId())
                            .orElseThrow(() -> new IllegalStateException("User not found: " + entity.getUserId()));
                    return toDomain(entity, user);
                });
    }

    public List<RefreshToken> findByUserId(UUID userId) {
        User user = userRepositoryAdapter.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        return springDataRefreshTokenRepository.findByUserId(userId)
                .stream()
                .map(entity -> toDomain(entity, user))
                .toList();
    }

    @Override
    public List<RefreshToken> findValidByUserId(UUID userId) {
        User user = userRepositoryAdapter.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        return springDataRefreshTokenRepository.findValidTokensByUserId(userId, Instant.now())
                .stream()
                .map(entity -> toDomain(entity, user))
                .toList();
    }

    @Override
    public void deleteById(TokenId tokenId) {
        springDataRefreshTokenRepository.deleteById(tokenId.value());
    }

    @Override
    public boolean isValidToken(String tokenValue) {
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

    private RefreshToken toDomain(JpaRefreshTokenEntity entity, User user) {
        return RefreshToken.reconstitute(
                TokenId.of(entity.getId()),
                new TokenValue(entity.getTokenValue()),
                user,
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
