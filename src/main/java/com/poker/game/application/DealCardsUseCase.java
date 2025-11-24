package com.poker.game.application;

import java.util.List;
import java.util.stream.Collectors;

import com.poker.game.domain.events.CardsDealtEvent;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.repository.GameRepository;
import com.poker.shared.domain.events.DomainEventPublisher;
import com.poker.shared.domain.valueobject.Card;

/**
 * Use case for dealing community cards (Flop, Turn, River).
 */
public class DealCardsUseCase {
    private final GameRepository gameRepository;
    private final DomainEventPublisher eventPublisher;

    public DealCardsUseCase(GameRepository gameRepository, DomainEventPublisher eventPublisher) {
        this.gameRepository = gameRepository;
        this.eventPublisher = eventPublisher;
    }

    public CardsResponse dealFlop(DealCardsCommand command) {
        Game game = loadGame(command.gameId());
        int prevCount = game.getCommunityCards().size();
        game.dealFlop();
        gameRepository.save(game);
        
        // Publish cards dealt event
        publishCardsEvent(game, "FLOP", prevCount);
        
        return createResponse(game);
    }

    public CardsResponse dealTurn(DealCardsCommand command) {
        Game game = loadGame(command.gameId());
        int prevCount = game.getCommunityCards().size();
        game.dealTurn();
        gameRepository.save(game);
        
        // Publish cards dealt event
        publishCardsEvent(game, "TURN", prevCount);
        
        return createResponse(game);
    }

    public CardsResponse dealRiver(DealCardsCommand command) {
        Game game = loadGame(command.gameId());
        int prevCount = game.getCommunityCards().size();
        game.dealRiver();
        gameRepository.save(game);
        
        // Publish cards dealt event
        publishCardsEvent(game, "RIVER", prevCount);
        
        return createResponse(game);
    }

    private void publishCardsEvent(Game game, String phase, int prevCount) {
        List<Card> allCards = game.getCommunityCards();
        List<String> newCards = allCards.stream()
            .skip(prevCount)
            .map(card -> card.getRank().name() + card.getSuit().getSymbol())
            .collect(Collectors.toList());
        
        List<String> allCardsStr = allCards.stream()
            .map(card -> card.getRank().name() + card.getSuit().getSymbol())
            .collect(Collectors.toList());
        
        CardsDealtEvent event = new CardsDealtEvent(
            game.getId().getValue().toString(),
            phase,
            newCards,
            allCardsStr
        );
        eventPublisher.publishToScope(game.getId().getValue().toString(), event);
    }

    private CardsResponse createResponse(Game game) {
        return new CardsResponse(
            game.getId().getValue().toString(),
            game.getState().name(),
            game.getCommunityCards().size(),
            formatCards(game.getCommunityCards())
        );
    }

    private Game loadGame(String gameId) {
        return gameRepository.findById(GameId.from(gameId))
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }

    private String formatCards(List<Card> cards) {
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
