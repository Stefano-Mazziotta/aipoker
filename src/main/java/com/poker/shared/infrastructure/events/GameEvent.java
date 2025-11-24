package com.poker.shared.infrastructure.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all game events that need to be broadcast to clients.
 */
public abstract class GameEvent {
    private final String eventId;
    private final String gameId;
    private final String eventType;
    private final Instant timestamp;

    protected GameEvent(String gameId, String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.gameId = gameId;
        this.eventType = eventType;
        this.timestamp = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public String getGameId() {
        return gameId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Convert event to JSON for transmission over WebSocket.
     */
    public abstract String toJson();
}
