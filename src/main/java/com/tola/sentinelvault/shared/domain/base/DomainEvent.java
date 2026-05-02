package com.tola.sentinelvault.shared.domain.base;

import java.time.Instant;
import java.util.UUID;

/**
 * Base type for all domain events.
 *
 * Every event is immutable and carries:
 *  - eventId   : globally unique identifier for this event occurrence
 *  - occurredOn: wall-clock timestamp of when the event was raised
 *
 * Implementations should be Java records, e.g.:
 *
 *   public record UserRegistered(UUID eventId, Instant occurredOn, UUID userId)
 *       implements DomainEvent {}
 *
 * The AggregateRoot registers events; DomainEventPublisher dispatches them.
 */
public interface DomainEvent {
    UUID eventId();
    Instant occurredOn();
}
