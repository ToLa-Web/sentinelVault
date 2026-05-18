package com.tola.sentinelvault.identity.infrastructure.persistence;

import com.tola.sentinelvault.identity.domain.model.TokenValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataRefreshTokenRepository extends JpaRepository<JpaRefreshTokenEntity, UUID> {
    Optional<JpaRefreshTokenEntity> findByTokenValue(String tokenValue);
    List<JpaRefreshTokenEntity>  findByUserId(UUID userId);
    @Query("SELECT rt FROM JpaRefreshTokenEntity rt WHERE rt.tokenValue = :tokenValue AND rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP")
    Optional<JpaRefreshTokenEntity> findValidToken(@Param("tokenValue") TokenValue tokenValue);
    @Query("SELECT rt FROM JpaRefreshTokenEntity rt WHERE rt.userId = :userId AND rt.revoked = false AND rt.used = false AND rt.expiryDate > :now")
    List<JpaRefreshTokenEntity> findValidTokensByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
    // Revoke all tokens for a user
    @Modifying
    @Transactional
    @Query("UPDATE JpaRefreshTokenEntity rt SET rt.revoked = true, rt.revokedAt = :now, rt.updatedAt = :now WHERE rt.userId = :userId AND rt.revoked = false")
    void revokeAllByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    // Find tokens that are used but not revoked (for audit purposes)
    @Query("SELECT rt FROM JpaRefreshTokenEntity rt WHERE rt.userId = :userId AND rt.used = true AND rt.revoked = false")
    List<JpaRefreshTokenEntity> findUsedTokensByUserId(@Param("userId") UUID userId);

    // Find tokens that are revoked but not used (immediate revocation before use)
    @Query("SELECT rt FROM JpaRefreshTokenEntity rt WHERE rt.userId = :userId AND rt.revoked = true AND rt.used = false")
    List<JpaRefreshTokenEntity> findRevokedUnusedTokensByUserId(@Param("userId") UUID userId);
}
