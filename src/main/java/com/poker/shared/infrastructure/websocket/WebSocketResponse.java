package com.poker.shared.infrastructure.websocket;

import java.time.Instant;

import com.poker.shared.domain.enums.EventTypeEnum;

/**
 * Generic WebSocket response wrapper.
 * All responses from the server will be wrapped in this structure.
 */
public class WebSocketResponse<T> {
    private final EventTypeEnum eventType;
    private final String message;
    private final boolean success;
    private final Instant timestamp;
    private final T data;

    public WebSocketResponse(EventTypeEnum eventType, String message,boolean success, Instant timestamp, T data) {
        this.eventType = eventType;
        this.message = message;
        this.success = success;
        this.timestamp = timestamp;
        this.data = data;
    }

    // Getters
    public EventTypeEnum getEventType() { return eventType; }
    public String getMessage() { return message; }
    public boolean isSuccess() { return success; }
    public Instant getTimestamp() { return timestamp; }
    public T getData() { return data; }
}
