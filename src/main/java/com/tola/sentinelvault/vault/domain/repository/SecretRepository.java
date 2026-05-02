package com.tola.sentinelvault.vault.domain.repository;

import com.tola.sentinelvault.vault.domain.model.Secret;
import com.tola.sentinelvault.vault.domain.model.SecretId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for Secret persistence.
 * Infrastructure provides the JPA-backed adapter.
 */
public interface SecretRepository {
    Secret save(Secret secret);
    Optional<Secret> findById(SecretId id);
    List<Secret> findByOwnerId(UUID ownerId);
    List<Secret> SearchByOwnerAndName(UUID ownerId, String nameFragment);
    void deleteById(SecretId id);
    boolean existsByIdAndOwnerId(SecretId id, UUID ownerId);

}
