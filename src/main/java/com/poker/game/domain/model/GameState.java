package com.poker.game.domain.model;

/**
 * Enum representing the various states of a poker game.
 */
public enum GameState {
    WAITING,      // Waiting for players to join
    PRE_FLOP,     // Before community cards (hole cards dealt)
    FLOP,         // 3 community cards revealed
    TURN,         // 4th community card revealed
    RIVER,        // 5th community card revealed
    SHOWDOWN,     // Revealing hands and determining winner
    FINISHED      // Game concluded
}
