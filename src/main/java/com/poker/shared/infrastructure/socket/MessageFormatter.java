package com.poker.shared.infrastructure.socket;

import com.poker.game.application.*;
import com.poker.lobby.application.*;
import com.poker.player.application.*;

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
        return String.format("""
            SUCCESS: Player registered
            ID: %s
            Name: %s
            Chips: %d
            """, response.id(), response.name(), response.chips());
    }

    public String formatGameStarted(StartGameUseCase.GameResponse response) {
        return String.format("""
            SUCCESS: Game started
            Game ID: %s
            State: %s
            Players: %s
            Pot: %d
            """, response.gameId(), response.state(), String.join(", ", response.players()), response.pot());
    }

    public String formatActionExecuted(PlayerActionUseCase.ActionResponse response) {
        return String.format("""
            SUCCESS: Action executed
            Game State: %s
            Current Bet: %d
            Pot: %d
            Player Folded: %s
            """, response.gameState(), response.currentBet(), response.pot(), response.playerFolded());
    }

    public String formatCardsDealt(DealCardsUseCase.CardsResponse response) {
        return String.format("""
            SUCCESS: Cards dealt
            Game ID: %s
            State: %s
            Community Cards: %s
            Total Cards: %d
            """, response.gameId(), response.state(), response.communityCards(), response.communityCardsCount());
    }

    public String formatWinnerDetermined(DetermineWinnerUseCase.WinnerResponse response) {
        return String.format("""
            SUCCESS: Winner determined!
            Winner: %s (ID: %s)
            Pot Won: %d
            Total Chips: %d
            """, response.winnerName(), response.winnerId(), response.potWon(), response.totalChips());
    }

    public String formatLobbyCreated(CreateLobbyUseCase.LobbyResponse response) {
        return String.format("""
            SUCCESS: Lobby created
            Lobby ID: %s
            Name: %s
            Players: %d/%d
            Open: %s
            """, response.lobbyId(), response.name(), response.currentPlayers(), 
            response.maxPlayers(), response.isOpen());
    }

    public String formatLobbyJoined(JoinLobbyUseCase.LobbyResponse response) {
        return String.format("""
            SUCCESS: Joined lobby
            Lobby ID: %s
            Name: %s
            Players: %d/%d
            Open: %s
            """, response.lobbyId(), response.name(), response.currentPlayers(), 
            response.maxPlayers(), response.isOpen());
    }

    public String formatLeaderboard(GetLeaderboardUseCase.LeaderboardResponse response) {
        StringBuilder sb = new StringBuilder("SUCCESS: Leaderboard\n");
        sb.append("╔═══════════════════════════════════════════════╗\n");
        sb.append("║  RANK  │  NAME                │  CHIPS       ║\n");
        sb.append("╠═══════════════════════════════════════════════╣\n");
        
        int rank = 1;
        for (var player : response.rankings()) {
            sb.append(String.format("║  %-5d │  %-20s │  %-11d ║\n", 
                rank++, player.name(), player.chips()));
        }
        
        sb.append("╚═══════════════════════════════════════════════╝\n");
        return sb.toString();
    }
}
