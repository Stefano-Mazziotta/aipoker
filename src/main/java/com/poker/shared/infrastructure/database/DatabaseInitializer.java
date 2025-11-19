package com.poker.shared.infrastructure.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

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
        return new String(Files.readAllBytes(Paths.get("schema.sql")));
    }
    
    private static void executeSQLScript(Connection conn, String script) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Split by semicolon and execute each statement
        String[] statements = script.split(";");
        for (String sql : statements) {
            String trimmed = sql.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                stmt.execute(trimmed);
            }
        }
        
        stmt.close();
    }
}
