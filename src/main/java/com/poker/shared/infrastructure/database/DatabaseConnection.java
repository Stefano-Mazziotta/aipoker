package com.poker.shared.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages SQLite database connections. Singleton pattern for connection
 * pooling. Supports configurable database path via DB_PATH environment
 * variable.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private static final String DB_FILE = System.getenv().getOrDefault("DB_PATH", "poker.db");
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private DatabaseConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        conn.setAutoCommit(true);
        return conn;
    }

    public void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
