package com.poker.shared.domain.events;

/**
 * No-op implementation of DomainEventPublisher for testing.
 * Events are published but not sent anywhere.
 */
public class NoOpEventPublisher implements DomainEventPublisher {
    
    @Override
    public void publishToScope(String scopeId, DomainEvent event) {
        // No-op for tests
    }
}
