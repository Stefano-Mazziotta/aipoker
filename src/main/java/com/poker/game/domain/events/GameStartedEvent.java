package com.poker.game.domain.events;

import java.util.List;

import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when a game is started.
 * This event is broadcast to all players in the lobby to notify them the game has begun.
 * Contains complete game state information so players can immediately render the game table.
 */
public class GameStartedEvent extends DomainEvent {
    private final GameStartedEventData data;

    public GameStartedEvent(
        String gameId,
        String lobbyId,
        List<GamePlayerData> players,
        int smallBlind,
        int bigBlind,
        int pot,
        int currentBet,
        String currentPlayerId,
        String currentPlayerName,
        String gameState
    ) {
        super(EventTypeEnum.GAME_STARTED);
        this.data = new GameStartedEventData(
            gameId, 
            lobbyId, 
            players, 
            smallBlind, 
            bigBlind,
            pot,
            currentBet,
            currentPlayerId,
            currentPlayerName,
            gameState
        );
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.GAME_STARTED;
    }

    @Override
    public GameStartedEventData getData() {
        return data;
    }

    public record GameStartedEventData(
        String gameId,
        String lobbyId,
        List<GamePlayerData> players,
        int smallBlind,
        int bigBlind,
        int pot,
        int currentBet,
        String currentPlayerId,
        String currentPlayerName,
        String gameState
    ) {}
}
