package com.poker.game.application.dto;

import java.util.List;

/**
 * Data Transfer Object for Game State information.
 * Used to transfer complete game state to clients.
 */
public record GameStateDTO(
    String gameId,
    String phase,
    int pot,
    int currentBet,
    String currentPlayerId,
    String currentPlayerName,
    List<CardDTO> communityCards,
    List<PlayerInGameDTO> players
) {
    public static GameStateDTO fromDomain(String gameId, String phase, int pot, int currentBet,
                                         String currentPlayerId, String currentPlayerName,
                                         List<CardDTO> communityCards, List<PlayerInGameDTO> players) {
        return new GameStateDTO(gameId, phase, pot, currentBet, currentPlayerId, 
                               currentPlayerName, communityCards, players);
    }
}
