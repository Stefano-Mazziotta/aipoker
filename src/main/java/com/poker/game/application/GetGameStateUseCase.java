package com.poker.game.application;

import java.util.List;
import java.util.stream.Collectors;

import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.repository.GameRepository;
import com.poker.shared.domain.valueobject.Card;

/**
 * Use case for getting current game state.
 */
public class GetGameStateUseCase {
    private final GameRepository gameRepository;

    public GetGameStateUseCase(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public GameStateResponse execute(GameStateCommand command) {
        Game game = gameRepository.findById(GameId.from(command.gameId()))
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        List<Card> communityCards = game.getCommunityCards();
        String communityCardsStr = communityCards.stream()
            .map(card -> card.getRank().name() + card.getSuit().getSymbol())
            .reduce((a, b) -> a + " " + b)
            .orElse("None");

        String playersList = game.getPlayers().stream()
            .map(p -> p.getName() + (p.isFolded() ? " (folded)" : ""))
            .collect(Collectors.joining(", "));

        return new GameStateResponse(
            game.getState().name(),
            communityCardsStr,
            communityCards.size(),
            game.getCurrentPot().getAmount(),
            playersList,
            game.getPlayers().size()
        );
    }

    public record GameStateCommand(String gameId) {}
    
    public record GameStateResponse(
        String state,
        String communityCards,
        int communityCardCount,
        int pot,
        String players,
        int playerCount
    ) {}
}
