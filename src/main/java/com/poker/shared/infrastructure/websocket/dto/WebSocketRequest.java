package com.poker.shared.infrastructure.websocket.dto;

import com.google.gson.JsonObject;

/**
 * Base WebSocket request structure.
 * All client requests must follow this format.
 */
public class WebSocketRequest {
    private String command;
    private JsonObject data;

    public WebSocketRequest() {}

    public WebSocketRequest(String command, JsonObject data) {
        this.command = command;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }
}
