package com.poker.shared.infrastructure.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
            System.out.println("Database schema initialized successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read schema file", e);
        } finally {
            dbConn.close(conn);
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
            if (Files.exists(Paths.get(path))) {
                return new String(Files.readAllBytes(Paths.get(path)));
            }
        }

        throw new IOException("schema.sql not found in any expected location");
    }

    private static void executeSQLScript(Connection conn, String script) throws SQLException {
        Statement stmt = conn.createStatement();

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

        stmt.close();
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
