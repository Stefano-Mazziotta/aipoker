package com.poker.game.application.dto;

import java.util.List;

/**
 * Data Transfer Object for Dealt Cards event.
 * Used to transfer dealt cards event information.
 */
public record DealtCardsEventDTO(
    String gameId,
    String phase,
    List<String> newCards,
    List<String> allCommunityCards
) {
    public static DealtCardsEventDTO fromDomain(String gameId, String phase, 
                                                 List<String> newCards, List<String> allCommunityCards) {
        return new DealtCardsEventDTO(gameId, phase, 
                                      List.copyOf(newCards), 
                                      List.copyOf(allCommunityCards));
    }
}
