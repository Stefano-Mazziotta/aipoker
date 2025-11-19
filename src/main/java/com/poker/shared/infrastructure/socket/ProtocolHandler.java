package com.poker.shared.infrastructure.socket;

import com.poker.game.application.*;
import com.poker.game.domain.model.Blinds;
import com.poker.lobby.application.*;
import com.poker.player.application.*;
import com.poker.player.domain.model.PlayerAction;

import java.util.List;
import java.util.logging.Logger;

/**
 * Handles protocol commands and delegates to appropriate use cases.
 * Command format: COMMAND [args...]
 */
public class ProtocolHandler {
    private static final Logger LOGGER = Logger.getLogger(ProtocolHandler.class.getName());
    
    private final RegisterPlayerUseCase registerPlayer;
    private final StartGameUseCase startGame;
    private final PlayerActionUseCase playerAction;
    private final DealCardsUseCase dealCards;
    private final DetermineWinnerUseCase determineWinner;
    private final CreateLobbyUseCase createLobby;
    private final JoinLobbyUseCase joinLobby;
    private final GetLeaderboardUseCase getLeaderboard;
    private final MessageFormatter formatter;

    public ProtocolHandler(
            RegisterPlayerUseCase registerPlayer,
            StartGameUseCase startGame,
            PlayerActionUseCase playerAction,
            DealCardsUseCase dealCards,
            DetermineWinnerUseCase determineWinner,
            CreateLobbyUseCase createLobby,
            JoinLobbyUseCase joinLobby,
            GetLeaderboardUseCase getLeaderboard,
            MessageFormatter formatter) {
        this.registerPlayer = registerPlayer;
        this.startGame = startGame;
        this.playerAction = playerAction;
        this.dealCards = dealCards;
        this.determineWinner = determineWinner;
        this.createLobby = createLobby;
        this.joinLobby = joinLobby;
        this.getLeaderboard = getLeaderboard;
        this.formatter = formatter;
    }

    public String handle(String command) {
        if (command == null || command.trim().isEmpty()) {
            return formatter.formatError("Empty command");
        }

        String[] parts = command.trim().split("\\s+");
        String cmd = parts[0].toUpperCase();

        try {
            return switch (cmd) {
                case "REGISTER" -> handleRegister(parts);
                case "START_GAME" -> handleStartGame(parts);
                case "FOLD" -> handleFold(parts);
                case "CHECK" -> handleCheck(parts);
                case "CALL" -> handleCall(parts);
                case "RAISE" -> handleRaise(parts);
                case "ALL_IN" -> handleAllIn(parts);
                case "DEAL_FLOP" -> handleDealFlop(parts);
                case "DEAL_TURN" -> handleDealTurn(parts);
                case "DEAL_RIVER" -> handleDealRiver(parts);
                case "DETERMINE_WINNER" -> handleDetermineWinner(parts);
                case "CREATE_LOBBY" -> handleCreateLobby(parts);
                case "JOIN_LOBBY" -> handleJoinLobby(parts);
                case "LEADERBOARD" -> handleLeaderboard(parts);
                case "HELP" -> handleHelp();
                case "QUIT" -> formatter.formatInfo("Goodbye!");
                default -> formatter.formatError("Unknown command: " + cmd);
            };
        } catch (Exception e) {
            LOGGER.warning("Command error: " + e.getMessage());
            return formatter.formatError(e.getMessage());
        }
    }

    private String handleRegister(String[] parts) {
        if (parts.length < 3) {
            return formatter.formatError("Usage: REGISTER <name> <chips>");
        }
        
        String name = parts[1];
        int chips = Integer.parseInt(parts[2]);
        
        var response = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand(name, chips)
        );
        
