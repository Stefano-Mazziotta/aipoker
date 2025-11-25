package com.poker.shared.infrastructure.websocket;

import com.poker.game.application.dto.DealCardsDTO;
import com.poker.game.application.dto.DetermineWinnerDTO;
import com.poker.game.application.dto.GetGameStateDTO;
import com.poker.game.application.dto.PlayerActionDTO;
import com.poker.game.application.dto.PlayerCardsDTO;
import com.poker.game.application.dto.StartGameDTO;
import com.poker.lobby.application.dto.LobbyDTO;
import com.poker.player.application.GetLeaderboardUseCase;
import com.poker.player.application.dto.RegisterPlayerDTO;

/**
 * Formats messages and responses for socket communication.
 * Uses simple text format (could be extended to JSON).
 * Now uses DTOs for all use case responses to maintain layer separation.
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

    public String formatPlayerRegistered(RegisterPlayerDTO dto) {
        return """
            SUCCESS: Player registered
            ID: %s
            Name: %s
            Chips: %d
            """.formatted(dto.id(), dto.name(), dto.chips());
    }

    public String formatGameStarted(StartGameDTO dto) {
        return """
            SUCCESS: Game started
            Game ID: %s
            State: %s
            Players: %s
            Pot: %d
            """.formatted(dto.gameId(), dto.state(), String.join(", ", dto.players()), dto.pot());
    }

    public String formatActionExecuted(PlayerActionDTO dto) {
        return """
            SUCCESS: Action executed
            Game State: %s
            Current Bet: %d
            Pot: %d
            Player Folded: %s
            """.formatted(dto.gameState(), dto.currentBet(), dto.pot(), dto.playerFolded());
    }

    public String formatCardsDealt(DealCardsDTO dto) {
        return """
            SUCCESS: Cards dealt
            Game ID: %s
            State: %s
            Community Cards: %s
            Total Cards: %d
            """.formatted(dto.gameId(), dto.state(), dto.communityCards(), dto.communityCardsCount());
    }

    public String formatWinnerDetermined(DetermineWinnerDTO dto) {
        return """
            SUCCESS: Winner determined!
            Winner: %s (ID: %s)
            Pot Won: %d
            Total Chips: %d
            """.formatted(dto.winnerName(), dto.winnerId(), dto.potWon(), dto.totalChips());
    }

    public String formatLobbyCreated(LobbyDTO response) {
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

    public String formatLobbyJoined(LobbyDTO response) {
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

    public String formatPlayerCards(PlayerCardsDTO dto) {
        return """
            SUCCESS: Your hole cards
            Player: %s
            Cards: %s
            Card Count: %d
            """.formatted(dto.playerName(), dto.cards(), dto.cardCount());
    }

    public String formatGameState(GetGameStateDTO dto) {
        return """
            SUCCESS: Current game state
            State: %s
            Community Cards: %s
            Pot: %d
            Players: %s
            Player Count: %d
            """.formatted(dto.state(), dto.communityCards(), dto.pot(),
            dto.players(), dto.playerCount());
    }
}
