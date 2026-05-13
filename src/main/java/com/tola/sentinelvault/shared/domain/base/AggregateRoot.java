package com.tola.sentinelvault.shared.domain.base;

import lombok.Getter;
import java.util.*;

/**
 * Base class for all Aggregate Roots.
 * Responsibilities:
 *  1. Carries the aggregate's unique identity (UUID).
 *  2. Collects domain events raised during a business operation.
 *  3. Exposes pullEvents() so the application layer can
 *     drain and publish events after the aggregate is persisted.
 */
public abstract class AggregateRoot {
    private final UUID id;
    private final List<DomainEvent> domainEvents  = new ArrayList<>();
    public AggregateRoot(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
    /*
     * Registers a domain event to be published after persistence.
     * Called from within the aggregate's business methods.
     */
    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
