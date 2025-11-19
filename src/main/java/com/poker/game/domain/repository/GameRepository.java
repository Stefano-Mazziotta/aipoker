package com.poker.game.domain.repository;

import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.model.GameState;
import com.poker.player.domain.model.PlayerId;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for Game aggregate.
 * Defines persistence operations without implementation details.
 */
public interface GameRepository {
    
    /**
     * Save a new game or update existing one.
     */
    void save(Game game);
    
    /**
     * Find game by unique identifier.
     */
    Optional<Game> findById(GameId id);
    
    /**
     * Get all games with specific state.
     */
    List<Game> findByState(GameState state);
    
    /**
     * Get all active games (not FINISHED).
     */
    List<Game> findActiveGames();
    
    /**
     * Find games that a player is participating in.
     */
    List<Game> findByPlayer(PlayerId playerId);
    
    /**
     * Check if game exists.
     */
    boolean exists(GameId id);
    
    /**
     * Delete a game.
     */
    void delete(GameId id);
    
    /**
     * Get all games (for history/statistics).
     */
    List<Game> findAll();
}
