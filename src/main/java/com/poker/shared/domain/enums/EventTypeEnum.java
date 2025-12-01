package com.poker.shared.domain.enums;

/**
 * Enum representing all possible event types in the poker system.
 */
public enum EventTypeEnum {

    ERROR("ERROR"),
    
    // Player events
    PLAYER_REGISTERED("PLAYER_REGISTERED"),
    PLAYER_JOINED("PLAYER_JOINED"),
    PLAYER_LEFT("PLAYER_LEFT"),
    PLAYER_READY("PLAYER_READY"),
    PLAYER_UNREADY("PLAYER_UNREADY"),

    // Game events
    GAME_STARTED("GAME_STARTED"),
    GAME_ENDED("GAME_ENDED"),
    ROUND_COMPLETED("ROUND_COMPLETED"),
    DEALER_DEALT_CARDS("DEALER_DEALT_CARDS"),

    // Player actions
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
    LEADERBOARD_RETRIEVED("LEADERBOARD_RETRIEVED");

    private final String value;

    EventTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}