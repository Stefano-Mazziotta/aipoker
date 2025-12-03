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
    
    /**
     * Publish an event to a specific player only.
     */
    void publishToPlayer(String playerId, DomainEvent event);
    
    /**
     * Unsubscribe a player from a specific scope (e.g., when leaving a game or lobby).
     */
    void unsubscribeFromScope(String scopeId, String playerId);
}
