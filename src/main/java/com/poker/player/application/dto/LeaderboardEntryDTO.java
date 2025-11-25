package com.poker.player.application.dto;

/**
 * Data Transfer Object for Leaderboard entry.
 * Used to transfer player ranking data.
 */
public record LeaderboardEntryDTO(
    int rank,
    String playerName,
    int chips,
    int gamesPlayed,
    int gamesWon
) {
    public static LeaderboardEntryDTO fromDomain(int rank, String playerName, int chips,
                                                 int gamesPlayed, int gamesWon) {
        return new LeaderboardEntryDTO(rank, playerName, chips, gamesPlayed, gamesWon);
    }
}
