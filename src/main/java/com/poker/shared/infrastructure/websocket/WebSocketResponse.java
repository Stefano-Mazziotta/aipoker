package com.poker.shared.infrastructure.websocket;

import java.time.LocalDateTime;

import com.poker.shared.domain.enums.EventTypeEnum;

/**
 * Generic WebSocket response wrapper.
 * All responses from the server will be wrapped in this structure.
 */
public class WebSocketResponse<T> {
    private final EventTypeEnum eventType;
    private final String message;
    private final boolean success;
    private final LocalDateTime ocurredOn;
    private final T data;

    public WebSocketResponse(EventTypeEnum eventType, String message,boolean success, LocalDateTime ocurredOn, T data) {
        this.eventType = eventType;
        this.message = message;
        this.success = success;
        this.ocurredOn = ocurredOn;
        this.data = data;
    }

    // Getters
    public EventTypeEnum getEventType() { return eventType; }
    public String getMessage() { return message; }
    public boolean isSuccess() { return success; }
    public LocalDateTime getOcurredOn() { return ocurredOn; }
    public T getData() { return data; }
}
