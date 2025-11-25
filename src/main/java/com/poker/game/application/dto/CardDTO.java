package com.poker.game.application.dto;

/**
 * Data Transfer Object for Card information.
 * Used to transfer card data to clients without exposing domain entities.
 */
public record CardDTO(
    String rank,
    String suit
) {
    public static CardDTO fromDomain(String rank, String suit) {
        return new CardDTO(rank, suit);
    }
}
