package com.poker.shared.infrastructure.websocket;

import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.poker.game.application.GetGameStateUseCase.GameStateCommand;
import com.poker.game.application.PlayerActionUseCase.PlayerActionCommand;
import com.poker.game.application.StartGameUseCase.StartGameCommand;
import com.poker.game.domain.model.Blinds;
import com.poker.lobby.application.CreateLobbyUseCase.CreateLobbyCommand;
import com.poker.lobby.application.JoinLobbyUseCase.JoinLobbyCommand;
import com.poker.lobby.application.LeaveLobbyUseCase.LeaveLobbyCommand;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.player.application.GetLeaderboardUseCase.GetLeaderboardCommand;
import com.poker.player.application.RegisterPlayerUseCase.RegisterPlayerCommand;
import com.poker.player.domain.model.PlayerAction;
import com.poker.shared.application.dto.PokerUseCasesDTO;
import com.poker.shared.application.dto.StartGameRequest;

/**
 * Handles protocol commands and delegates to appropriate use cases.
 * Accepts JSON-based requests for type safety.
 * Command format: { "command": "COMMAND_NAME", "data": {...} }
 */
public class ProtocolHandler {
    private static final Logger LOGGER = Logger.getLogger(ProtocolHandler.class.getName());
    private static final Gson gson = new Gson();
    private final PokerUseCasesDTO pokerUseCases;

    public ProtocolHandler(PokerUseCasesDTO pokerUseCases) {
        this.pokerUseCases = pokerUseCases;
    }

    public WebSocketResponse<?> handle(String command) {
        if (command == null || command.trim().isEmpty()) {
            return WebSocketResponse.error("Empty command");
        }

        // Parse JSON request
        try {
            LOGGER.info(() -> "Parsing message: " + command);
            
            JsonObject jsonRequest = gson.fromJson(command, JsonObject.class);
            if (jsonRequest == null || !jsonRequest.has("command")) {
                return WebSocketResponse.error("Invalid request format. Expected JSON with 'command' field");
            }
            
            String commandName = jsonRequest.get("command").getAsString();
            LOGGER.info(() -> "Processing command: " + commandName);
            
            // Extract data field - must be a JSON object
            JsonObject data;
            if (jsonRequest.has("data")) {
                var dataElement = jsonRequest.get("data");
                if (!dataElement.isJsonObject()) {
                    LOGGER.warning(() -> "Invalid data type for command " + commandName + ": " + dataElement);
                    return WebSocketResponse.error("Invalid data format. Expected JSON object.");
                }
                data = dataElement.getAsJsonObject();
            } else {
                data = new JsonObject();
            }
            
            LOGGER.info(() -> String.format("Command: %s, Data: %s", commandName, data));
            
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
                // case GET_MY_CARDS -> handleGetMyCards(data);
                case LEADERBOARD -> handleLeaderboard(data);
                case QUIT -> WebSocketResponse.successMessage("Goodbye!");
                default -> WebSocketResponse.error("Unknown command: " + commandName);
            };
        } catch (JsonSyntaxException exception) {
            LOGGER.warning(() -> String.format("Command error: %s", exception.getMessage()));
            return WebSocketResponse.error(exception.getMessage());
        }
    }
    
    private WebSocketResponse<?> handleRegister(JsonObject data) {
        String playerName = data.get("playerName").getAsString();
        int chips = data.has("chips") ? data.get("chips").getAsInt() : 1000;
        
        LOGGER.info(() -> String.format("Registering player: %s with chips: %d", playerName, chips));
        
        RegisterPlayerCommand cmd = new RegisterPlayerCommand(playerName, chips);
        var response = pokerUseCases.getRegisterPlayer().execute(cmd);
        
        return WebSocketResponse.success("PLAYER_REGISTERED", response);
    }

    private WebSocketResponse<?> handleStartGame(JsonObject data) {
        StartGameRequest request = gson.fromJson(data, StartGameRequest.class);
        
        Blinds blinds = new Blinds(request.getSmallBlind(), request.getBigBlind());
        LobbyId lobbyId = LobbyId.from(request.getLobbyId());
        List<String> playerIds = request.getPlayerIds();

        StartGameCommand command = new StartGameCommand(playerIds, blinds, lobbyId);
        var response = pokerUseCases.getStartGame().execute(command);
        
        // Add lobbyId to response for broadcasting
        return WebSocketResponse.successWithLobby("GAME_STARTED", response, request.getLobbyId());
    }

    private WebSocketResponse<?> handleCreateLobby(JsonObject data) {
        String playerId = data.get("playerId").getAsString();
        int maxPlayers = data.get("maxPlayers").getAsInt();

        String lobbyName = data.has("name") 
            ? data.get("name").getAsString() 
            : "Lobby-" + System.currentTimeMillis();

        CreateLobbyCommand command = new CreateLobbyCommand(lobbyName, maxPlayers, playerId);
        var response = pokerUseCases.getCreateLobby().execute(command);
        
        return WebSocketResponse.success("LOBBY_CREATED", response);
    }

    private WebSocketResponse<?> handleJoinLobby(JsonObject data) {
        String lobbyId = data.get("lobbyId").getAsString();
        String playerId = data.get("playerId").getAsString();

        JoinLobbyCommand command = new JoinLobbyCommand(lobbyId, playerId);
        var response = pokerUseCases.getJoinLobby().execute(command);
        
        return WebSocketResponse.success("LOBBY_JOINED", response);
    }

    private WebSocketResponse<?> handleLeaveLobby(JsonObject data) {
        String lobbyId = data.get("lobbyId").getAsString();
        String playerId = data.get("playerId").getAsString();
        
        LeaveLobbyCommand command = new LeaveLobbyCommand(lobbyId, playerId);
        pokerUseCases.getLeaveLobby().execute(command);
        
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
        GameStateCommand command = new GameStateCommand(gameId);
        var response = pokerUseCases.getGetGameState().execute(command);
        
        return WebSocketResponse.success("GAME_STATE", response);
    }

    private WebSocketResponse<?> handleLeaderboard(JsonObject data) {
        int limit = data.has("limit") ? data.get("limit").getAsInt() : 10;

        GetLeaderboardCommand command = new GetLeaderboardCommand(limit);
        var response = pokerUseCases.getGetLeaderboard().execute(command);
        
        return WebSocketResponse.success("LEADERBOARD", response);
    }

    private WebSocketResponse<?> executePlayerAction(String gameId, String playerId, PlayerAction action, int amount) {
        PlayerActionCommand command = new PlayerActionCommand(gameId, playerId, action, amount);
        
        var response = pokerUseCases.getPlayerAction().execute(command);
        
        return WebSocketResponse.success("PLAYER_ACTION", response);
    }

    // private WebSocketResponse<?> handleGetMyCards(JsonObject data) {
    //     String gameId = data.get("gameId").getAsString();
    //     String playerId = data.get("playerId").getAsString();
        
    //     PlayerCardsCommand command = new PlayerCardsCommand(gameId, playerId);

    //     // var response = pokerUseCases..execute(command);
    //     var response = "";

    //     return WebSocketResponse.success("PLAYER_CARDS", response);
    // }

}
