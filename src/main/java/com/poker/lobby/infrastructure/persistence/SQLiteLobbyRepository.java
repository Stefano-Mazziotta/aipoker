package com.poker.lobby.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.poker.lobby.domain.model.Lobby;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerId;
import com.poker.shared.infrastructure.database.DatabaseConnection;

/**
 * SQLite implementation of LobbyRepository.
 */
public class SQLiteLobbyRepository implements LobbyRepository {

    private final DatabaseConnection dbConnection;

    public SQLiteLobbyRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public void save(Lobby lobby) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            if (exists(lobby.getId())) {
                updateLobby(conn, lobby);
            } else {
                insertLobby(conn, lobby);
            }

            // Save lobby players
            saveLobbyPlayers(conn, lobby);

            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException("Failed to save lobby", e);
        } finally {
            dbConnection.close(conn);
        }
    }

    private void insertLobby(Connection conn, Lobby lobby) throws SQLException {
        String sql = "INSERT INTO lobbies (id, name, max_players, buy_in, small_blind, big_blind, status, started, admin_player_id, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lobby.getId().getValue());
            stmt.setString(2, lobby.getName());
            stmt.setInt(3, lobby.getMaxPlayers());
            // Default values for fields not yet in domain model
            stmt.setInt(4, 1000);  // Default buy_in
            stmt.setInt(5, 10);    // Default small_blind
            stmt.setInt(6, 20);    // Default big_blind
            stmt.setString(7, lobby.isStarted() ? "STARTED" : "OPEN");  // status
            stmt.setBoolean(8, lobby.isStarted());
            stmt.setString(9, lobby.getAdminPlayerId().getValue().toString());
            stmt.executeUpdate();
        }
    }

    private void updateLobby(Connection conn, Lobby lobby) throws SQLException {
        String sql = "UPDATE lobbies SET name = ?, max_players = ?, started = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lobby.getName());
            stmt.setInt(2, lobby.getMaxPlayers());
            stmt.setBoolean(3, lobby.isStarted());
            stmt.setString(4, lobby.getId().getValue());
            stmt.executeUpdate();
        }
    }

    private void saveLobbyPlayers(Connection conn, Lobby lobby) throws SQLException {
        // Delete existing players
        String deleteSql = "DELETE FROM lobby_players WHERE lobby_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setString(1, lobby.getId().getValue());
            stmt.executeUpdate();
        }

        // Insert current players
        String insertSql = "INSERT INTO lobby_players (lobby_id, player_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            for (Player player : lobby.getPlayers()) {
                stmt.setString(1, lobby.getId().getValue());
                stmt.setString(2, player.getId().getValue().toString());
                stmt.executeUpdate();
            }
        }
    }

    @Override
    public Optional<Lobby> findById(LobbyId id) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            
            // Single query with JOIN to fetch lobby and all players at once
            // This avoids the N+1 query problem
            String sql = """
                SELECT 
                    l.id as lobby_id,
                    l.name as lobby_name,
                    l.max_players,
                    l.started,
                    l.admin_player_id,
                    p.id as player_id,
                    p.name as player_name,
                    p.chips as player_chips
                FROM lobbies l
                LEFT JOIN lobby_players lp ON l.id = lp.lobby_id
                LEFT JOIN players p ON lp.player_id = p.id
                WHERE l.id = ?
                ORDER BY lp.rowid
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.getValue());
                ResultSet rs = stmt.executeQuery();

                return reconstructLobbyWithPlayers(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find lobby with players", e);
        } finally {
            dbConnection.close(conn);
        }
    }

    /**
     * Reconstructs a Lobby from a result set with JOIN data.
     * This method processes multiple rows where lobby data is repeated
     * and player data varies for each player in the lobby.
     */
    private Optional<Lobby> reconstructLobbyWithPlayers(ResultSet rs) throws SQLException {
        Lobby lobby = null;
        Map<String, Player> playerMap = new HashMap<>();

        while (rs.next()) {
            // Reconstruct lobby only once (same for all rows)
            if (lobby == null) {
                String lobbyId = rs.getString("lobby_id");
                String lobbyName = rs.getString("lobby_name");
                int maxPlayers = rs.getInt("max_players");
                String adminPlayerId = rs.getString("admin_player_id");

                lobby = new Lobby(
                    new LobbyId(lobbyId),
                    lobbyName,
                    maxPlayers,
                    PlayerId.from(adminPlayerId)
                );
            }

            // Reconstruct each player (avoid duplicates)
            String playerId = rs.getString("player_id");
            if (playerId != null && !playerMap.containsKey(playerId)) {
                String playerName = rs.getString("player_name");
                int playerChips = rs.getInt("player_chips");

                // Players in lobby context are not "folded" - that's a game state
                // So we always pass false for folded status
                Player player = Player.reconstitute(
                    PlayerId.from(playerId),
                    playerName,
                    playerChips,
                    false  // Folded is game state, not relevant in lobby context
                );
                
                playerMap.put(playerId, player);
                
                // Add player to lobby
                lobby.addPlayer(player);
            }
        }

        if (lobby == null) {
            return Optional.empty();
        }

        return Optional.of(lobby);
    }

    @Override
    public List<Lobby> findOpenLobbies() {
        List<Lobby> lobbies = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM lobbies WHERE started = 0 ORDER BY created_at DESC";

            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    Lobby lobby = reconstructLobby(conn, rs);
                    if (lobby.isOpen()) {
                        lobbies.add(lobby);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find open lobbies", e);
        } finally {
            dbConnection.close(conn);
        }

        return lobbies;
    }

    @Override
    public List<Lobby> findByPlayer(PlayerId playerId) {
        List<Lobby> lobbies = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT l.* FROM lobbies l "
                    + "JOIN lobby_players lp ON l.id = lp.lobby_id "
                    + "WHERE lp.player_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerId.getValue().toString());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    lobbies.add(reconstructLobby(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find lobbies by player", e);
        } finally {
            dbConnection.close(conn);
        }

        return lobbies;
    }

    @Override
    public boolean exists(LobbyId id) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM lobbies WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.getValue());
                ResultSet rs = stmt.executeQuery();
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check lobby existence", e);
        } finally {
            dbConnection.close(conn);
        }
    }

    @Override
    public void delete(LobbyId id) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            String sql = "DELETE FROM lobbies WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.getValue());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete lobby", e);
        } finally {
            dbConnection.close(conn);
        }
    }

    @Override
    public List<Lobby> findAll() {
        List<Lobby> lobbies = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM lobbies ORDER BY created_at DESC";

            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    lobbies.add(reconstructLobby(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all lobbies", e);
        } finally {
            dbConnection.close(conn);
        }

        return lobbies;
    }

    private Lobby reconstructLobby(Connection conn, ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        int maxPlayers = rs.getInt("max_players");
        String adminPlayerId = rs.getString("admin_player_id");

        Lobby lobby = new Lobby(new LobbyId(id), name, maxPlayers, PlayerId.from(adminPlayerId));

        // Load players with their full data using JOIN
        loadLobbyPlayers(conn, lobby);

        return lobby;
    }

    private void loadLobbyPlayers(Connection conn, Lobby lobby) throws SQLException {
        String sql = """
            SELECT p.id, p.name, p.chips
            FROM lobby_players lp
            JOIN players p ON lp.player_id = p.id
            WHERE lp.lobby_id = ?
            ORDER BY lp.rowid
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lobby.getId().getValue());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Player player = Player.reconstitute(
                    PlayerId.from(rs.getString("id")),
                    rs.getString("name"),
                    rs.getInt("chips"),
                    false  // Folded is game state, not relevant in lobby context
                );
                lobby.addPlayer(player);
            }
        }
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
