package com.poker.game.application;

import com.poker.game.domain.model.*;
import com.poker.game.domain.repository.GameRepository;

/**
 * Use case for dealing community cards (Flop, Turn, River).
 */
public class DealCardsUseCase {
    private final GameRepository gameRepository;

    public DealCardsUseCase(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public CardsResponse dealFlop(DealCardsCommand command) {
        Game game = loadGame(command.gameId());
        game.dealFlop();
        gameRepository.save(game);
        
        return new CardsResponse(
            game.getId().getValue(),
            game.getState().name(),
            game.getCommunityCards().size(),
            formatCards(game.getCommunityCards())
        );
    }

    public CardsResponse dealTurn(DealCardsCommand command) {
        Game game = loadGame(command.gameId());
        game.dealTurn();
        gameRepository.save(game);
        
        return new CardsResponse(
            game.getId().getValue(),
            game.getState().name(),
            game.getCommunityCards().size(),
            formatCards(game.getCommunityCards())
        );
    }

    public CardsResponse dealRiver(DealCardsCommand command) {
        Game game = loadGame(command.gameId());
        game.dealRiver();
        gameRepository.save(game);
        
        return new CardsResponse(
            game.getId().getValue(),
            game.getState().name(),
            game.getCommunityCards().size(),
            formatCards(game.getCommunityCards())
        );
    }

    private Game loadGame(String gameId) {
        return gameRepository.findById(new GameId(gameId))
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }

    private String formatCards(java.util.List<com.poker.shared.domain.valueobject.Card> cards) {
        return cards.stream()
            .map(card -> card.getRank().name() + card.getSuit().getSymbol())
            .reduce((a, b) -> a + " " + b)
            .orElse("");
    }

    public record DealCardsCommand(String gameId) {}
    
    public record CardsResponse(
        String gameId,
        String state,
        int communityCardsCount,
        String communityCards
    ) {}
}
