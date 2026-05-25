package com.tola.sentinelvault.identity.domain.model;

import com.tola.sentinelvault.shared.domain.base.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record PasswordChangedEvent(
        UUID eventId,
        Instant occurredOn,
        UUID userId,
        String oldPasswordHash
) implements DomainEvent {
}
