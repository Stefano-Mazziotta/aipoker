package com.poker.shared.infrastructure.websocket;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.infrastructure.events.WebSocketEventPublisher;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * WebSocket endpoint for poker game communication.
 * Handles bi-directional real-time communication with clients.
 */
@ServerEndpoint("/poker")
public class PokerWebSocketEndpoint {
    private static final Logger LOGGER = Logger.getLogger(PokerWebSocketEndpoint.class.getName());
    private static final Gson gson = new GsonBuilder().create();
    
    // These will be injected/configured
    private static ProtocolHandler protocolHandler;
    private static final WebSocketEventPublisher eventPublisher = WebSocketEventPublisher.getInstance();

    public static void setProtocolHandler(ProtocolHandler handler) {
        protocolHandler = handler;
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info(() -> String.format("WebSocket connection opened: %s", session.getId()));
        
        try {
            String msg = "Welcome to the Texas Hold'em Poker Server!";
            String timestamp = java.time.LocalDateTime.now().toString();
            WebSocketResponse<Void> response = new WebSocketResponse<>(
                EventTypeEnum.WELCOME,
                msg,
                true,
                timestamp,
                null
            );

            session.getBasicRemote().sendText(gson.toJson(response));
        } catch (java.io.IOException e) {
            LOGGER.warning(() -> String.format("Error sending welcome: %s", e.getMessage()));
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info(() -> String.format("Received message from %s: %s", session.getId(), message));
        
        try {
            if (protocolHandler == null) {
                WebSocketResponse<Void> error = WebSocketHelper.errorResponse("Server not initialized");
                session.getBasicRemote().sendText(gson.toJson(error));
                return;
            }
            
            // Process command through protocol handler (includes subscription logic)
            WebSocketResponse<?> response = protocolHandler.handle(message, session);
            
            // Send response back to requester
            session.getBasicRemote().sendText(gson.toJson(response));
            
        } catch (JsonSyntaxException | IOException e) {
            LOGGER.warning(() -> String.format("Error processing message: %s", e.getMessage()));
            try {
                WebSocketResponse<Void> error = WebSocketHelper.errorResponse(e.getMessage());
                session.getBasicRemote().sendText(gson.toJson(error));
            } catch (java.io.IOException ex) {
                LOGGER.severe(() -> String.format("Failed to send error response: %s", ex.getMessage()));
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.info(() -> String.format("WebSocket connection closed: %s", session.getId()));
        eventPublisher.cleanupSession(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.severe(() -> String.format("WebSocket error for session %s: %s", 
            session.getId(), throwable.getMessage()));
    }
}

