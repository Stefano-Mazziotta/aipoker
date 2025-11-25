package com.poker.game.application.dto;

import java.util.List;

/**
 * Data Transfer Object for Start Game response.
 * Used to return game initialization data after starting a game.
 */
public record StartGameDTO(
    String gameId,
    String state,
    List<String> players,
    int pot
) {
    public static StartGameDTO fromDomain(String gameId, String state, List<String> players, int pot) {
        return new StartGameDTO(gameId, state, players, pot);
    }
}
