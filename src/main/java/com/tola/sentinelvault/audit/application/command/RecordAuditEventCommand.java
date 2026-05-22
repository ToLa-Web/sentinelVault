package com.tola.sentinelvault.audit.application.command;

import com.tola.sentinelvault.audit.domain.model.AuditLog;
import com.tola.sentinelvault.shared.domain.base.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record RecordAuditEventCommand(
        UUID eventId,
        UUID actorId,
        String action,
        String resourceType,
        UUID resourceId,     // nullable
        AuditLog.Outcome outcome,
        String clientIp,
        String userAgent,
        String detail,
        Instant occurredOn
) implements DomainEvent {
}
