package com.poker.game.application.dto;

/**
 * Data Transfer Object for Player Cards response.
 * Used to return a player's hole cards information.
 */
public record PlayerCardsDTO(
    String playerName,
    String cards,
    int cardCount
) {
    public static PlayerCardsDTO fromDomain(String playerName, String cards, int cardCount) {
        return new PlayerCardsDTO(playerName, cards, cardCount);
    }
}
