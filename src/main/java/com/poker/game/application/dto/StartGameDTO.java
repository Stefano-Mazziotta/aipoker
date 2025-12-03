package com.poker.game.application.dto;

import java.util.List;

/**
 * Data Transfer Object for Start Game response.
 * Used to return game initialization data after starting a game.
 * Matches the structure of GameStartedEvent data for consistency.
 */
public record StartGameDTO(
    String gameId,
    String lobbyId,
    List<PlayerGameStateDTO> players,
    int smallBlind,
    int bigBlind,
    int pot,
    int currentBet,
    String currentPlayerId,
    String currentPlayerName,
    String gameState
) {
    public static StartGameDTO fromDomain(
        String gameId,
        String lobbyId,
        List<PlayerGameStateDTO> players,
        int smallBlind,
        int bigBlind,
        int pot,
        int currentBet,
        String currentPlayerId,
        String currentPlayerName,
        String gameState
    ) {
        return new StartGameDTO(
            gameId,
            lobbyId,
            players,
            smallBlind,
            bigBlind,
            pot,
            currentBet,
            currentPlayerId,
            currentPlayerName,
            gameState
        );
    }

    /**
     * Nested DTO for player state in the game.
     * Matches GamePlayerData structure from events.
     */
    public record PlayerGameStateDTO(
        String playerId,
        String playerName,
        int chips,
        int currentBet,
        boolean isFolded,
        boolean isAllIn
    ) {}
}
