package com.tola.sentinelvault.vault.domain.model;

import com.tola.sentinelvault.shared.domain.base.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record SecretUpdatedEvent(
        UUID eventId,
        Instant occurredOn,
        UUID secretId,
        UUID ownerId
) implements DomainEvent {
}
