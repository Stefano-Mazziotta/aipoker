package com.poker.game.infrastructure.persistence;

import com.poker.game.domain.model.*;
import com.poker.game.domain.repository.GameRepository;
import com.poker.player.domain.model.*;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.shared.domain.valueobject.*;
import com.poker.shared.infrastructure.database.DatabaseConnection;
import java.sql.*;
import java.util.*;

/**
 * SQLite implementation of GameRepository.
 * Handles game state persistence and reconstruction.
 */
public class SQLiteGameRepository implements GameRepository {
    private final DatabaseConnection dbConnection;
    private final PlayerRepository playerRepository;

    public SQLiteGameRepository(PlayerRepository playerRepository) {
        this.dbConnection = DatabaseConnection.getInstance();
        this.playerRepository = playerRepository;
    }

    @Override
    public void save(Game game) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            if (exists(game.getId())) {
                updateGame(conn, game);
            } else {
                insertGame(conn, game);
            }
            
            // Save game players relationship
            saveGamePlayers(conn, game);
            
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException("Failed to save game", e);
        } finally {
            dbConnection.close(conn);
        }
    }

    private void insertGame(Connection conn, Game game) throws SQLException {
        String sql = "INSERT INTO games (id, state, small_blind, big_blind, pot, " +
                     "dealer_position, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.getId().getValue().toString());
            stmt.setString(2, game.getState().name());
            stmt.setInt(3, game.getBlinds().getSmallBlind());
            stmt.setInt(4, game.getBlinds().getBigBlind());
            stmt.setInt(5, game.getCurrentPot().getAmount());
            stmt.setInt(6, game.getDealerPosition());
            stmt.executeUpdate();
        }
    }

    private void updateGame(Connection conn, Game game) throws SQLException {
        String sql = "UPDATE games SET state = ?, pot = ?, dealer_position = ?, " +
                     "updated_at = datetime('now') WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.getState().name());
            stmt.setInt(2, game.getCurrentPot().getAmount());
            stmt.setInt(3, game.getDealerPosition());
            stmt.setString(4, game.getId().getValue().toString());
            stmt.executeUpdate();
        }
    }

    private void saveGamePlayers(Connection conn, Game game) throws SQLException {
        // Delete existing relationships
        String deleteSql = "DELETE FROM game_players WHERE game_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setString(1, game.getId().getValue().toString());
            stmt.executeUpdate();
        }
        
        // Insert current players
        String insertSql = "INSERT INTO game_players (game_id, player_id, position) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            List<Player> players = game.getPlayers();
            for (int i = 0; i < players.size(); i++) {
                stmt.setString(1, game.getId().getValue().toString());
                stmt.setString(2, players.get(i).getId().getValue().toString());
                stmt.setInt(3, i);
                stmt.executeUpdate();
            }
        }
    }

    @Override
    public Optional<Game> findById(GameId id) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM games WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.getValue().toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return Optional.of(reconstructGame(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find game", e);
        } finally {
            dbConnection.close(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<Game> findByState(GameState state) {
        List<Game> games = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM games WHERE state = ? ORDER BY created_at DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, state.name());
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    games.add(reconstructGame(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find games by state", e);
        } finally {
            dbConnection.close(conn);
        }
        
        return games;
    }

    @Override
    public List<Game> findActiveGames() {
        List<Game> games = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM games WHERE state != 'FINISHED' ORDER BY created_at DESC";
            
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                
                while (rs.next()) {
                    games.add(reconstructGame(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find active games", e);
        } finally {
            dbConnection.close(conn);
        }
        
        return games;
    }

    @Override
    public List<Game> findByPlayer(PlayerId playerId) {
        List<Game> games = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT g.* FROM games g " +
                        "JOIN game_players gp ON g.id = gp.game_id " +
                        "WHERE gp.player_id = ? ORDER BY g.created_at DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerId.getValue().toString());
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    games.add(reconstructGame(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find games by player", e);
        } finally {
            dbConnection.close(conn);
        }
        
        return games;
    }

    @Override
    public boolean exists(GameId id) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM games WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.getValue().toString());
                ResultSet rs = stmt.executeQuery();
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check game existence", e);
        } finally {
            dbConnection.close(conn);
        }
    }

    @Override
    public void delete(GameId id) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            String sql = "DELETE FROM games WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.getValue().toString());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete game", e);
        } finally {
            dbConnection.close(conn);
        }
    }

    @Override
    public List<Game> findAll() {
        List<Game> games = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM games ORDER BY created_at DESC";
            
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                
                while (rs.next()) {
                    games.add(reconstructGame(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all games", e);
        } finally {
            dbConnection.close(conn);
        }
        
        return games;
    }

    private Game reconstructGame(Connection conn, ResultSet rs) throws SQLException {
        GameId id = GameId.from(rs.getString("id"));
        GameState state = GameState.valueOf(rs.getString("state"));
        int smallBlind = rs.getInt("small_blind");
        int bigBlind = rs.getInt("big_blind");
        int dealerPosition = rs.getInt("dealer_position");
        
        Blinds blinds = new Blinds(smallBlind, bigBlind);
        
        // Load players
        List<Player> players = loadGamePlayers(conn, id);
        
        // Reconstruct game
        return Game.reconstitute(id, players, blinds, state, dealerPosition);
    }

    private List<Player> loadGamePlayers(Connection conn, GameId gameId) throws SQLException {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT player_id FROM game_players WHERE game_id = ? ORDER BY position";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, gameId.getValue().toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                PlayerId playerId = PlayerId.from(rs.getString("player_id"));
                playerRepository.findById(playerId).ifPresent(players::add);
            }
        }
        
        return players;
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                System.err.println("Failed to rollback: " + e.getMessage());
            }
        }
    }
}
