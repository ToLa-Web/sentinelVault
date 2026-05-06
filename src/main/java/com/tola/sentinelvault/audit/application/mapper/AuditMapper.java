package com.tola.sentinelvault.audit.application.mapper;

import com.tola.sentinelvault.audit.application.command.RecordAuditEventCommand;
import com.tola.sentinelvault.identity.domain.model.UserRegisteredEvent;
import com.tola.sentinelvault.vault.domain.model.SecretCreatedEvent;
import com.tola.sentinelvault.vault.domain.model.SecretUpdatedEvent;
import org.springframework.stereotype.Component;

@Component
public class AuditMapper {
    public RecordAuditEventCommand fromUserRegistered(UserRegisteredEvent event){
        return new RecordAuditEventCommand(
                event.userId(),
                "USER_REGISTERED",
                "User",
                event.userId(),
                event.occurredOn()
        );
    }

    public RecordAuditEventCommand fromSecretCreated(SecretCreatedEvent event) {
        return new RecordAuditEventCommand(
                event.ownerId(),
                "SECRET_CREATED",
                "Secret",
                event.secretId(),
                event.occurredOn()
        );
    }

    public RecordAuditEventCommand fromSecretUpdated(SecretUpdatedEvent event) {
        return new RecordAuditEventCommand(
                event.ownerId(),
                "SECRET_UPDATED",
                "Secret",
                event.secretId(),
                event.occurredOn()
        );
    }
}
