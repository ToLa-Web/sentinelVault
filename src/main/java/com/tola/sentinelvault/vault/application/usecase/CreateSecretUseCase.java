package com.tola.sentinelvault.vault.application.usecase;

import com.tola.sentinelvault.shared.domain.base.DomainEventPublisher;
import com.tola.sentinelvault.vault.application.command.CreateSecretCommand;
import com.tola.sentinelvault.vault.application.dto.SecretResponse;
import com.tola.sentinelvault.vault.domain.model.Secret;
import com.tola.sentinelvault.vault.domain.repository.SecretRepository;
import com.tola.sentinelvault.vault.domain.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
/**
 * Creates a new secret.
 * Flow:
 *  1. Encrypt plaintext via EncryptionService port
 *  2. Create Secret aggregate (raises SecretCreatedEvent)
 *  3. Persist
 *  4. Publish domain events
 *  5. Return response (never returns plaintext or ciphertext)
 */
@Service
@RequiredArgsConstructor
public class CreateSecretUseCase {

    private final SecretRepository secretRepository;
    private final EncryptionService encryptionService;
    private final DomainEventPublisher eventPublisher;

    public SecretResponse execute(CreateSecretCommand command) {

        String ciphertext = encryptionService.encrypt(command.plaintext());

        Secret secret = Secret.create(command.name(), ciphertext, command.ownerId());
        secretRepository.save(secret);
        eventPublisher.publishAll(secret.pullEvents());

        return SecretResponse.from(secret);
    }
}
