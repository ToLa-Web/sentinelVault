package com.tola.sentinelvault.vault.domain.service;
/**
 * Domain port for symmetric encryption.
 *
 * The domain layer depends only on this interface.
 * {@link com.sentinelvault.vault.infrastructure.crypto.AesEncryptionService}
 * provides the AES-256-GCM implementation.
 */
public interface EncryptionService  {

    String encrypt(String plaintext);

    String decrypt(String ciphertext);
}
