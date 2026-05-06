package com.tola.sentinelvault.audit.application.command;

import java.time.Instant;
import java.util.UUID;

public record RecordAuditEventCommand(
        UUID actorId,
        String action,
        String resourceType,
        UUID resourceId,     // nullable
        Instant occurredOn
) {
}
