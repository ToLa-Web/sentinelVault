package com.tola.sentinelvault.identity.config;

import com.tola.sentinelvault.shared.domain.base.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the DomainEventPublisher port to Spring's ApplicationEventPublisher.
 * Domain events flow: AggregateRoot → use case → publisher → Spring → @EventListener
 */
@Configuration
public class IdentityConfig {

    @Bean
    public DomainEventPublisher domainEventPublisher(ApplicationEventPublisher springPublisher) {
        return springPublisher::publishEvent;
    }
}
