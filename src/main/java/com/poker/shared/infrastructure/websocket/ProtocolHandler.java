package com.poker.shared.infrastructure.websocket;

import java.util.List;
import java.util.logging.Logger;

import com.poker.game.application.DealCardsUseCase;
import com.poker.game.application.DetermineWinnerUseCase;
import com.poker.game.application.GetGameStateUseCase;
import com.poker.game.application.GetGameStateUseCase.GameStateCommand;
import com.poker.game.application.GetPlayerCardsUseCase;
import com.poker.game.application.GetPlayerCardsUseCase.PlayerCardsCommand;
import com.poker.game.application.PlayerActionUseCase;
import com.poker.game.application.StartGameUseCase;
import com.poker.game.domain.model.Blinds;
import com.poker.lobby.application.CreateLobbyUseCase;
import com.poker.lobby.application.JoinLobbyUseCase;
import com.poker.lobby.application.LeaveLobbyUseCase;
import com.poker.player.application.GetLeaderboardUseCase;
import com.poker.player.application.RegisterPlayerUseCase;
import com.poker.player.domain.model.PlayerAction;

/**
 * Handles protocol commands and delegates to appropriate use cases.
 * Returns WebSocketResponse objects that will be serialized to JSON.
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
    private final LeaveLobbyUseCase leaveLobby;
    private final GetLeaderboardUseCase getLeaderboard;
    private final GetPlayerCardsUseCase getPlayerCards;
    private final GetGameStateUseCase getGameState;

    public ProtocolHandler(
            RegisterPlayerUseCase registerPlayer,
            StartGameUseCase startGame,
            PlayerActionUseCase playerAction,
            DealCardsUseCase dealCards,
            DetermineWinnerUseCase determineWinner,
            CreateLobbyUseCase createLobby,
            JoinLobbyUseCase joinLobby,
            LeaveLobbyUseCase leaveLobby,
            GetLeaderboardUseCase getLeaderboard,
            GetPlayerCardsUseCase getPlayerCards,
            GetGameStateUseCase getGameState) {
        this.registerPlayer = registerPlayer;
        this.startGame = startGame;
        this.playerAction = playerAction;
        this.dealCards = dealCards;
        this.determineWinner = determineWinner;
        this.createLobby = createLobby;
        this.joinLobby = joinLobby;
        this.leaveLobby = leaveLobby;
        this.getLeaderboard = getLeaderboard;
        this.getPlayerCards = getPlayerCards;
        this.getGameState = getGameState;
    }

    public WebSocketResponse<?> handle(String command) {
        if (command == null || command.trim().isEmpty()) {
            return WebSocketResponse.error("Empty command");
        }

        String[] parts = command.trim().split("\\s+");
        WebSocketCommand cmd = WebSocketCommand.fromString(parts[0]);

        try {
            return switch (cmd) {
                case REGISTER -> handleRegister(parts);
                case START_GAME -> handleStartGame(parts);
                case FOLD -> handleFold(parts);
                case CHECK -> handleCheck(parts);
                case CALL -> handleCall(parts);
                case RAISE -> handleRaise(parts);
                case ALL_IN -> handleAllIn(parts);
                case DEAL_FLOP -> handleDealFlop(parts);
                case DEAL_TURN -> handleDealTurn(parts);
                case DEAL_RIVER -> handleDealRiver(parts);
                case DETERMINE_WINNER -> handleDetermineWinner(parts);
                case CREATE_LOBBY -> handleCreateLobby(parts);
                case JOIN_LOBBY -> handleJoinLobby(parts);
                case LEAVE_LOBBY -> handleLeaveLobby(parts);
                case LEADERBOARD -> handleLeaderboard(parts);
                case GET_MY_CARDS -> handleGetMyCards(parts);
                case GET_GAME_STATE -> handleGetGameState(parts);
                case HELP -> WebSocketResponse.info(getHelpText());
                case QUIT -> WebSocketResponse.successMessage("Goodbye!");
                default -> WebSocketResponse.error("Unknown command: " + parts[0]);
            };
        } catch (Exception e) {
            LOGGER.warning(() -> String.format("Command error: %s", e.getMessage()));
            return WebSocketResponse.error(e.getMessage());
        }
    }

    private WebSocketResponse<?> handleRegister(String[] parts) {
        if (parts.length < 3) {
            return WebSocketResponse.error("Usage: REGISTER <name> <chips>");
        }
        
        String name = parts[1];
        int chips = Integer.parseInt(parts[2]);
        
        var response = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand(name, chips)
        );
        
        return WebSocketResponse.success("PLAYER_REGISTERED", response);
    }

    private WebSocketResponse<?> handleStartGame(String[] parts) {
        if (parts.length < 5) {
            return WebSocketResponse.error("Usage: START_GAME <player1Id> <player2Id> ... <smallBlind> <bigBlind>");
        }
        
        int numPlayers = parts.length - 3;
        List<String> playerIds = List.of(parts).subList(1, numPlayers + 1);
        int smallBlind = Integer.parseInt(parts[numPlayers + 1]);
        int bigBlind = Integer.parseInt(parts[numPlayers + 2]);
        
        var response = startGame.execute(
            new StartGameUseCase.StartGameCommand(playerIds, new Blinds(smallBlind, bigBlind))
        );
        
        return WebSocketResponse.success("GAME_STARTED", response);
    }

    private WebSocketResponse<?> handleFold(String[] parts) {
        if (parts.length < 3) {
            return WebSocketResponse.error("Usage: FOLD <gameId> <playerId>");
        }
        
        return executePlayerAction(parts[1], parts[2], PlayerAction.FOLD, 0);
    }

    private WebSocketResponse<?> handleCheck(String[] parts) {
        if (parts.length < 3) {
            return WebSocketResponse.error("Usage: CHECK <gameId> <playerId>");
        }
        
        return executePlayerAction(parts[1], parts[2], PlayerAction.CHECK, 0);
    }

    private WebSocketResponse<?> handleCall(String[] parts) {
        if (parts.length < 4) {
            return WebSocketResponse.error("Usage: CALL <gameId> <playerId> <amount>");
        }
        
        int amount = Integer.parseInt(parts[3]);
        return executePlayerAction(parts[1], parts[2], PlayerAction.CALL, amount);
    }

    private WebSocketResponse<?> handleRaise(String[] parts) {
        if (parts.length < 4) {
            return WebSocketResponse.error("Usage: RAISE <gameId> <playerId> <amount>");
        }
        
        int amount = Integer.parseInt(parts[3]);
        return executePlayerAction(parts[1], parts[2], PlayerAction.RAISE, amount);
    }

    private WebSocketResponse<?> handleAllIn(String[] parts) {
        if (parts.length < 3) {
            return WebSocketResponse.error("Usage: ALL_IN <gameId> <playerId>");
        }
        
        return executePlayerAction(parts[1], parts[2], PlayerAction.ALL_IN, 0);
    }

    private WebSocketResponse<?> executePlayerAction(String gameId, String playerId, PlayerAction action, int amount) {
        var response = playerAction.execute(
            new PlayerActionUseCase.PlayerActionCommand(gameId, playerId, action, amount)
        );
        
        return WebSocketResponse.success("PLAYER_ACTION", response);
    }

    private WebSocketResponse<?> handleDealFlop(String[] parts) {
        if (parts.length < 2) {
            return WebSocketResponse.error("Usage: DEAL_FLOP <gameId>");
        }
        
        var response = dealCards.dealFlop(new DealCardsUseCase.DealCardsCommand(parts[1]));
        return WebSocketResponse.success("FLOP_DEALT", response);
    }

    private WebSocketResponse<?> handleDealTurn(String[] parts) {
        if (parts.length < 2) {
            return WebSocketResponse.error("Usage: DEAL_TURN <gameId>");
        }
        
        var response = dealCards.dealTurn(new DealCardsUseCase.DealCardsCommand(parts[1]));
        return WebSocketResponse.success("TURN_DEALT", response);
    }

    private WebSocketResponse<?> handleDealRiver(String[] parts) {
        if (parts.length < 2) {
            return WebSocketResponse.error("Usage: DEAL_RIVER <gameId>");
        }
        
        var response = dealCards.dealRiver(new DealCardsUseCase.DealCardsCommand(parts[1]));
        return WebSocketResponse.success("RIVER_DEALT", response);
    }

    private WebSocketResponse<?> handleDetermineWinner(String[] parts) {
        if (parts.length < 2) {
            return WebSocketResponse.error("Usage: DETERMINE_WINNER <gameId>");
        }
        
        var response = determineWinner.execute(
            new DetermineWinnerUseCase.DetermineWinnerCommand(parts[1])
        );
        
        return WebSocketResponse.success("WINNER_DETERMINED", response);
    }

    private WebSocketResponse<?> handleCreateLobby(String[] parts) {
        if (parts.length < 4) {
            return WebSocketResponse.error("Usage: CREATE_LOBBY <name> <maxPlayers> <adminPlayerId>");
        }
        
        String name = parts[1];
        int maxPlayers = Integer.parseInt(parts[2]);
        String adminPlayerId = parts[3];
        
        var response = createLobby.execute(
            new CreateLobbyUseCase.CreateLobbyCommand(name, maxPlayers, adminPlayerId)
        );
        
        return WebSocketResponse.success("LOBBY_CREATED", response);
    }

    private WebSocketResponse<?> handleJoinLobby(String[] parts) {
        if (parts.length < 3) {
            return WebSocketResponse.error("Usage: JOIN_LOBBY <lobbyId> <playerId>");
        }
        
        var response = joinLobby.execute(
            new JoinLobbyUseCase.JoinLobbyCommand(parts[1], parts[2])
        );
        
        return WebSocketResponse.success("LOBBY_JOINED", response);
    }

    private WebSocketResponse<?> handleLeaveLobby(String[] parts) {
        if (parts.length < 3) {
            return WebSocketResponse.error("Usage: LEAVE_LOBBY <lobbyId> <playerId>");
        }
        
        leaveLobby.execute(
            new LeaveLobbyUseCase.LeaveLobbyCommand(parts[1], parts[2])
        );
        
        return WebSocketResponse.successMessage("Successfully left lobby");
    }

    private WebSocketResponse<?> handleLeaderboard(String[] parts) {
        int limit = parts.length > 1 ? Integer.parseInt(parts[1]) : 10;
        
        var response = getLeaderboard.execute(
            new GetLeaderboardUseCase.GetLeaderboardCommand(limit)
        );
        
        return WebSocketResponse.success("LEADERBOARD", response);
    }

    private WebSocketResponse<?> handleGetMyCards(String[] parts) {
        if (parts.length < 3) {
            return WebSocketResponse.error("Usage: GET_MY_CARDS <gameId> <playerId>");
        }
        
        var response = getPlayerCards.execute(
            new PlayerCardsCommand(parts[1], parts[2])
        );
        
        return WebSocketResponse.success("PLAYER_CARDS", response);
    }

    private WebSocketResponse<?> handleGetGameState(String[] parts) {
        if (parts.length < 2) {
            return WebSocketResponse.error("Usage: GET_GAME_STATE <gameId>");
        }
        
        var response = getGameState.execute(
            new GameStateCommand(parts[1])
        );
        
        return WebSocketResponse.success("GAME_STATE", response);
    }

    private String getHelpText() {
        return """
            Available commands:
            
            Player Management:
              REGISTER <name> <chips>                      - Register a new player
              LEADERBOARD [limit]                          - Show top players
            
            Lobby Management:
              CREATE_LOBBY <name> <maxPlayers> <adminId>   - Create a new lobby
              JOIN_LOBBY <lobbyId> <playerId>              - Join existing lobby
            
            Game Management:
              START_GAME <playerIds...> <sb> <bb>          - Start a new game (admin only)
              GET_GAME_STATE <gameId>                      - View current game state
              DEAL_FLOP <gameId>                           - Deal flop (after betting round)
              DEAL_TURN <gameId>                           - Deal turn (after betting round)
              DEAL_RIVER <gameId>                          - Deal river (after betting round)
              DETERMINE_WINNER <gameId>                    - Determine winner
            
            Player Actions:
              GET_MY_CARDS <gameId> <playerId>             - View your hole cards
              FOLD <gameId> <playerId>                     - Fold current hand
              CHECK <gameId> <playerId>                    - Check
              CALL <gameId> <playerId> <amount>            - Call bet
              RAISE <gameId> <playerId> <amount>           - Raise bet
              ALL_IN <gameId> <playerId>                   - Go all-in
            
            Other:
              HELP                                         - Show this help
              QUIT                                         - Disconnect
            """;
    }
}
