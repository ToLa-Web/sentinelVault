package com.tola.sentinelvault.audit.infrastructure.listener;

import com.tola.sentinelvault.audit.application.command.RecordAuditEventCommand;
import com.tola.sentinelvault.audit.application.mapper.AuditMapper;
import com.tola.sentinelvault.audit.application.usecase.RecordAuditEventUseCase;
import com.tola.sentinelvault.identity.domain.model.UserRegisteredEvent;
import com.tola.sentinelvault.vault.domain.model.SecretCreatedEvent;
import com.tola.sentinelvault.vault.domain.model.SecretUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final RecordAuditEventUseCase recordAuditEventUseCase;
    private final AuditMapper auditMapper;

    @Async
    @EventListener
    public void onAuthAuditEvent(RecordAuditEventCommand command) {
        record(command);
    }

    @Async
    @EventListener
    public void onSecretCreated(SecretCreatedEvent event) {
        record(auditMapper.fromSecretCreated(event));
    }

    @Async
    @EventListener
    public void onSecretUpdated(SecretUpdatedEvent event) {
        record(auditMapper.fromSecretUpdated(event));
    }

    private void record(RecordAuditEventCommand command) {
        try {
            recordAuditEventUseCase.execute(command);
        } catch (Exception e) {
            log.error("Failed to record audit event action={} outcome={}",
                    command.action(), command.outcome(), e);
        }
    }
}
