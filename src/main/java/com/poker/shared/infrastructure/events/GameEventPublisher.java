package com.poker.shared.infrastructure.events;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import jakarta.websocket.Session;

/**
 * Central event publisher that broadcasts game events to WebSocket clients.
 * Manages subscriptions by game ID and lobby ID.
 */
public class GameEventPublisher {
    private static final Logger LOGGER = Logger.getLogger(GameEventPublisher.class.getName());
    private static GameEventPublisher instance;
    
    // Map of gameId -> Set of subscribed WebSocket sessions
    private final Map<String, Set<Session>> gameSubscriptions;
    
    // Map of lobbyId -> Set of subscribed WebSocket sessions
    private final Map<String, Set<Session>> lobbySubscriptions;
    
    // Map of session -> playerId for identifying clients
    private final Map<Session, String> sessionToPlayer;

    private GameEventPublisher() {
        this.gameSubscriptions = new ConcurrentHashMap<>();
        this.lobbySubscriptions = new ConcurrentHashMap<>();
        this.sessionToPlayer = new ConcurrentHashMap<>();
    }

    public static synchronized GameEventPublisher getInstance() {
        if (instance == null) {
            instance = new GameEventPublisher();
        }
        return instance;
    }

    /**
     * Subscribe a WebSocket session to a specific game.
     */
    public void subscribeToGame(String gameId, Session session, String playerId) {
        gameSubscriptions.computeIfAbsent(gameId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionToPlayer.put(session, playerId);
        LOGGER.info(() -> String.format("Player %s subscribed to game %s", playerId, gameId));
    }

    /**
     * Unsubscribe a WebSocket session from a game.
     */
    public void unsubscribeFromGame(String gameId, Session session) {
        Set<Session> sessions = gameSubscriptions.get(gameId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                gameSubscriptions.remove(gameId);
            }
        }
        sessionToPlayer.remove(session);
    }

    /**
     * Subscribe a WebSocket session to a specific lobby.
     */
    public void subscribeToLobby(String lobbyId, Session session, String playerId) {
        lobbySubscriptions.computeIfAbsent(lobbyId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionToPlayer.put(session, playerId);
        LOGGER.info(() -> String.format("Player %s subscribed to lobby %s", playerId, lobbyId));
    }

    /**
     * Unsubscribe from lobby.
     */
    public void unsubscribeFromLobby(String lobbyId, Session session) {
        Set<Session> sessions = lobbySubscriptions.get(lobbyId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                lobbySubscriptions.remove(lobbyId);
            }
        }
    }

    /**
     * Publish an event to all clients subscribed to a game.
     */
    public void publishToGame(GameEvent event) {
        String gameId = event.getGameId();
        Set<Session> sessions = gameSubscriptions.get(gameId);
        
        if (sessions == null || sessions.isEmpty()) {
            LOGGER.fine(() -> String.format("No subscribers for game %s", gameId));
            return;
        }

        String json = event.toJson();
        int successCount = 0;
        
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(json);
                    successCount++;
                } catch (Exception e) {
                    LOGGER.warning(() -> String.format("Failed to send event to session %s: %s", 
                        session.getId(), e.getMessage()));
                }
            }
        }
        
        final int sent = successCount;
        LOGGER.info(() -> String.format("Published %s event to %d/%d subscribers of game %s", 
            event.getEventType(), sent, sessions.size(), gameId));
    }

    /**
     * Publish an event to all clients subscribed to a lobby.
     */
    public void publishToLobby(String lobbyId, String eventJson) {
        Set<Session> sessions = lobbySubscriptions.get(lobbyId);
        
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(eventJson);
                } catch (Exception e) {
                    LOGGER.warning(() -> String.format("Failed to send lobby event: %s", e.getMessage()));
                }
            }
        }
    }

    /**
     * Remove all subscriptions for a closed session.
     */
    public void cleanupSession(Session session) {
        sessionToPlayer.remove(session);
        
        // Remove from all game subscriptions
        gameSubscriptions.values().forEach(sessions -> sessions.remove(session));
        
        // Remove from all lobby subscriptions
        lobbySubscriptions.values().forEach(sessions -> sessions.remove(session));
        
        LOGGER.info(() -> String.format("Cleaned up session %s", session.getId()));
    }

    /**
     * Get player ID associated with a session.
     */
    public String getPlayerId(Session session) {
        return sessionToPlayer.get(session);
    }
}
