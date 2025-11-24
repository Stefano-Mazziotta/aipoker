package com.poker.shared.infrastructure.events;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.poker.shared.domain.events.DomainEvent;
import com.poker.shared.domain.events.DomainEventPublisher;

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
    private static final Gson gson = new GsonBuilder().create();
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
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                subscriptions.remove(scopeId);
            }
        }
        sessionToPlayer.remove(session);
    }

    @Override
    public void publish(DomainEvent event) {
        // Global publish - not implemented yet, would need a different subscription mechanism
        LOGGER.warning("Global event publishing not implemented");
    }

    @Override
    public void publishToScope(String scopeId, DomainEvent event) {
        Set<Session> sessions = subscriptions.get(scopeId);
        
        if (sessions == null || sessions.isEmpty()) {
            LOGGER.fine(() -> String.format("No subscribers for scope %s", scopeId));
            return;
        }

        String json = toJson(event);
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
        LOGGER.info(() -> String.format("Published %s event to %d/%d subscribers of scope %s", 
            event.eventType(), sent, sessions.size(), scopeId));
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
     * Convert domain event to JSON for WebSocket transmission.
     */
    private String toJson(DomainEvent event) {
        JsonObject json = new JsonObject();
        json.addProperty("eventId", event.eventId());
        json.addProperty("eventType", event.eventType());
        json.addProperty("timestamp", event.occurredOn().toString());
        
        // Add event-specific data
        String eventData = gson.toJson(event);
        JsonObject data = gson.fromJson(eventData, JsonObject.class);
        json.add("data", data);
        
        return gson.toJson(json);
    }
}
