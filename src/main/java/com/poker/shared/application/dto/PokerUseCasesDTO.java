package com.poker.shared.application.dto;

import com.poker.game.application.DealCardsUseCase;
import com.poker.game.application.PlayerActionUseCase;
import com.poker.game.application.StartGameUseCase;
import com.poker.lobby.application.CreateLobbyUseCase;
import com.poker.lobby.application.JoinLobbyUseCase;
import com.poker.lobby.application.LeaveLobbyUseCase;
import com.poker.player.application.RegisterPlayerUseCase;
import com.poker.ranking.application.GetLeaderboardUseCase;

final public class PokerUseCasesDTO {
    final private RegisterPlayerUseCase registerPlayer;
    final private StartGameUseCase startGame;
    final private PlayerActionUseCase playerAction;
    final private DealCardsUseCase dealCards;
    final private CreateLobbyUseCase createLobby;
    final private JoinLobbyUseCase joinLobby;
    final private LeaveLobbyUseCase leaveLobby;
    final private GetLeaderboardUseCase getLeaderboard;

    public PokerUseCasesDTO(
        RegisterPlayerUseCase registerPlayer,
        StartGameUseCase startGame,
        PlayerActionUseCase playerAction,
        DealCardsUseCase dealCards,
        CreateLobbyUseCase createLobby,
        JoinLobbyUseCase joinLobby,
        LeaveLobbyUseCase leaveLobby,
        GetLeaderboardUseCase getLeaderboard
    ) {
        this.registerPlayer = registerPlayer;
        this.startGame = startGame;
        this.playerAction = playerAction;
        this.dealCards = dealCards;
        this.createLobby = createLobby;
        this.joinLobby = joinLobby;
        this.leaveLobby = leaveLobby;
        this.getLeaderboard = getLeaderboard;
    }

    public RegisterPlayerUseCase getRegisterPlayer() {
        return registerPlayer;
    }

    public StartGameUseCase getStartGame() {
        return startGame;
    }

    public PlayerActionUseCase getPlayerAction() {
        return playerAction;
    }

    public CreateLobbyUseCase getCreateLobby() {
        return createLobby;
    }

    public JoinLobbyUseCase getJoinLobby() {
        return joinLobby;
    }

    public LeaveLobbyUseCase getLeaveLobby() {
        return leaveLobby;
    }

    public GetLeaderboardUseCase getGetLeaderboard() {
        return getLeaderboard;
    }

    public DealCardsUseCase getDealCards() {
        return dealCards;
    }
}