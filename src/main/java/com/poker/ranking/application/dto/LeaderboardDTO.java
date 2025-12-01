package com.poker.ranking.application.dto;

import java.util.List;

import com.poker.ranking.application.GetLeaderboardUseCase.PlayerRanking;


public record LeaderboardDTO(
    List<PlayerRanking> rankings
) 
{
    public static LeaderboardDTO fromDomain(
        List<PlayerRanking> rankings
    ) 
    {
        return new LeaderboardDTO(rankings);
    }    
}
