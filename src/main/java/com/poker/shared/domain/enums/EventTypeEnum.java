package com.poker.shared.domain.enums;

/**
 * Enum representing all possible event types in the poker system.
 */
public enum EventTypeEnum {
    
    WELCOME("WELCOME"),
    
    // Player events
    PLAYER_REGISTERED("PLAYER_REGISTERED"),
    PLAYER_JOINED_LOBBY("PLAYER_JOINED_LOBBY"),
    PLAYER_LEFT_LOBBY("PLAYER_LEFT_LOBBY"),
    PLAYER_READY("PLAYER_READY"),
    PLAYER_UNREADY("PLAYER_UNREADY"),

    // Game events
    GAME_STARTED("GAME_STARTED"),
    GAME_ENDED("GAME_ENDED"),
    ROUND_COMPLETED("ROUND_COMPLETED"),
    DEALT_CARDS("DEALT_CARDS"),
    PLAYER_CARDS_DEALT("PLAYER_CARDS_DEALT"),
    WINNER_DETERMINED("WINNER_DETERMINED"),
    GAME_STATE_CHANGED("GAME_STATE_CHANGED"),

    // Player actions
    PLAYER_ACTION("PLAYER_ACTION"),
    PLAYER_BET("PLAYER_BET"),
    PLAYER_FOLD("PLAYER_FOLD"),
    PLAYER_CHECK("PLAYER_CHECK"),
    PLAYER_CALL("PLAYER_CALL"),
    PLAYER_RAISE("PLAYER_RAISE"),
    PLAYER_ALL_IN("PLAYER_ALL_IN"),

    // Lobby events
    LOBBY_CREATED("LOBBY_CREATED"),
    LOBBY_CLOSED("LOBBY_CLOSED"),

    // Chat events
    CHAT_MESSAGE_SENT("CHAT_MESSAGE_SENT"),

    // Ranking
    LEADERBOARD_RETRIEVED("LEADERBOARD_RETRIEVED"),

    ERROR("ERROR");

    private final String value;

    EventTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EventTypeEnum fromString(String eventStr) {
        if (eventStr == null || eventStr.trim().isEmpty()) {
            return ERROR;
        }
        
        String upperEvent = eventStr.trim().toUpperCase();
        
        for (EventTypeEnum event : EventTypeEnum.values()) {
            if (event.value.equals(upperEvent)) {
                return event;
            }
        }
        
        return ERROR;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
