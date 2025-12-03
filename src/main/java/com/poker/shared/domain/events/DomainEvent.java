package com.poker.shared.domain.events;

import java.time.Instant;
import java.util.UUID;

import com.poker.shared.domain.enums.EventTypeEnum;

/**
 * Base class for all domain events.
 * Domain events represent something that happened in the domain that domain experts care about.
 */
public abstract class DomainEvent {
    
    private final String eventId;
    private final Instant timestamp;
    private final EventTypeEnum eventType;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.eventType = eventType();
    }

    public String eventId() {
        return eventId;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public EventTypeEnum eventType() {
        return eventType;
    }

    public abstract Object getData();
}
