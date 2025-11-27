package com.poker.shared.infrastructure.websocket;

/**
 * Enumeration of all supported WebSocket commands.
 * Provides type-safe command handling and eliminates magic strings.
 */
public enum WebSocketCommand {
    // Player commands
    REGISTER("REGISTER"),
    
    // Lobby commands
    CREATE_LOBBY("CREATE_LOBBY"),
    JOIN_LOBBY("JOIN_LOBBY"),
    LEAVE_LOBBY("LEAVE_LOBBY"),
    SUBSCRIBE_LOBBY("SUBSCRIBE_LOBBY"),
    
    // Game commands
    START_GAME("START_GAME"),
    SUBSCRIBE_GAME("SUBSCRIBE_GAME"),
    
    // Player action commands
    FOLD("FOLD"),
    CHECK("CHECK"),
    CALL("CALL"),
    RAISE("RAISE"),
    ALL_IN("ALL_IN"),
    
    // Dealing commands
    DEAL_FLOP("DEAL_FLOP"),
    DEAL_TURN("DEAL_TURN"),
    DEAL_RIVER("DEAL_RIVER"),
    
    // Game state commands
    DETERMINE_WINNER("DETERMINE_WINNER"),
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
    
    /**
     * Check if command starts with this command prefix.
     * Useful for commands with arguments like "SUBSCRIBE_LOBBY <lobbyId>".
     */
    public boolean isPrefixOf(String commandStr) {
        if (commandStr == null) {
            return false;
        }
        return commandStr.trim().toUpperCase().startsWith(this.command);
    }
    
    @Override
    public String toString() {
        return command;
    }
}
