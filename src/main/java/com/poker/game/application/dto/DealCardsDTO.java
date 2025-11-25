package com.poker.game.application.dto;

/**
 * Data Transfer Object for Deal Cards response.
 * Used to return community cards information after dealing flop/turn/river.
 */
public record DealCardsDTO(
    String gameId,
    String state,
    int communityCardsCount,
    String communityCards
) {
    public static DealCardsDTO fromDomain(String gameId, String state, int communityCardsCount, String communityCards) {
        return new DealCardsDTO(gameId, state, communityCardsCount, communityCards);
    }
}
