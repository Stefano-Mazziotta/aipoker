package com.poker.lobby.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.poker.lobby.domain.model.Lobby;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.lobby.domain.repository.LobbyRepository;
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
            for (PlayerId playerId : lobby.getPlayers()) {
                stmt.setString(1, lobby.getId().getValue());
                stmt.setString(2, playerId.getValue().toString());
                stmt.executeUpdate();
            }
        }
    }

    @Override
    public Optional<Lobby> findById(LobbyId id) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM lobbies WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.getValue());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return Optional.of(reconstructLobby(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find lobby", e);
        } finally {
            dbConnection.close(conn);
        }
        return Optional.empty();
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

        // Load players
        loadLobbyPlayers(conn, lobby);

        return lobby;
    }

    private void loadLobbyPlayers(Connection conn, Lobby lobby) throws SQLException {
        String sql = "SELECT player_id FROM lobby_players WHERE lobby_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lobby.getId().getValue());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lobby.addPlayer(PlayerId.from(rs.getString("player_id")));
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
