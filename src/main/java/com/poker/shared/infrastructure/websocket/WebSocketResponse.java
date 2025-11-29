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
    private final String lobbyId; // For broadcasting GAME_STARTED

    private WebSocketResponse(String type, String message, T data, boolean success, String lobbyId) {
        this.type = type;
        this.message = message;
        this.data = data;
        this.success = success;
        this.timestamp = System.currentTimeMillis();
        this.lobbyId = lobbyId;
    }

    public static <T> WebSocketResponse<T> success(String type, T data) {
        return new WebSocketResponse<>(type, null, data, true, null);
    }

    public static <T> WebSocketResponse<T> success(String type, String message, T data) {
        return new WebSocketResponse<>(type, message, data, true, null);
    }

    public static <T> WebSocketResponse<T> successWithLobby(String type, T data, String lobbyId) {
        return new WebSocketResponse<>(type, null, data, true, lobbyId);
    }

    public static WebSocketResponse<Void> successMessage(String message) {
        return new WebSocketResponse<>("success", message, null, true, null);
    }

    public static WebSocketResponse<Void> error(String message) {
        return new WebSocketResponse<>("error", message, null, false, null);
    }

    public static WebSocketResponse<String> info(String message) {
        return new WebSocketResponse<>("info", message, message, true, null);
    }

    // Getters
    public String getType() { return type; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public long getTimestamp() { return timestamp; }
    public boolean isSuccess() { return success; }
    public String getLobbyId() { return lobbyId; }
}
