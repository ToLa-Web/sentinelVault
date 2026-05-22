package com.tola.sentinelvault.audit.application.usecase;

import com.tola.sentinelvault.audit.application.command.RecordAuditEventCommand;
import com.tola.sentinelvault.audit.domain.model.AuditLog;
import com.tola.sentinelvault.audit.domain.repository.AuditLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordAuditEventUseCase {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void execute(RecordAuditEventCommand cmd) {
        AuditLog auditLog = AuditLog.record(
                cmd.actorId(),
                cmd.action(),
                cmd.resourceType(),
                cmd.resourceId(),
                cmd.outcome(),
                cmd.clientIp(),
                cmd.userAgent(),
                cmd.detail(),
                cmd.occurredOn()
        );

        auditLogRepository.save(auditLog);
        log.info("Audit recorded: actor={} action={} resource={}/{}",
                cmd.actorId(), cmd.action(), cmd.resourceType(), cmd.resourceId());
    }
}
