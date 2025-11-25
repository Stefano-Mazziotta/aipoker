package com.poker.game.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.poker.game.domain.model.Blinds;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.model.GameState;
import com.poker.game.domain.repository.GameRepository;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerId;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.shared.domain.valueobject.Card;
import com.poker.shared.infrastructure.database.DatabaseConnection;

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
                     "dealer_position, current_player_index, players_acted_this_round, " +
                     "community_card_1, community_card_2, community_card_3, " +
                     "community_card_4, community_card_5, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))";
        
        List<Card> communityCards = game.getCommunityCards();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.getId().getValue().toString());
            stmt.setString(2, game.getState().name());
            stmt.setInt(3, game.getBlinds().getSmallBlind());
            stmt.setInt(4, game.getBlinds().getBigBlind());
            stmt.setInt(5, game.getCurrentPot().getAmount());
            stmt.setInt(6, game.getDealerPosition());
            stmt.setInt(7, game.getCurrentPlayerIndex());
            
            // Convert Set to comma-separated string
            String playersActed = String.join(",", game.getPlayersActedThisRound());
            stmt.setString(8, playersActed);
            
            // Set community cards (null if not dealt yet)
            for (int i = 0; i < 5; i++) {
                if (i < communityCards.size()) {
                    stmt.setString(9 + i, communityCards.get(i).toString());
                } else {
                    stmt.setNull(9 + i, java.sql.Types.VARCHAR);
                }
            }
            
            stmt.executeUpdate();
        }
    }

    private void updateGame(Connection conn, Game game) throws SQLException {
        String sql = "UPDATE games SET state = ?, pot = ?, dealer_position = ?, " +
                     "current_player_index = ?, players_acted_this_round = ?, " +
                     "community_card_1 = ?, community_card_2 = ?, community_card_3 = ?, " +
                     "community_card_4 = ?, community_card_5 = ?, " +
                     "updated_at = datetime('now') WHERE id = ?";
        
        List<Card> communityCards = game.getCommunityCards();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.getState().name());
            stmt.setInt(2, game.getCurrentPot().getAmount());
            stmt.setInt(3, game.getDealerPosition());
            stmt.setInt(4, game.getCurrentPlayerIndex());
            
            // Convert Set to comma-separated string
            String playersActed = String.join(",", game.getPlayersActedThisRound());
            stmt.setString(5, playersActed);
            
            // Set community cards (null if not dealt yet)
            for (int i = 0; i < 5; i++) {
                if (i < communityCards.size()) {
                    stmt.setString(6 + i, communityCards.get(i).toString());
                } else {
                    stmt.setNull(6 + i, java.sql.Types.VARCHAR);
                }
            }
            
            stmt.setString(11, game.getId().getValue().toString());
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
        
        // Get player bets from current round if available
        Map<String, Integer> playerBets = new HashMap<>();
        if (game.getCurrentRound() != null) {
            playerBets = game.getCurrentRound().getAllPlayerBets();
        }
        
        // Insert current players
        String insertSql = "INSERT INTO game_players (game_id, player_id, position, chips_at_start, current_chips, is_folded, is_all_in, current_bet, hole_card_1, hole_card_2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            List<Player> players = game.getPlayers();
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                String playerId = player.getId().getValue().toString();
                int currentBet = playerBets.getOrDefault(playerId, 0);
                List<Card> holeCards = player.getHand().getCards();
                
                stmt.setString(1, game.getId().getValue().toString());
                stmt.setString(2, playerId);
                stmt.setInt(3, i);
                stmt.setInt(4, player.getChipsAmount()); // chips_at_start
                stmt.setInt(5, player.getChipsAmount()); // current_chips
                stmt.setBoolean(6, player.isFolded()); // is_folded
                stmt.setBoolean(7, player.isAllIn()); // is_all_in
                stmt.setInt(8, currentBet); // current_bet from round
                
                // Save hole cards
                if (holeCards.size() >= 1) {
                    stmt.setString(9, holeCards.get(0).toString());
                } else {
                    stmt.setNull(9, java.sql.Types.VARCHAR);
                }
                if (holeCards.size() >= 2) {
                    stmt.setString(10, holeCards.get(1).toString());
                } else {
                    stmt.setNull(10, java.sql.Types.VARCHAR);
                }
                
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
        int pot = rs.getInt("pot");
        int dealerPosition = rs.getInt("dealer_position");
        int currentPlayerIndex = rs.getInt("current_player_index");
        
        // Load players acted this round
        String playersActedStr = rs.getString("players_acted_this_round");
        Set<String> playersActed = new HashSet<>();
        if (playersActedStr != null && !playersActedStr.isEmpty()) {
            playersActed.addAll(Arrays.asList(playersActedStr.split(",")));
        }
        
        // Load community cards
        List<Card> communityCards = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String cardStr = rs.getString("community_card_" + i);
            if (cardStr != null && !cardStr.isEmpty()) {
                communityCards.add(Card.fromString(cardStr));
            }
        }
        
        Blinds blinds = new Blinds(smallBlind, bigBlind);
        
        // Load players and their bets
        List<Player> players = loadGamePlayers(conn, id);
        Map<String, Integer> playerBets = loadPlayerBets(conn, id);
        
        // Reconstruct game with pot, current bet, player bets, and community cards
        int currentBet = (state == GameState.PRE_FLOP) ? bigBlind : 0;
        Game game = Game.reconstitute(id, players, blinds, state, dealerPosition, pot, currentBet, playerBets, communityCards);
        
        // Set turn tracking state
        game.setCurrentPlayerIndex(currentPlayerIndex);
        game.setPlayersActedThisRound(playersActed);
        
        return game;
    }

    private List<Player> loadGamePlayers(Connection conn, GameId gameId) throws SQLException {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT player_id, hole_card_1, hole_card_2 FROM game_players WHERE game_id = ? ORDER BY position";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, gameId.getValue().toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                PlayerId playerId = PlayerId.from(rs.getString("player_id"));
                Optional<Player> playerOpt = playerRepository.findById(playerId);
                
                if (playerOpt.isPresent()) {
                    Player player = playerOpt.get();
                    
                    // Restore hole cards
                    String card1Str = rs.getString("hole_card_1");
                    String card2Str = rs.getString("hole_card_2");
                    
                    if (card1Str != null && !card1Str.isEmpty()) {
                        player.receiveCard(Card.fromString(card1Str));
                    }
                    if (card2Str != null && !card2Str.isEmpty()) {
                        player.receiveCard(Card.fromString(card2Str));
                    }
                    
                    players.add(player);
                }
            }
        }
        
        return players;
    }
    
    private Map<String, Integer> loadPlayerBets(Connection conn, GameId gameId) throws SQLException {
        Map<String, Integer> playerBets = new HashMap<>();
        String sql = "SELECT player_id, current_bet FROM game_players WHERE game_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, gameId.getValue().toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String playerId = rs.getString("player_id");
                int currentBet = rs.getInt("current_bet");
                if (currentBet > 0) {
                    playerBets.put(playerId, currentBet);
                }
            }
        }
        
        return playerBets;
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
