package com.poker.shared.infrastructure.websocket;

/**
 * Enumeration of all supported WebSocket commands.
 * Provides type-safe command handling and eliminates magic strings.
 * Only includes commands used in the actual game flow.
 */
public enum WebSocketCommand {
    // Player commands
    REGISTER("REGISTER_PLAYER"),
    
    // Lobby commands
    CREATE_LOBBY("CREATE_LOBBY"),
    JOIN_LOBBY("JOIN_LOBBY"),
    LEAVE_LOBBY("LEAVE_LOBBY"),
    
    // Game commands
    START_GAME("START_GAME"),
    
    // Player action commands
    FOLD("FOLD"),
    CHECK("CHECK"),
    CALL("CALL"),
    RAISE("RAISE"),
    ALL_IN("ALL_IN"),
    
    // Game state commands
    GET_MY_CARDS("GET_MY_CARDS"),
    GET_GAME_STATE("GET_GAME_STATE"),
    
    // Utility commands
    LEADERBOARD("LEADERBOARD"),
    HELP("HELP"),
    QUIT("QUIT"),
    
    // Unknown command
    UNKNOWN("UNKNOWN");
    
    private final String command;
    
    WebSocketCommand(String command) {
        this.command = command;
    }
    
    public String getCommand() {
        return command;
    }
    
    /**
     * Parse a string command to enum value.
     * Returns UNKNOWN if command is not recognized.
     */
    public static WebSocketCommand fromString(String commandStr) {
        if (commandStr == null || commandStr.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        String upperCommand = commandStr.trim().toUpperCase();
        
        for (WebSocketCommand cmd : WebSocketCommand.values()) {
            if (cmd.command.equals(upperCommand)) {
                return cmd;
            }
        }
        
        return UNKNOWN;
    }
    
    @Override
    public String toString() {
        return command;
    }
}
