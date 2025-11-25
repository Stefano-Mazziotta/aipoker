package com.poker.game.application;

import java.util.List;

import com.poker.game.application.dto.PlayerCardsDTO;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.repository.GameRepository;
import com.poker.player.domain.model.Player;
import com.poker.shared.domain.valueobject.Card;

/**
 * Use case for getting a player's hole cards.
 * Returns PlayerCardsDTO to decouple the application layer from domain entities.
 */
public class GetPlayerCardsUseCase {
    private final GameRepository gameRepository;

    public GetPlayerCardsUseCase(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public PlayerCardsDTO execute(PlayerCardsCommand command) {
        Game game = gameRepository.findById(GameId.from(command.gameId()))
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        Player player = game.getPlayers().stream()
            .filter(p -> p.getId().getValue().toString().equals(command.playerId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Player not in game"));

        List<Card> holeCards = player.getHand().getCards();
        String cardsStr = holeCards.stream()
            .map(card -> card.getRank().name() + card.getSuit().getSymbol())
            .reduce((a, b) -> a + " " + b)
            .orElse("No cards");

        return PlayerCardsDTO.fromDomain(
            player.getName(),
            cardsStr,
            holeCards.size()
        );
    }

    public record PlayerCardsCommand(String gameId, String playerId) {}
}