        return formatter.formatPlayerRegistered(response);
    }

    private String handleStartGame(String[] parts) {
        if (parts.length < 5) {
            return formatter.formatError("Usage: START_GAME <player1Id> <player2Id> ... <smallBlind> <bigBlind>");
        }
        
        int numPlayers = parts.length - 3;
        List<String> playerIds = List.of(parts).subList(1, numPlayers + 1);
        int smallBlind = Integer.parseInt(parts[numPlayers + 1]);
        int bigBlind = Integer.parseInt(parts[numPlayers + 2]);
        
        var response = startGame.execute(
            new StartGameUseCase.StartGameCommand(playerIds, new Blinds(smallBlind, bigBlind))
        );
        
        return formatter.formatGameStarted(response);
    }

    private String handleFold(String[] parts) {
        if (parts.length < 3) {
            return formatter.formatError("Usage: FOLD <gameId> <playerId>");
        }
        
        return executePlayerAction(parts[1], parts[2], PlayerAction.FOLD, 0);
    }

    private String handleCheck(String[] parts) {
        if (parts.length < 3) {
            return formatter.formatError("Usage: CHECK <gameId> <playerId>");
        }
        
        return executePlayerAction(parts[1], parts[2], PlayerAction.CHECK, 0);
    }

    private String handleCall(String[] parts) {
        if (parts.length < 4) {
            return formatter.formatError("Usage: CALL <gameId> <playerId> <amount>");
        }
        
        int amount = Integer.parseInt(parts[3]);
        return executePlayerAction(parts[1], parts[2], PlayerAction.CALL, amount);
    }

    private String handleRaise(String[] parts) {
        if (parts.length < 4) {
            return formatter.formatError("Usage: RAISE <gameId> <playerId> <amount>");
        }
        
        int amount = Integer.parseInt(parts[3]);
        return executePlayerAction(parts[1], parts[2], PlayerAction.RAISE, amount);
    }

    private String handleAllIn(String[] parts) {
        if (parts.length < 3) {
            return formatter.formatError("Usage: ALL_IN <gameId> <playerId>");
        }
        
        return executePlayerAction(parts[1], parts[2], PlayerAction.ALL_IN, 0);
    }

    private String executePlayerAction(String gameId, String playerId, PlayerAction action, int amount) {
        var response = playerAction.execute(
            new PlayerActionUseCase.PlayerActionCommand(gameId, playerId, action, amount)
        );
        
        return formatter.formatActionExecuted(response);
    }

    private String handleDealFlop(String[] parts) {
        if (parts.length < 2) {
            return formatter.formatError("Usage: DEAL_FLOP <gameId>");
        }
        
        var response = dealCards.dealFlop(new DealCardsUseCase.DealCardsCommand(parts[1]));
        return formatter.formatCardsDealt(response);
    }

    private String handleDealTurn(String[] parts) {
        if (parts.length < 2) {
            return formatter.formatError("Usage: DEAL_TURN <gameId>");
        }
        
        var response = dealCards.dealTurn(new DealCardsUseCase.DealCardsCommand(parts[1]));
        return formatter.formatCardsDealt(response);
    }

    private String handleDealRiver(String[] parts) {
        if (parts.length < 2) {
            return formatter.formatError("Usage: DEAL_RIVER <gameId>");
        }
        
        var response = dealCards.dealRiver(new DealCardsUseCase.DealCardsCommand(parts[1]));
        return formatter.formatCardsDealt(response);
    }

    private String handleDetermineWinner(String[] parts) {
        if (parts.length < 2) {
            return formatter.formatError("Usage: DETERMINE_WINNER <gameId>");
        }
        
        var response = determineWinner.execute(
            new DetermineWinnerUseCase.DetermineWinnerCommand(parts[1])
        );
        
        return formatter.formatWinnerDetermined(response);
    }

    private String handleCreateLobby(String[] parts) {
        if (parts.length < 3) {
            return formatter.formatError("Usage: CREATE_LOBBY <name> <maxPlayers>");
        }
        
        String name = parts[1];
        int maxPlayers = Integer.parseInt(parts[2]);
        
        var response = createLobby.execute(
            new CreateLobbyUseCase.CreateLobbyCommand(name, maxPlayers)
        );
        
        return formatter.formatLobbyCreated(response);
    }

    private String handleJoinLobby(String[] parts) {
        if (parts.length < 3) {
            return formatter.formatError("Usage: JOIN_LOBBY <lobbyId> <playerId>");
        }
        
        var response = joinLobby.execute(
            new JoinLobbyUseCase.JoinLobbyCommand(parts[1], parts[2])
        );
        
        return formatter.formatLobbyJoined(response);
    }

    private String handleLeaderboard(String[] parts) {
        int limit = parts.length > 1 ? Integer.parseInt(parts[1]) : 10;
        
        var response = getLeaderboard.execute(
            new GetLeaderboardUseCase.GetLeaderboardCommand(limit)
        );
        
        return formatter.formatLeaderboard(response);
    }

    private String handleHelp() {
        return """
            Available commands:
            
            Player Management:
              REGISTER <name> <chips>              - Register a new player
              LEADERBOARD [limit]                  - Show top players
            
            Lobby Management:
              CREATE_LOBBY <name> <maxPlayers>     - Create a new lobby
              JOIN_LOBBY <lobbyId> <playerId>      - Join existing lobby
            
            Game Management:
              START_GAME <playerIds...> <sb> <bb>  - Start a new game
              DEAL_FLOP <gameId>                   - Deal flop
              DEAL_TURN <gameId>                   - Deal turn
              DEAL_RIVER <gameId>                  - Deal river
              DETERMINE_WINNER <gameId>            - Determine winner
            
            Player Actions:
              FOLD <gameId> <playerId>             - Fold current hand
              CHECK <gameId> <playerId>            - Check
              CALL <gameId> <playerId> <amount>    - Call bet
              RAISE <gameId> <playerId> <amount>   - Raise bet
              ALL_IN <gameId> <playerId>           - Go all-in
            
            Other:
              HELP                                 - Show this help
              QUIT                                 - Disconnect
            """;
    }
}
