package com.tola.sentinelvault.vault.infrastructure.persistence;

import com.tola.sentinelvault.vault.domain.model.Secret;
import com.tola.sentinelvault.vault.domain.model.SecretId;
import com.tola.sentinelvault.vault.domain.repository.SecretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecretRepositoryAdapter implements SecretRepository {

    private final SpringDataSecretRepository springDataSecretRepository;

    @Override
    public Secret save(Secret secret) {
        springDataSecretRepository.save(toEntity(secret));
        return secret;
    }

    @Override
    public Optional<Secret> findById(SecretId secretId) {
        return springDataSecretRepository
                .findById(secretId.value())
                .map(this::toDomain);
    }

    @Override
    public List<Secret> findByOwnerId(UUID ownerId) {
        return springDataSecretRepository
                .findByOwnerId(ownerId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Secret> SearchByOwnerAndName(UUID ownerId, String nameFragment) {
        return springDataSecretRepository.searchByOwnerAndNameFragment(ownerId, nameFragment)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(SecretId secretId) {
        springDataSecretRepository.deleteById(secretId.value());
    }

    @Override
    public boolean existsByIdAndOwnerId(SecretId secretId, UUID ownerId) {
        return springDataSecretRepository.existsByIdAndOwnerId(secretId.value(), ownerId);
    }
    private JpaSecretEntity toEntity(Secret s) {
        return JpaSecretEntity.builder()
                .id(s.getSecretId().value())
                .name(s.getName())
                .ciphertext(s.getCiphertext())
                .ownerId(s.getOwnerId())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private Secret toDomain(JpaSecretEntity e) {
        return Secret.reconstitute(
                SecretId.of(e.getId()),
                e.getName(),
                e.getCiphertext(),
                e.getOwnerId(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
