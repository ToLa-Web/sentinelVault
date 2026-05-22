package com.tola.sentinelvault.audit.infrastructure.event;

import com.tola.sentinelvault.audit.domain.port.AuditEventPublisher;
import com.tola.sentinelvault.shared.domain.base.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAuditEventPublisher implements AuditEventPublisher {

    private final ApplicationEventPublisher springPublisher;
    @Override
    public void publish(DomainEvent event) {
        springPublisher.publishEvent(event);
    }
}