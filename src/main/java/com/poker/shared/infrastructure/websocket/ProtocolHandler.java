package com.poker.shared.infrastructure.websocket;

import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import com.poker.shared.infrastructure.websocket.dto.StartGameRequest;

/**
 * Handles protocol commands and delegates to appropriate use cases.
 * Accepts JSON-based requests for type safety.
 * Command format: { "command": "COMMAND_NAME", "data": {...} }
 */
public class ProtocolHandler {
    private static final Logger LOGGER = Logger.getLogger(ProtocolHandler.class.getName());
    private static final Gson gson = new Gson();
    
    private final RegisterPlayerUseCase registerPlayer;
    private final StartGameUseCase startGame;
    private final PlayerActionUseCase playerAction;
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
            CreateLobbyUseCase createLobby,
            JoinLobbyUseCase joinLobby,
            LeaveLobbyUseCase leaveLobby,
            GetLeaderboardUseCase getLeaderboard,
            GetPlayerCardsUseCase getPlayerCards,
            GetGameStateUseCase getGameState) {
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

    public WebSocketResponse<?> handle(String command) {
        if (command == null || command.trim().isEmpty()) {
            return WebSocketResponse.error("Empty command");
        }

        // Parse JSON request
        try {
            JsonObject jsonRequest = gson.fromJson(command, JsonObject.class);
            if (jsonRequest == null || !jsonRequest.has("command")) {
                return WebSocketResponse.error("Invalid request format. Expected JSON with 'command' field");
            }
            
            String commandName = jsonRequest.get("command").getAsString();
            JsonObject data = jsonRequest.has("data") ? jsonRequest.get("data").getAsJsonObject() : new JsonObject();
            
            WebSocketCommand cmd = WebSocketCommand.fromString(commandName);

            return switch (cmd) {
                case REGISTER -> handleRegister(data);
                case START_GAME -> handleStartGame(data);
                case CREATE_LOBBY -> handleCreateLobby(data);
                case JOIN_LOBBY -> handleJoinLobby(data);
                case LEAVE_LOBBY -> handleLeaveLobby(data);
                case FOLD -> handleFold(data);
                case CHECK -> handleCheck(data);
                case CALL -> handleCall(data);
                case RAISE -> handleRaise(data);
                case ALL_IN -> handleAllIn(data);
                case GET_GAME_STATE -> handleGetGameState(data);
                case GET_MY_CARDS -> handleGetMyCards(data);
                case LEADERBOARD -> handleLeaderboard(data);
                case HELP -> WebSocketResponse.info(getHelpText());
                case QUIT -> WebSocketResponse.successMessage("Goodbye!");
                default -> WebSocketResponse.error("Unknown command: " + commandName);
            };
        } catch (Exception e) {
            LOGGER.warning(() -> String.format("Command error: %s", e.getMessage()));
            return WebSocketResponse.error(e.getMessage());
        }
    }
    
    private WebSocketResponse<?> handleRegister(JsonObject data) {
        String playerName = data.get("playerName").getAsString();
        // Default chips to 1000 if not provided
        int chips = data.has("chips") ? data.get("chips").getAsInt() : 1000;
        
        var response = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand(playerName, chips)
        );
        
        return WebSocketResponse.success("PLAYER_REGISTERED", response);
    }

    private WebSocketResponse<?> handleStartGame(JsonObject data) {
        StartGameRequest request = gson.fromJson(data, StartGameRequest.class);
        
        var response = startGame.execute(
            new StartGameUseCase.StartGameCommand(request.getPlayerIds(), 
                new Blinds(request.getSmallBlind(), request.getBigBlind()))
        );
        
        // Add lobbyId to response for broadcasting
        return WebSocketResponse.successWithLobby("GAME_STARTED", response, request.getLobbyId());
    }

    private WebSocketResponse<?> handleCreateLobby(JsonObject data) {
        String playerId = data.get("playerId").getAsString();
        int maxPlayers = data.get("maxPlayers").getAsInt();
        // Generate a default lobby name if not provided
        String lobbyName = data.has("name") ? data.get("name").getAsString() : "Lobby-" + System.currentTimeMillis();
        
        var response = createLobby.execute(
            new CreateLobbyUseCase.CreateLobbyCommand(lobbyName, maxPlayers, playerId)
        );
        
        return WebSocketResponse.success("LOBBY_CREATED", response);
    }

    private WebSocketResponse<?> handleJoinLobby(JsonObject data) {
        String lobbyId = data.get("lobbyId").getAsString();
        String playerId = data.get("playerId").getAsString();
        
        var response = joinLobby.execute(
            new JoinLobbyUseCase.JoinLobbyCommand(lobbyId, playerId)
        );
        
        return WebSocketResponse.success("LOBBY_JOINED", response);
    }

    private WebSocketResponse<?> handleLeaveLobby(JsonObject data) {
        String lobbyId = data.get("lobbyId").getAsString();
        String playerId = data.get("playerId").getAsString();
        
        leaveLobby.execute(
            new LeaveLobbyUseCase.LeaveLobbyCommand(lobbyId, playerId)
        );
        
        return WebSocketResponse.successMessage("Successfully left lobby");
    }

    private WebSocketResponse<?> handleFold(JsonObject data) {
        String gameId = data.get("gameId").getAsString();
        String playerId = data.get("playerId").getAsString();
        return executePlayerAction(gameId, playerId, PlayerAction.FOLD, 0);
    }

    private WebSocketResponse<?> handleCheck(JsonObject data) {
        String gameId = data.get("gameId").getAsString();
        String playerId = data.get("playerId").getAsString();
        return executePlayerAction(gameId, playerId, PlayerAction.CHECK, 0);
    }

    private WebSocketResponse<?> handleCall(JsonObject data) {
        String gameId = data.get("gameId").getAsString();
        String playerId = data.get("playerId").getAsString();
        int amount = data.get("amount").getAsInt();
        return executePlayerAction(gameId, playerId, PlayerAction.CALL, amount);
    }

    private WebSocketResponse<?> handleRaise(JsonObject data) {
        String gameId = data.get("gameId").getAsString();
        String playerId = data.get("playerId").getAsString();
        int amount = data.get("amount").getAsInt();
        return executePlayerAction(gameId, playerId, PlayerAction.RAISE, amount);
    }

    private WebSocketResponse<?> handleAllIn(JsonObject data) {
        String gameId = data.get("gameId").getAsString();
        String playerId = data.get("playerId").getAsString();
        return executePlayerAction(gameId, playerId, PlayerAction.ALL_IN, 0);
    }

    private WebSocketResponse<?> handleGetGameState(JsonObject data) {
        String gameId = data.get("gameId").getAsString();
        
        var response = getGameState.execute(
            new GameStateCommand(gameId)
        );
        
        return WebSocketResponse.success("GAME_STATE", response);
    }

    private WebSocketResponse<?> handleGetMyCards(JsonObject data) {
        String gameId = data.get("gameId").getAsString();
        String playerId = data.get("playerId").getAsString();
        
        var response = getPlayerCards.execute(
            new PlayerCardsCommand(gameId, playerId)
        );
        
        return WebSocketResponse.success("PLAYER_CARDS", response);
    }

    private WebSocketResponse<?> handleLeaderboard(JsonObject data) {
        int limit = data.has("limit") ? data.get("limit").getAsInt() : 10;
        
        var response = getLeaderboard.execute(
            new GetLeaderboardUseCase.GetLeaderboardCommand(limit)
        );
        
        return WebSocketResponse.success("LEADERBOARD", response);
    }

    private WebSocketResponse<?> executePlayerAction(String gameId, String playerId, PlayerAction action, int amount) {
        var response = playerAction.execute(
            new PlayerActionUseCase.PlayerActionCommand(gameId, playerId, action, amount)
        );
        
        return WebSocketResponse.success("PLAYER_ACTION", response);
    }

    private String getHelpText() {
        return """
            Available commands (JSON format):
            
            All commands should be sent as JSON: {"command": "COMMAND_NAME", "data": {...}}
            
            Player Management:
              REGISTER - data: {name, chips}
              LEADERBOARD - data: {limit?}
            
            Lobby Management:
              CREATE_LOBBY - data: {name, maxPlayers, adminPlayerId}
              JOIN_LOBBY - data: {lobbyId, playerId}
              LEAVE_LOBBY - data: {lobbyId, playerId}
            
            Game Management:
              START_GAME - data: {lobbyId, playerIds[], smallBlind, bigBlind}
              GET_GAME_STATE - data: {gameId}
            
            Player Actions:
              GET_MY_CARDS - data: {gameId, playerId}
              FOLD - data: {gameId, playerId}
              CHECK - data: {gameId, playerId}
              CALL - data: {gameId, playerId, amount}
              RAISE - data: {gameId, playerId, amount}
              ALL_IN - data: {gameId, playerId}
            
            Other:
              HELP - Show this help
              QUIT - Disconnect
            """;
    }
}
