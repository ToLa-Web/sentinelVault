package com.tola.sentinelvault.shared.domain.base;

import java.util.List;

/**
 * Domain port for publishing domain events.
 *
 * The domain layer depends only on this interface.
 * The infrastructure layer provides the adapter
 * (e.g. Spring ApplicationEventPublisher).
 *
 * Typically called by application-layer use cases
 * after persisting the aggregate:
 *
 *   aggregate.doSomething();
 *   repository.save(aggregate);
 *   publisher.publishAll(aggregate.pullEvents());
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);

    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
