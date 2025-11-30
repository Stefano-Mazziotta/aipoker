package com.poker.shared.application.dto;

import com.poker.game.application.GetGameStateUseCase;
import com.poker.game.application.GetPlayerCardsUseCase;
import com.poker.game.application.PlayerActionUseCase;
import com.poker.game.application.StartGameUseCase;
import com.poker.lobby.application.CreateLobbyUseCase;
import com.poker.lobby.application.JoinLobbyUseCase;
import com.poker.lobby.application.LeaveLobbyUseCase;
import com.poker.player.application.GetLeaderboardUseCase;
import com.poker.player.application.RegisterPlayerUseCase;

final public class PokerUseCasesDTO {
    final private RegisterPlayerUseCase registerPlayer;
    final private StartGameUseCase startGame;
    final private PlayerActionUseCase playerAction;
    final private CreateLobbyUseCase createLobby;
    final private JoinLobbyUseCase joinLobby;
    final private LeaveLobbyUseCase leaveLobby;
    final private GetLeaderboardUseCase getLeaderboard;
    final private GetPlayerCardsUseCase getPlayerCards;
    final private GetGameStateUseCase getGameState;

    public PokerUseCasesDTO(
        RegisterPlayerUseCase registerPlayer,
        StartGameUseCase startGame,
        PlayerActionUseCase playerAction,
        CreateLobbyUseCase createLobby,
        JoinLobbyUseCase joinLobby,
        LeaveLobbyUseCase leaveLobby,
        GetLeaderboardUseCase getLeaderboard,
        GetPlayerCardsUseCase getPlayerCards,
        GetGameStateUseCase getGameState
    ) {
        this.registerPlayer = registerPlayer;
        this.startGame = startGame;
        this.playerAction = playerAction;
        this.createLobby = createLobby;
        this.joinLobby = joinLobby;
        this.leaveLobby = leaveLobby;
        this.getLeaderboard = getLeaderboard;
        this.getPlayerCards = getPlayerCards;
        this.getGameState = getGameState;
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

    public GetPlayerCardsUseCase getGetPlayerCards() {
        return getPlayerCards;
    }

    public GetGameStateUseCase getGetGameState() {
        return getGameState;
    }
}