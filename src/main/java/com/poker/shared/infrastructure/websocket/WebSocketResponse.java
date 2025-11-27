package com.poker.shared.infrastructure.websocket;

/**
 * Generic WebSocket response wrapper.
 * All responses from the server will be wrapped in this structure.
 */
public class WebSocketResponse<T> {
    private final String type;
    private final String message;
    private final T data;
    private final long timestamp;
    private final boolean success;

    private WebSocketResponse(String type, String message, T data, boolean success) {
        this.type = type;
        this.message = message;
        this.data = data;
        this.success = success;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> WebSocketResponse<T> success(String type, T data) {
        return new WebSocketResponse<>(type, null, data, true);
    }

    public static <T> WebSocketResponse<T> success(String type, String message, T data) {
        return new WebSocketResponse<>(type, message, data, true);
    }

    public static WebSocketResponse<Void> successMessage(String message) {
        return new WebSocketResponse<>("success", message, null, true);
    }

    public static WebSocketResponse<Void> error(String message) {
        return new WebSocketResponse<>("error", message, null, false);
    }

    public static WebSocketResponse<String> info(String message) {
        return new WebSocketResponse<>("info", message, message, true);
    }

    // Getters
    public String getType() { return type; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public long getTimestamp() { return timestamp; }
    public boolean isSuccess() { return success; }
}
