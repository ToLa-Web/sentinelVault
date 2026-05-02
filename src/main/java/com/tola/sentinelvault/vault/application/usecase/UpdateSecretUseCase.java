package com.tola.sentinelvault.vault.application.usecase;

import com.tola.sentinelvault.shared.domain.base.DomainEventPublisher;
import com.tola.sentinelvault.shared.domain.base.EntityNotFoundException;
import com.tola.sentinelvault.vault.application.command.UpdateSecretCommand;
import com.tola.sentinelvault.vault.application.dto.SecretResponse;
import com.tola.sentinelvault.vault.domain.model.Secret;
import com.tola.sentinelvault.vault.domain.model.SecretId;
import com.tola.sentinelvault.vault.domain.repository.SecretRepository;
import com.tola.sentinelvault.vault.domain.service.EncryptionService;
import com.tola.sentinelvault.vault.domain.service.SecretAccessPolicy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateSecretUseCase {

    private final SecretRepository secretRepository;
    private final EncryptionService encryptionService;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public SecretResponse execute(UpdateSecretCommand command) {
        SecretId secretId = SecretId.of(command.secretId());

        Secret secret = secretRepository
                .findById(secretId)
                .orElseThrow(() -> new EntityNotFoundException("Secret", command.secretId()));
        SecretAccessPolicy.assertOwnership(secret, command.requestingUserId());
        String newCiphertext = encryptionService.encrypt(command.newPlaintext());
        secret.update(command.newName(), newCiphertext);
        secretRepository.save(secret);
        eventPublisher.publishAll(secret.pullEvents());

        return SecretResponse.from(secret);
    }
}
