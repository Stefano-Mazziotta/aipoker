package com.poker.player.domain.repository;

import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerId;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for Player aggregate.
 * Defines persistence operations without implementation details.
 */
public interface PlayerRepository {
    
    /**
     * Save a new player or update existing one.
     */
    void save(Player player);
    
    /**
     * Find player by unique identifier.
     */
    Optional<Player> findById(PlayerId id);
    
    /**
     * Find player by name.
     */
    Optional<Player> findByName(String name);
    
    /**
     * Get all registered players.
     */
    List<Player> findAll();
    
    /**
     * Check if player exists.
     */
    boolean exists(PlayerId id);
    
    /**
     * Delete a player.
     */
    void delete(PlayerId id);
    
    /**
     * Get players sorted by chips (leaderboard).
     */
    List<Player> findTopByChips(int limit);
}
