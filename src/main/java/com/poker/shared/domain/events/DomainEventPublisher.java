package com.poker.shared.domain.events;

/**
 * Port for publishing domain events.
 * This is a domain-level abstraction that infrastructure will implement.
 */
public interface DomainEventPublisher {    
    /**
     * Publish an event to specific scope (e.g., a particular game or lobby).
     */
    void publishToScope(String scopeId, DomainEvent event);
}
