package com.tola.sentinelvault.audit.application.mapper;

import com.tola.sentinelvault.audit.application.command.RecordAuditEventCommand;
import com.tola.sentinelvault.audit.domain.model.AuditActions;
import com.tola.sentinelvault.audit.domain.model.AuditLog;
import com.tola.sentinelvault.identity.domain.model.UserRegisteredEvent;
import com.tola.sentinelvault.vault.domain.model.SecretCreatedEvent;
import com.tola.sentinelvault.vault.domain.model.SecretUpdatedEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuditMapper {
    public RecordAuditEventCommand fromSecretCreated(SecretCreatedEvent event) {
        return new RecordAuditEventCommand(
                UUID.randomUUID(),
                event.ownerId(),                  // actorId  — owner who created it
                AuditActions.VAULT_SECRET_CREATED,
                "Secret",
                event.secretId(),                 // resourceId — the new secret
                AuditLog.Outcome.SUCCESS,
                null,
                null,
                "Secret created",
                event.occurredOn()
        );
    }

    public RecordAuditEventCommand fromSecretUpdated(SecretUpdatedEvent event) {
        return new RecordAuditEventCommand(
                UUID.randomUUID(),
                event.ownerId(),
                AuditActions.VAULT_SECRET_UPDATED,
                "Secret",
                event.secretId(),
                AuditLog.Outcome.SUCCESS,
                null,
                null,
                "Secret updated",
                event.occurredOn()
        );
    }
}