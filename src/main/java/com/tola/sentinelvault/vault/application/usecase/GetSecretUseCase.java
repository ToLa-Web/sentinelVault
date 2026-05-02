package com.tola.sentinelvault.vault.application.usecase;

import com.tola.sentinelvault.shared.domain.base.EntityNotFoundException;
import com.tola.sentinelvault.vault.application.dto.SecretDetailResponse;
import com.tola.sentinelvault.vault.domain.model.Secret;
import com.tola.sentinelvault.vault.domain.model.SecretId;
import com.tola.sentinelvault.vault.domain.repository.SecretRepository;
import com.tola.sentinelvault.vault.domain.service.EncryptionService;
import com.tola.sentinelvault.vault.domain.service.SecretAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetSecretUseCase {
    private final EncryptionService encryptionService;
    private final SecretRepository secretRepository;

    @Transactional(readOnly = true)
    public SecretDetailResponse execute( UUID secretId, UUID requestingUserId) {
        Secret secret = secretRepository.findById(SecretId.of(secretId))
                .orElseThrow(() -> new EntityNotFoundException("Secret", secretId));

        SecretAccessPolicy.assertOwnership(secret, requestingUserId);
        String plaintext = encryptionService.decrypt(secret.getCiphertext());
        return new SecretDetailResponse(
                secret.getSecretId().value(),
                secret.getName(),
                plaintext,
                secret.getOwnerId(),
                secret.getCreatedAt(),
                secret.getUpdatedAt()
        );
    }
}
