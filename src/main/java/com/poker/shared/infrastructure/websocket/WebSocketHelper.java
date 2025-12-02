package com.poker.shared.infrastructure.websocket;

import com.poker.shared.domain.enums.EventTypeEnum;

public class WebSocketHelper {

    public static WebSocketResponse<Void> errorResponse(String message) {
    
        String now = java.time.LocalDateTime.now().toString();
        return new WebSocketResponse<>(
            EventTypeEnum.ERROR, 
            message, 
            false, 
            now, 
            null
        );
    }
}
