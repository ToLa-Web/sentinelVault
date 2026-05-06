package com.tola.sentinelvault.audit.application.dto;

import com.tola.sentinelvault.audit.domain.model.AuditLog;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID actorId,
        String action,
        String resourceType,
        UUID resourceId,
        Instant occurredOn
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActorId(),
                log.getAction(),
                log.getResourceType(),
                log.getResourceId(),
                log.getOccurredOn()
        );
    }
}
