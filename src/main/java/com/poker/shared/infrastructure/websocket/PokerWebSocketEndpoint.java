package com.poker.shared.infrastructure.websocket;

import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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
    private static MessageFormatter messageFormatter;
    private static WebSocketEventPublisher eventPublisher = WebSocketEventPublisher.getInstance();

    public static void setProtocolHandler(ProtocolHandler handler) {
        protocolHandler = handler;
    }

    public static void setMessageFormatter(MessageFormatter formatter) {
        messageFormatter = formatter;
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info(() -> String.format("WebSocket connection opened: %s", session.getId()));
        
        try {
            // Send welcome message
            String welcome = messageFormatter != null ? 
                messageFormatter.formatWelcome() : 
                "Welcome to Texas Hold'em Poker Server";
            session.getBasicRemote().sendText(createJsonResponse("welcome", welcome));
        } catch (Exception e) {
            LOGGER.warning(() -> String.format("Error sending welcome: %s", e.getMessage()));
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info(() -> String.format("Received message from %s: %s", session.getId(), message));
        
        try {
            // Parse JSON message
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String command = json.has("command") ? json.get("command").getAsString() : message;
            
            // Handle special subscription commands
            if (command.startsWith("SUBSCRIBE_GAME ")) {
                handleGameSubscription(command, session);
                return;
            } else if (command.startsWith("SUBSCRIBE_LOBBY ")) {
                handleLobbySubscription(command, session);
                return;
            }
            
            // Process command through protocol handler
            if (protocolHandler != null) {
                String response = protocolHandler.handle(command);
                session.getBasicRemote().sendText(createJsonResponse("response", response));
            } else {
                session.getBasicRemote().sendText(createJsonResponse("error", "Server not initialized"));
            }
            
        } catch (Exception e) {
            LOGGER.warning(() -> String.format("Error processing message: %s", e.getMessage()));
            try {
                session.getBasicRemote().sendText(createJsonResponse("error", e.getMessage()));
            } catch (Exception ex) {
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

    private void handleGameSubscription(String command, Session session) {
        try {
            String[] parts = command.split(" ");
            if (parts.length < 3) {
                session.getBasicRemote().sendText(createJsonResponse("error", 
                    "Usage: SUBSCRIBE_GAME <gameId> <playerId>"));
                return;
            }
            
            String gameId = parts[1];
            String playerId = parts[2];
            
            eventPublisher.subscribe(gameId, session, playerId);
            session.getBasicRemote().sendText(createJsonResponse("success", 
                "Subscribed to game " + gameId));
                
        } catch (Exception e) {
            LOGGER.warning(() -> String.format("Game subscription error: %s", e.getMessage()));
        }
    }

    private void handleLobbySubscription(String command, Session session) {
        try {
            String[] parts = command.split(" ");
            if (parts.length < 3) {
                session.getBasicRemote().sendText(createJsonResponse("error", 
                    "Usage: SUBSCRIBE_LOBBY <lobbyId> <playerId>"));
                return;
            }
            
            String lobbyId = parts[1];
            String playerId = parts[2];
            
            eventPublisher.subscribe(lobbyId, session, playerId);
            session.getBasicRemote().sendText(createJsonResponse("success", 
                "Subscribed to lobby " + lobbyId));
                
        } catch (Exception e) {
            LOGGER.warning(() -> String.format("Lobby subscription error: %s", e.getMessage()));
        }
    }

    private String createJsonResponse(String type, String content) {
        JsonObject response = new JsonObject();
        response.addProperty("type", type);
        response.addProperty("content", content);
        response.addProperty("timestamp", System.currentTimeMillis());
        return gson.toJson(response);
    }
}
