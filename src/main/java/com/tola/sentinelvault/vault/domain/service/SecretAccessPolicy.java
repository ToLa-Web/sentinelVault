package com.tola.sentinelvault.vault.domain.service;

import com.tola.sentinelvault.shared.domain.exception.DomainException;
import com.tola.sentinelvault.vault.domain.model.Secret;
import com.tola.sentinelvault.vault.domain.model.SecretId;

import java.util.UUID;

public class SecretAccessPolicy {

    private SecretAccessPolicy() {}

    public static void assertOwnership(Secret secret, UUID requestingUserId) {
        if (!secret.getOwnerId().equals(requestingUserId)) {
            throw new SecretAccessDeniedException(secret.getSecretId(), requestingUserId);
        }
    }

    public static class SecretAccessDeniedException extends DomainException {
        public SecretAccessDeniedException(SecretId secretId, UUID userId) {
            super("User " + userId + " does not have access to secret " + secretId);
        }
    }
}
