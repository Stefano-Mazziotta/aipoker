package com.poker.shared.infrastructure.socket;

import com.poker.game.application.DealCardsUseCase;
import com.poker.game.application.DetermineWinnerUseCase;
import com.poker.game.application.GetGameStateUseCase;
import com.poker.game.application.GetPlayerCardsUseCase;
import com.poker.game.application.PlayerActionUseCase;
import com.poker.game.application.StartGameUseCase;
import com.poker.lobby.application.CreateLobbyUseCase;
import com.poker.lobby.application.JoinLobbyUseCase;
import com.poker.player.application.GetLeaderboardUseCase;
import com.poker.player.application.RegisterPlayerUseCase;

/**
 * Formats messages and responses for socket communication.
 * Uses simple text format (could be extended to JSON).
 */
public class MessageFormatter {

    public String formatWelcome() {
        return """
            ╔═══════════════════════════════════════════════╗
            ║   TEXAS HOLD'EM POKER SERVER                 ║
            ║   Type 'HELP' for available commands         ║
            ╚═══════════════════════════════════════════════╝
            """;
    }

    public String formatError(String message) {
        return "ERROR: " + message;
    }

    public String formatInfo(String message) {
        return "INFO: " + message;
    }

    public String formatPlayerRegistered(RegisterPlayerUseCase.PlayerResponse response) {
        return """
            SUCCESS: Player registered
            ID: %s
            Name: %s
            Chips: %d
            """.formatted(response.id(), response.name(), response.chips());
    }

    public String formatGameStarted(StartGameUseCase.GameResponse response) {
        return """
            SUCCESS: Game started
            Game ID: %s
            State: %s
            Players: %s
            Pot: %d
            """.formatted(response.gameId(), response.state(), String.join(", ", response.players()), response.pot());
    }

    public String formatActionExecuted(PlayerActionUseCase.ActionResponse response) {
        return """
            SUCCESS: Action executed
            Game State: %s
            Current Bet: %d
            Pot: %d
            Player Folded: %s
            """.formatted(response.gameState(), response.currentBet(), response.pot(), response.playerFolded());
    }

    public String formatCardsDealt(DealCardsUseCase.CardsResponse response) {
        return """
            SUCCESS: Cards dealt
            Game ID: %s
            State: %s
            Community Cards: %s
            Total Cards: %d
            """.formatted(response.gameId(), response.state(), response.communityCards(), response.communityCardsCount());
    }

    public String formatWinnerDetermined(DetermineWinnerUseCase.WinnerResponse response) {
        return """
            SUCCESS: Winner determined!
            Winner: %s (ID: %s)
            Pot Won: %d
            Total Chips: %d
            """.formatted(response.winnerName(), response.winnerId(), response.potWon(), response.totalChips());
    }

    public String formatLobbyCreated(CreateLobbyUseCase.LobbyResponse response) {
        return """
            SUCCESS: Lobby created
            Lobby ID: %s
            Name: %s
            Players: %d/%d
            Admin: %s
            Open: %s
            """.formatted(response.lobbyId(), response.name(), response.currentPlayers(),
            response.maxPlayers(), response.adminPlayerId(), response.isOpen());
    }

    public String formatLobbyJoined(JoinLobbyUseCase.LobbyResponse response) {
        return """
            SUCCESS: Joined lobby
            Lobby ID: %s
            Name: %s
            Players: %d/%d
            Admin: %s
            Open: %s
            """.formatted(response.lobbyId(), response.name(), response.currentPlayers(),
            response.maxPlayers(), response.adminPlayerId(), response.isOpen());
    }

    public String formatLeaderboard(GetLeaderboardUseCase.LeaderboardResponse response) {
        StringBuilder sb = new StringBuilder("SUCCESS: Leaderboard\n");
        sb.append("╔═══════════════════════════════════════════════╗\n");
        sb.append("║  RANK  │  NAME                │  CHIPS       ║\n");
        sb.append("╠═══════════════════════════════════════════════╣\n");
        
        int rank = 1;
        for (var player : response.rankings()) {
            sb.append("║  %-5d │  %-20s │  %-11d ║\n".formatted(
                rank++, player.name(), player.chips()));
        }
        
        sb.append("╚═══════════════════════════════════════════════╝\n");
        return sb.toString();
    }

    public String formatPlayerCards(GetPlayerCardsUseCase.PlayerCardsResponse response) {
        return """
            SUCCESS: Your hole cards
            Player: %s
            Cards: %s
            Card Count: %d
            """.formatted(response.playerName(), response.cards(), response.cardCount());
    }

    public String formatGameState(GetGameStateUseCase.GameStateResponse response) {
        return """
            SUCCESS: Current game state
            State: %s
            Community Cards: %s
            Pot: %d
            Players: %s
            Player Count: %d
            """.formatted(response.state(), response.communityCards(), response.pot(),
            response.players(), response.playerCount());
    }
}
