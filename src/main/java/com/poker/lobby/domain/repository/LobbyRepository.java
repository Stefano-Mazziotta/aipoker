package com.poker.lobby.domain.repository;

import java.util.List;
import java.util.Optional;

import com.poker.lobby.domain.model.Lobby;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.player.domain.model.PlayerId;

/**
 * Repository port for Lobby aggregate.
 * Defines persistence operations without implementation details.
 * 
 * Note: Lobby now contains Player entities directly, so all repository
 * methods will load the complete lobby with players in a single query.
 */
public interface LobbyRepository {
    
    /**
     * Save a new lobby or update existing one.
     */
    void save(Lobby lobby);
    
    /**
     * Find lobby by unique identifier with all players loaded.
     * Uses efficient JOIN query to load lobby and all its players.
     */
    Optional<Lobby> findById(LobbyId id);
    
    /**
     * Get all open lobbies (not full, not started).
     */
    List<Lobby> findOpenLobbies();
    
    /**
     * Find lobbies that a player is in.
     */
    List<Lobby> findByPlayer(PlayerId playerId);
    
    /**
     * Check if lobby exists.
     */
    boolean exists(LobbyId id);
    
    /**
     * Delete a lobby.
     */
    void delete(LobbyId id);
    
    /**
     * Get all lobbies.
     */
    List<Lobby> findAll();
}
