package com.tola.sentinelvault.vault.domain.model;

import com.tola.sentinelvault.shared.domain.base.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root for the Vault bounded context.
 *
 * A Secret is owned by exactly one user and holds an
 * AES-encrypted ciphertext. The domain never sees raw keys —
 * encryption is delegated to the EncryptionService port.
 *
 * Invariants:
 *  - name is always non-blank
 *  - ciphertext is always non-blank (set by the use case after encryption)
 *  - ownerId is immutable after creation
 */
public class Secret extends AggregateRoot {

    private final SecretId id;
    private String name;
    private String ciphertext;       // AES-encrypted, base64-encoded
    private final UUID ownerId;
    private final Instant createdAt;
    private Instant updatedAt;

    public Secret(SecretId id, String name, String ciphertext, UUID ownerId, Instant createdAt, Instant updatedAt) {
        super(id.value());
        this.id         = id;
        this.name       = name;
        this.ciphertext = ciphertext;
        this.ownerId    = ownerId;
        this.createdAt  = createdAt;
        this.updatedAt  = updatedAt;
    }

    /**
     * Factory for creating a brand-new secret.
     * Raises {@link SecretCreatedEvent}.
     *
     * @param name       human-readable label
     * @param ciphertext already-encrypted content from EncryptionService
     * @param ownerId    ID of the owning user
     */

    public static Secret create(String name, String ciphertext, UUID ownerId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Secret name must not be blank");
        }
        if (ciphertext == null || ciphertext.isBlank()) {
            throw new IllegalArgumentException("Secret ciphertext must not be blank");
        }

        SecretId id = SecretId.generate();
        Instant now = Instant.now();
        Secret secret = new Secret(id, name, ciphertext, ownerId, now, now);
        secret.registerEvent(new SecretCreatedEvent(UUID.randomUUID(), now, id.value(), ownerId));
        return secret;
    }

    public static Secret reconstitute(SecretId id, String name, String ciphertext,
                                      UUID ownerId, Instant createdAt, Instant updatedAt) {
        return new Secret(id, name, ciphertext, ownerId, createdAt, updatedAt);
    }

    public void update(String newName, String newCiphertext) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Secret name must not be blank");
        }
        if (newCiphertext == null || newCiphertext.isBlank()) {
            throw new IllegalArgumentException("Secret ciphertext must not be blank");
        }
        this.name = newName;
        this.ciphertext = newCiphertext;
        this.updatedAt = Instant.now();
        registerEvent(new SecretUpdatedEvent(UUID.randomUUID(), this.updatedAt, id.value(), ownerId));
    }

    public SecretId getSecretId() { return id; }
    public String getName() { return name; }
    public String getCiphertext() { return ciphertext; }
    public UUID getOwnerId() { return ownerId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
