package com.poker.player.application;

import com.poker.player.domain.model.Player;
import com.poker.player.domain.repository.PlayerRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for retrieving the leaderboard.
 */
public class GetLeaderboardUseCase {
    private final PlayerRepository playerRepository;

    public GetLeaderboardUseCase(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public LeaderboardResponse execute(GetLeaderboardCommand command) {
        List<Player> topPlayers = playerRepository.findTopByChips(command.limit());
        
        List<PlayerRanking> rankings = topPlayers.stream()
            .map(player -> new PlayerRanking(
                player.getName(),
                player.getChipsAmount()
            ))
            .collect(Collectors.toList());
        
        return new LeaderboardResponse(rankings);
    }

    public record GetLeaderboardCommand(int limit) {
        public GetLeaderboardCommand {
            if (limit <= 0 || limit > 100) {
                throw new IllegalArgumentException("Limit must be between 1 and 100");
            }
        }
    }
    
    public record PlayerRanking(String name, int chips) {}
    
    public record LeaderboardResponse(List<PlayerRanking> rankings) {}
}
