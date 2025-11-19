package com.poker.lobby.domain.repository;

import com.poker.lobby.domain.model.Lobby;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.player.domain.model.PlayerId;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for Lobby aggregate.
 * Defines persistence operations without implementation details.
 */
public interface LobbyRepository {
    
    /**
     * Save a new lobby or update existing one.
     */
    void save(Lobby lobby);
    
    /**
     * Find lobby by unique identifier.
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
