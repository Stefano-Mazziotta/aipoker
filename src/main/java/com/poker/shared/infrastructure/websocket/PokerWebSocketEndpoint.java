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
    private static final WebSocketEventPublisher eventPublisher = WebSocketEventPublisher.getInstance();

    public static void setProtocolHandler(ProtocolHandler handler) {
        protocolHandler = handler;
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info(() -> String.format("WebSocket connection opened: %s", session.getId()));
        
        try {
            // Send welcome message
            String welcomeText = """
                ╔═══════════════════════════════════════════════╗
                ║   TEXAS HOLD'EM POKER SERVER                 ║
                ║   Type 'HELP' for available commands         ║
                ╚═══════════════════════════════════════════════╝
                """;
            WebSocketResponse<String> welcome = WebSocketResponse.success("welcome", welcomeText);
            session.getBasicRemote().sendText(gson.toJson(welcome));
        } catch (java.io.IOException e) {
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
            if (WebSocketCommand.SUBSCRIBE_GAME.isPrefixOf(command)) {
                handleGameSubscription(command, session);
                return;
            } else if (WebSocketCommand.SUBSCRIBE_LOBBY.isPrefixOf(command)) {
                handleLobbySubscription(command, session);
                return;
            }
            
            // Process command through protocol handler
            if (protocolHandler != null) {
                WebSocketResponse<?> response = protocolHandler.handle(command);
                
                // Handle GAME_STARTED specially - broadcast to lobby and subscribe to game
                if (response.isSuccess() && "GAME_STARTED".equals(response.getType()) && response.getLobbyId() != null) {
                    handleGameStarted(response);
                } else {
                    // Normal response - just send back to requester
                    session.getBasicRemote().sendText(gson.toJson(response));
                }
                
                // Auto-subscribe to lobby after CREATE_LOBBY or JOIN_LOBBY
                if (response.isSuccess() && response.getType() != null) {
                    if (response.getType().equals("LOBBY_CREATED") || response.getType().equals("LOBBY_JOINED")) {
                        autoSubscribeToLobby(command, response, session);
                    }
                }
            } else {
                WebSocketResponse<Void> error = WebSocketResponse.error("Server not initialized");
                session.getBasicRemote().sendText(gson.toJson(error));
            }
            
        } catch (com.google.gson.JsonSyntaxException | java.io.IOException e) {
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

    private void handleGameSubscription(String command, Session session) {
        try {
            String[] parts = command.split(" ");
            if (parts.length < 3) {
                WebSocketResponse<Void> error = WebSocketResponse.error("Usage: SUBSCRIBE_GAME <gameId> <playerId>");
                session.getBasicRemote().sendText(gson.toJson(error));
                return;
            }
            
            String gameId = parts[1];
            String playerId = parts[2];
            
            eventPublisher.subscribe(gameId, session, playerId);
            WebSocketResponse<Void> success = WebSocketResponse.successMessage("Subscribed to game " + gameId);
            session.getBasicRemote().sendText(gson.toJson(success));
                
        } catch (java.io.IOException e) {
            LOGGER.warning(() -> String.format("Game subscription error: %s", e.getMessage()));
        }
    }

    private void handleLobbySubscription(String command, Session session) {
        try {
            String[] parts = command.split(" ");
            if (parts.length < 3) {
                WebSocketResponse<Void> error = WebSocketResponse.error("Usage: SUBSCRIBE_LOBBY <lobbyId> <playerId>");
                session.getBasicRemote().sendText(gson.toJson(error));
                return;
            }
            
            String lobbyId = parts[1];
            String playerId = parts[2];
            
            eventPublisher.subscribe(lobbyId, session, playerId);
            WebSocketResponse<Void> success = WebSocketResponse.successMessage("Subscribed to lobby " + lobbyId);
            session.getBasicRemote().sendText(gson.toJson(success));
                
        } catch (java.io.IOException e) {
            LOGGER.warning(() -> String.format("Lobby subscription error: %s", e.getMessage()));
        }
    }

    /**
     * Automatically subscribes player to lobby after CREATE_LOBBY or JOIN_LOBBY.
     * Extracts lobbyId and playerId from the command and response data.
     */
    private void autoSubscribeToLobby(String command, WebSocketResponse<?> response, Session session) {
        try {
            // Extract player ID from command
            // CREATE_LOBBY <name> <maxPlayers> <adminPlayerId>
            // JOIN_LOBBY <lobbyId> <playerId>
            String[] parts = command.trim().split("\\s+");
            String playerId;
            
            if (WebSocketCommand.CREATE_LOBBY.isPrefixOf(command)) {
                if (parts.length < 4) return;
                playerId = parts[3]; // adminPlayerId
            } else if (WebSocketCommand.JOIN_LOBBY.isPrefixOf(command)) {
                if (parts.length < 3) return;
                playerId = parts[2]; // playerId
            } else {
                return;
            }
            
            // Extract lobby data from response
            Object data = response.getData();
            if (data == null) {
                return;
            }
            
            // Convert response data to JSON to extract lobbyId
            JsonObject lobbyData = gson.toJsonTree(data).getAsJsonObject();
            
            if (!lobbyData.has("lobbyId")) {
                LOGGER.warning("Cannot auto-subscribe: missing lobbyId in response");
                return;
            }
            
            String lobbyId = lobbyData.get("lobbyId").getAsString();
            
            // Subscribe player to lobby
            eventPublisher.subscribe(lobbyId, session, playerId);
            LOGGER.info(() -> String.format("Auto-subscribed player %s to lobby %s", playerId, lobbyId));
            
        } catch (Exception e) {
            LOGGER.warning(() -> String.format("Auto-subscription error: %s", e.getMessage()));
        }
    }

    /**
     * Handles GAME_STARTED by broadcasting to lobby and subscribing players to game scope.
     * Extracts lobbyId from response and gameId/playerIds from response data.
     */
    private void handleGameStarted(WebSocketResponse<?> response) {
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
    private com.poker.shared.domain.events.DomainEvent createDomainEventFromJson(String eventJson) {
        JsonObject json = gson.fromJson(eventJson, JsonObject.class);
        String eventType = json.get("eventType").getAsString();
        String eventId = json.get("eventId").getAsString();
        String timestamp = json.get("timestamp").getAsString();
        
        // Create a simple domain event wrapper
        return new com.poker.shared.domain.events.DomainEvent() {
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

