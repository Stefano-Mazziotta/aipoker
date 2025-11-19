package com.poker.player.domain.model;

/**
 * Enum representing possible player actions in a poker game.
 */
public enum PlayerAction {
    FOLD,       // Give up the hand
    CHECK,      // Pass without betting (only when no bet to call)
    CALL,       // Match the current bet
    RAISE,      // Increase the current bet
    ALL_IN      // Bet all remaining chips
}
