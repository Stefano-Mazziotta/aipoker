package com.poker.shared.infrastructure.websocket;

import java.time.Instant;

import com.poker.shared.domain.enums.EventTypeEnum;

public class WebSocketHelper {

    public static WebSocketResponse<Void> errorResponse(String message) {
    
        Instant now = Instant.now();
        return new WebSocketResponse<>(
            EventTypeEnum.ERROR, 
            message, 
            false, 
            now, 
            null
        );
    }
}
