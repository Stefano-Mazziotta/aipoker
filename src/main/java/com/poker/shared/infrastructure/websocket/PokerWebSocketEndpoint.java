package com.poker.shared.infrastructure.websocket;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.poker.shared.domain.events.DomainEvent;
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
            // Send simple welcome message
            WebSocketResponse<String> welcome = WebSocketResponse.success(
                "WELCOME", 
                "Connected to Texas Hold'em Poker Server!"
            );
            session.getBasicRemote().sendText(gson.toJson(welcome));
        } catch (java.io.IOException e) {
            LOGGER.warning(() -> String.format("Error sending welcome: %s", e.getMessage()));
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info(() -> String.format("Received message from %s: %s", session.getId(), message));
        
        try {
            if (protocolHandler == null) {
                WebSocketResponse<Void> error = WebSocketResponse.error("Server not initialized");
                session.getBasicRemote().sendText(gson.toJson(error));
                return;
            }
            
            // Process command through protocol handler
            WebSocketResponse<?> response = protocolHandler.handle(message);
            
            // Handle special broadcasting and subscription cases
            handleResponseWithSideEffects(message, response, session);
            
        } catch (JsonSyntaxException | IOException e) {
            LOGGER.warning(() -> String.format("Error processing message: %s", e.getMessage()));
            try {
                WebSocketResponse<Void> error = WebSocketResponse.error(e.getMessage());
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

    /**
     * Handles response side effects: broadcasting events and managing subscriptions.
     * This is infrastructure logic that coordinates WebSocket sessions.
     */
    private void handleResponseWithSideEffects(String message, WebSocketResponse<?> response, Session session) 
            throws java.io.IOException {
        
        if (!response.isSuccess()) {
            // For errors, just send back to requester
            session.getBasicRemote().sendText(gson.toJson(response));
            return;
        }
        
        String responseType = response.getType();
        if (responseType == null) {
            session.getBasicRemote().sendText(gson.toJson(response));
            return;
        }
        
        switch (responseType) {
            case "GAME_STARTED" -> {
                // Broadcast to lobby, don't send to requester
                if (response.getLobbyId() != null) {
                    broadcastGameStarted(response);
                }
            }
            case "LOBBY_CREATED", "LOBBY_JOINED" -> {
                // Subscribe player to lobby for future events
                subscribePlayerToLobby(message, response, session);
                // Send response back to requester
                session.getBasicRemote().sendText(gson.toJson(response));
            }
            default -> {
                // Normal response
                session.getBasicRemote().sendText(gson.toJson(response));
            }
        }
    }

    /**
     * Infrastructure logic: Subscribe player's WebSocket session to lobby events.
     * Extracts player and lobby IDs from the command/response.
     */
    private void subscribePlayerToLobby(String message, WebSocketResponse<?> response, Session session) {
        try {
            // Parse message to extract playerId
            JsonObject json = gson.fromJson(message, JsonObject.class);
            if (!json.has("data")) return;
            
            JsonObject data = json.getAsJsonObject("data");
            String playerId = data.has("playerId") ? data.get("playerId").getAsString() : null;
            if (playerId == null) {
                LOGGER.warning("Cannot subscribe: missing playerId in command");
                return;
            }
            
            // Extract lobbyId from response
            Object responseData = response.getData();
            if (responseData == null) return;
            
            JsonObject lobbyData = gson.toJsonTree(responseData).getAsJsonObject();
            if (!lobbyData.has("lobbyId")) {
                LOGGER.warning("Cannot subscribe: missing lobbyId in response");
                return;
            }
            
            String lobbyId = lobbyData.get("lobbyId").getAsString();
            
            // Subscribe session to lobby scope
            eventPublisher.subscribe(lobbyId, session, playerId);
            LOGGER.info(() -> String.format("Subscribed player %s to lobby %s", playerId, lobbyId));
            
        } catch (JsonSyntaxException exception) {
            LOGGER.warning(() -> String.format("Subscription error: %s", exception.getMessage()));
        }
    }

    

    /**
     * Infrastructure logic: Broadcast game started event to all lobby participants.
     * Handles the special case where START_GAME response goes to lobby, not requester.
     */
    private void broadcastGameStarted(WebSocketResponse<?> response) {
        try {
            String lobbyId = response.getLobbyId();
            if (lobbyId == null) {
                LOGGER.warning("Cannot handle game started: missing lobbyId");
                return;
            }
            
            // Extract game data from response
            Object data = response.getData();
            if (data == null) return;
            
            JsonObject gameData = gson.toJsonTree(data).getAsJsonObject();
            if (!gameData.has("gameId")) return;
            
            String gameId = gameData.get("gameId").getAsString();
            
            // Create GAME_STARTED event
            JsonObject eventData = new JsonObject();
            eventData.addProperty("gameId", gameId);
            
            JsonObject domainEvent = new JsonObject();
            domainEvent.addProperty("eventType", "GAME_STARTED");
            domainEvent.addProperty("eventId", java.util.UUID.randomUUID().toString());
            domainEvent.addProperty("timestamp", java.time.Instant.now().toString());
            domainEvent.add("data", eventData);
            
            String eventJson = gson.toJson(domainEvent);
            
            // Publish to lobby scope (all players in lobby will receive it)
            eventPublisher.publishToScope(lobbyId, createDomainEventFromJson(eventJson));
            
            // Subscribe all players in lobby to game scope
            // Get all sessions subscribed to lobby and subscribe them to game
            LOGGER.info(() -> String.format("Game %s started, broadcasted to lobby %s",
                gameId, lobbyId));
            
        } catch (Exception e) {
            LOGGER.warning(() -> String.format("Handle game started error: %s", e.getMessage()));
        }
    }
    
    /**
     * Helper to create a domain event for publishing
     */
    private DomainEvent createDomainEventFromJson(String eventJson) {
        JsonObject json = gson.fromJson(eventJson, JsonObject.class);
        String eventType = json.get("eventType").getAsString();
        String eventId = json.get("eventId").getAsString();
        String timestamp = json.get("timestamp").getAsString();
        
        // Create a simple domain event wrapper
        return new DomainEvent() {
            @Override
            public String eventId() { return eventId; }
            
            @Override
            public String eventType() { return eventType; }
            
            @Override
            public java.time.Instant occurredOn() { 
                return java.time.Instant.parse(timestamp);
            }
        };
    }
}

