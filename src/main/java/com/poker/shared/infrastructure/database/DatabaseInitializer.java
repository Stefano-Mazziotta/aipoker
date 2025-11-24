package com.poker.shared.infrastructure.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the database schema from schema.sql file.
 */
public class DatabaseInitializer {

    public static void initialize() {
        DatabaseConnection dbConn = DatabaseConnection.getInstance();
        Connection conn = null;

        try {
            conn = dbConn.getConnection();
            String schema = readSchemaFile();
            executeSQLScript(conn, schema);
            
            // Run migrations for existing databases
            runMigrations(conn);
            
            System.out.println("Database schema initialized successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read schema file", e);
        } finally {
            dbConn.close(conn);
        }
    }

    /**
     * Runs database migrations to update existing schemas
     */
    private static void runMigrations(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            try {
                // Migration: Add admin_player_id column to lobbies table if it doesn't exist
                stmt.execute("ALTER TABLE lobbies ADD COLUMN admin_player_id TEXT");
                System.out.println("Migration: Added admin_player_id column to lobbies table");
            } catch (SQLException e) {
                // Column might already exist, which is fine
                if (!e.getMessage().contains("duplicate column name")) {
                    System.out.println("Note: admin_player_id column already exists or migration not needed");
                }
            }
            
            try {
                // Migration: Add current_player_index column to games table if it doesn't exist
                stmt.execute("ALTER TABLE games ADD COLUMN current_player_index INTEGER DEFAULT 0");
                System.out.println("Migration: Added current_player_index column to games table");
            } catch (SQLException e) {
                // Column might already exist, which is fine
                if (!e.getMessage().contains("duplicate column name")) {
                    System.out.println("Note: current_player_index column already exists or migration not needed");
                }
            }
            
            try {
                // Migration: Add players_acted_this_round column to games table if it doesn't exist
                stmt.execute("ALTER TABLE games ADD COLUMN players_acted_this_round TEXT");
                System.out.println("Migration: Added players_acted_this_round column to games table");
            } catch (SQLException e) {
                // Column might already exist, which is fine
                if (!e.getMessage().contains("duplicate column name")) {
                    System.out.println("Note: players_acted_this_round column already exists or migration not needed");
                }
            }
        }
    }

    private static String readSchemaFile() throws IOException {
        // Try multiple paths to find schema.sql
        String[] paths = {
            "schema.sql", // Current directory (production)
            "../schema.sql", // One level up
            "../../schema.sql", // Two levels up
            "../../../schema.sql", // Three levels up
            "src/main/resources/schema.sql", // Resources directory
            "resources/schema.sql" // Alternative resources
        };

        for (String path : paths) {
            if (Files.exists(Path.of(path))) {
                return new String(Files.readAllBytes(Path.of(path)));
            }
        }

        throw new IOException("schema.sql not found in any expected location");
    }

    private static void executeSQLScript(Connection conn, String script) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Parse SQL statements properly, handling triggers and compound statements
            StringBuilder currentStatement = new StringBuilder();
            String[] lines = script.split("\n");
            boolean inTrigger = false;

            for (String line : lines) {
                String trimmed = line.trim();

                // Skip empty lines and comments
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }

                // Track if we're inside a trigger definition
                if (trimmed.toUpperCase().startsWith("CREATE TRIGGER")) {
                    inTrigger = true;
                }

                currentStatement.append(line).append("\n");

                // Check if statement is complete
                if (trimmed.endsWith(";")) {
                    // If in trigger, check for END; to complete the trigger
                    if (inTrigger) {
                        if (trimmed.toUpperCase().contains("END;")) {
                            inTrigger = false;
                            executeStatement(stmt, currentStatement.toString());
                            currentStatement = new StringBuilder();
                        }
                    } else {
                        // Regular statement ends with semicolon
                        executeStatement(stmt, currentStatement.toString());
                        currentStatement = new StringBuilder();
                    }
                }
            }

            // Execute any remaining statement
            if (currentStatement.length() > 0) {
                String remaining = currentStatement.toString().trim();
                if (!remaining.isEmpty()) {
                    executeStatement(stmt, remaining);
                }
            }
        }
    }

    private static void executeStatement(Statement stmt, String sql) throws SQLException {
        String trimmed = sql.trim();
        if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
            // Remove trailing semicolon for execution
            if (trimmed.endsWith(";")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
            stmt.execute(trimmed);
        }
    }
}
