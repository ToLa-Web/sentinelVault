package com.tola.sentinelvault.vault.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataSecretRepository extends JpaRepository<JpaSecretEntity, UUID> {

    List<JpaSecretEntity> findByOwnerId(UUID ownerId);
    @Query("SELECT s FROM JpaSecretEntity s WHERE s.ownerId = :ownerId " +
            "AND LOWER(s.name) LIKE LOWER(CONCAT('%', :fragment, '%'))")
    List<JpaSecretEntity> searchByOwnerAndNameFragment(@Param("ownerId") UUID ownerId,@Param("fragment") String fragment);
    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);
}
