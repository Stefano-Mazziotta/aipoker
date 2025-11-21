package com.poker.player.infrastructure.persistence;

import com.poker.player.domain.model.*;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.shared.infrastructure.database.DatabaseConnection;
import java.sql.*;
import java.util.*;

/**
 * SQLite implementation of PlayerRepository.
 */
public class SQLitePlayerRepository implements PlayerRepository {

    private final DatabaseConnection dbConnection;

    public SQLitePlayerRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public void save(Player player) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();

            if (exists(player.getId())) {
                updatePlayer(conn, player);
            } else {
                insertPlayer(conn, player);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save player", e);
        } finally {
            dbConnection.close(conn);
        }
    }

    private void insertPlayer(Connection conn, Player player) throws SQLException {
        String sql = "INSERT INTO players (id, name, chips, created_at, updated_at) "
                + "VALUES (?, ?, ?, datetime('now'), datetime('now'))";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getId().getValue().toString());
            stmt.setString(2, player.getName());
            stmt.setInt(3, player.getChips().getAmount());
            stmt.executeUpdate();
        }
    }

    private void updatePlayer(Connection conn, Player player) throws SQLException {
        String sql = "UPDATE players SET name = ?, chips = ?, "
                + "updated_at = datetime('now') WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getName());
            stmt.setInt(2, player.getChips().getAmount());
            stmt.setString(3, player.getId().getValue().toString());
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Player> findById(PlayerId id) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM players WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.getValue().toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return Optional.of(mapToPlayer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find player", e);
        } finally {
            dbConnection.close(conn);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Player> findByName(String name) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM players WHERE name = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return Optional.of(mapToPlayer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find player by name", e);
        } finally {
            dbConnection.close(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<Player> findAll() {
        List<Player> players = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM players ORDER BY name";

            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    players.add(mapToPlayer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all players", e);
        } finally {
            dbConnection.close(conn);
        }

        return players;
    }

    @Override
    public boolean exists(PlayerId id) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM players WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.getValue().toString());
                ResultSet rs = stmt.executeQuery();
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check player existence", e);
        } finally {
            dbConnection.close(conn);
        }
    }

    @Override
    public void delete(PlayerId id) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            String sql = "DELETE FROM players WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.getValue().toString());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete player", e);
        } finally {
            dbConnection.close(conn);
        }
    }

    @Override
    public List<Player> findTopByChips(int limit) {
        List<Player> players = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM players ORDER BY chips DESC LIMIT ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    players.add(mapToPlayer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find top players", e);
        } finally {
            dbConnection.close(conn);
        }

        return players;
    }

    private Player mapToPlayer(ResultSet rs) throws SQLException {
        PlayerId id = PlayerId.from(rs.getString("id"));
        String name = rs.getString("name");
        int chips = rs.getInt("chips");

        // Player folded state is managed in game_players table, not players table
        return Player.reconstitute(id, name, chips, false);
    }
}
