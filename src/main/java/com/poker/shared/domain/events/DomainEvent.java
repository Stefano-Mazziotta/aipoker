package com.poker.shared.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events.
 * Domain events represent something that happened in the domain that domain experts care about.
 */
public abstract class DomainEvent {
    private final String eventId;
    private final Instant timestamp;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }

    public String eventId() {
        return eventId;
    }

    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Returns the type of this event for routing and handling purposes.
     */
    public abstract String eventType();
}
