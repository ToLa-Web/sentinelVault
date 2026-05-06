package com.tola.sentinelvault.audit.domain.port;

import com.tola.sentinelvault.shared.domain.base.DomainEvent;

public interface AuditEventPublisher {

    void publish(DomainEvent event);
}
