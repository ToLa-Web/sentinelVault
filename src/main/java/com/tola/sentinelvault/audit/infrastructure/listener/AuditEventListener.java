package com.tola.sentinelvault.audit.infrastructure.listener;

import com.tola.sentinelvault.audit.application.mapper.AuditMapper;
import com.tola.sentinelvault.audit.application.usecase.RecordAuditEventUseCase;
import com.tola.sentinelvault.identity.domain.model.UserRegisteredEvent;
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
    public void onUserRegistered(UserRegisteredEvent event) {
        log.debug("Audit listener received UserRegisteredEvent for userId={}", event.userId());
        recordAuditEventUseCase.execute(auditMapper.fromUserRegistered(event));
    }
}
