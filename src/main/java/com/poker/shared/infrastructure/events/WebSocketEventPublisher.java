package com.poker.shared.infrastructure.events;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.poker.shared.domain.events.DomainEvent;
import com.poker.shared.domain.events.DomainEventPublisher;
import com.poker.shared.infrastructure.json.GsonFactory;

import jakarta.websocket.Session;

/**
 * Infrastructure adapter that publishes domain events to WebSocket clients.
 * Implements the DomainEventPublisher port from the domain layer.
 * 
 * This is the infrastructure concern that translates domain events into
 * WebSocket messages for real-time communication.
 */
public class WebSocketEventPublisher implements DomainEventPublisher {
    private static final Logger LOGGER = Logger.getLogger(WebSocketEventPublisher.class.getName());
    private static final Gson gson = GsonFactory.getInstance();
    private static WebSocketEventPublisher instance;
    
    // Map of scopeId (gameId/lobbyId) -> Set of subscribed WebSocket sessions
    private final Map<String, Set<Session>> subscriptions;
    
    // Map of session -> playerId for identifying clients
    private final Map<Session, String> sessionToPlayer;

    private WebSocketEventPublisher() {
        this.subscriptions = new ConcurrentHashMap<>();
        this.sessionToPlayer = new ConcurrentHashMap<>();
    }

    public static synchronized WebSocketEventPublisher getInstance() {
        if (instance == null) {
            instance = new WebSocketEventPublisher();
        }
        return instance;
    }

    /**
     * Subscribe a WebSocket session to a specific scope (game or lobby).
     */
    public void subscribe(String scopeId, Session session, String playerId) {
        subscriptions.computeIfAbsent(scopeId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionToPlayer.put(session, playerId);
        LOGGER.info(() -> String.format("Player %s subscribed to scope %s", playerId, scopeId));
    }

    /**
     * Unsubscribe a WebSocket session from a scope.
     */
    public void unsubscribe(String scopeId, Session session) {
        Set<Session> sessions = subscriptions.get(scopeId);

        if (sessions == null) {
            sessionToPlayer.remove(session);
            return;
        }        

        sessions.remove(session);
        if (sessions.isEmpty()) {
            subscriptions.remove(scopeId);
        }
        sessionToPlayer.remove(session);
    }
    
    @Override
    public void unsubscribeFromScope(String scopeId, String playerId) {
        Session session = getSessionByPlayerId(playerId);
        if (session != null) {
            unsubscribe(scopeId, session);
            LOGGER.info(() -> String.format("Player %s unsubscribed from scope %s", playerId, scopeId));
        } else {
            LOGGER.warning(() -> String.format("Attempted to unsubscribe player %s from scope %s, but no active session found", playerId, scopeId));
        }
    }

    @Override
    public void publishToScope(String scopeId, DomainEvent event) {
        Set<Session> sessions = subscriptions.get(scopeId);
        
        if (sessions == null || sessions.isEmpty()) {
            LOGGER.fine(() -> String.format("No subscribers for scope %s", scopeId));
            return;
        }

        String json = gson.toJson(event);
        
        int successCount = 0;
        
        for (Session session : sessions) {

            if (!session.isOpen()) {
                continue;
            }

            try {
                session.getBasicRemote().sendText(json);
                successCount++;
            } catch (IOException e) {
                LOGGER.warning(() -> String.format("Failed to send event to session %s: %s", 
                    session.getId(), e.getMessage()));
            }
        }
        
        final int sent = successCount;
        
        LOGGER.info(() -> 
            String.format("Published %s event to %d/%d subscribers of scope %s", 
            event.eventType(), 
            sent,
            sessions.size(),
            scopeId)
        );
    }

    /**
     * Remove all subscriptions for a closed session.
     */
    public void cleanupSession(Session session) {
        sessionToPlayer.remove(session);
        subscriptions.values().forEach(sessions -> sessions.remove(session));
        LOGGER.info(() -> String.format("Cleaned up session %s", session.getId()));
    }

    /**
     * Get player ID associated with a session.
     */
    public String getPlayerId(Session session) {
        return sessionToPlayer.get(session);
    }

    /**
     * Get session for a specific player ID.
     */
    public Session getSessionByPlayerId(String playerId) {
        for (Map.Entry<Session, String> entry : sessionToPlayer.entrySet()) {
            if (entry.getValue().equals(playerId) && entry.getKey().isOpen()) {
                return entry.getKey();
            }
        }
        return null;
    }
}
