package com.poker.shared.infrastructure.websocket;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.poker.game.application.PlayerActionUseCase.PlayerActionCommand;
import com.poker.game.application.StartGameUseCase.StartGameCommand;
import com.poker.game.application.dto.PlayerActionDTO;
import com.poker.game.application.dto.StartGameDTO;
import com.poker.game.domain.model.Blinds;
import com.poker.lobby.application.CreateLobbyUseCase.CreateLobbyCommand;
import com.poker.lobby.application.JoinLobbyUseCase.JoinLobbyCommand;
import com.poker.lobby.application.LeaveLobbyUseCase.LeaveLobbyCommand;
import com.poker.lobby.application.dto.LobbyDTO;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.player.application.RegisterPlayerUseCase.RegisterPlayerCommand;
import com.poker.player.application.dto.RegisterPlayerDTO;
import com.poker.player.domain.model.PlayerAction;
import com.poker.ranking.application.GetLeaderboardUseCase.GetLeaderboardCommand;
import com.poker.ranking.application.dto.LeaderboardDTO;
import com.poker.shared.application.dto.PokerUseCasesDTO;
import com.poker.shared.application.dto.StartGameRequest;
import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.infrastructure.events.WebSocketEventPublisher;
import com.poker.shared.infrastructure.json.GsonFactory;

import jakarta.websocket.Session;

/**
 * Handles protocol commands and delegates to appropriate use cases.
 * Accepts JSON-based requests for type safety.
 * Command format: { "command": "COMMAND_NAME", "data": {...} }
 */
public class ProtocolHandler {
    private static final Logger LOGGER = Logger.getLogger(ProtocolHandler.class.getName());
    private static final Gson gson = GsonFactory.getInstance();
    private final Instant now = Instant.now();
    
    private final PokerUseCasesDTO pokerUseCases;
    private final WebSocketEventPublisher eventPublisher;

    public ProtocolHandler(PokerUseCasesDTO pokerUseCases, WebSocketEventPublisher eventPublisher) {
        this.pokerUseCases = pokerUseCases;
        this.eventPublisher = eventPublisher;
    }

    public WebSocketResponse<?> handle(String command, Session session) {
        if (isBlank(command)) {
            return WebSocketHelper.errorResponse("Empty command");
        }

        LOGGER.info(String.format("Parsing message: %s", command));

        JsonObject jsonRequest;
        try {
            jsonRequest = gson.fromJson(command, JsonObject.class);
        } catch (JsonSyntaxException e) {
            LOGGER.warning(String.format("Invalid JSON format: %s", e.getMessage()));
            return WebSocketHelper.errorResponse("Invalid JSON format: " + e.getMessage());
        }

        // Validate basic structure
        if (jsonRequest == null || !jsonRequest.has("command")) {
            return WebSocketHelper.errorResponse("Invalid request format. Expected JSON with 'command' field");
        }

        String commandName = jsonRequest.get("command").getAsString();
        LOGGER.info(String.format("Processing command: %s", commandName));

        JsonObject data = extractDataObject(jsonRequest, commandName);
        if (data == null) {
            return WebSocketHelper.errorResponse("Invalid data format. Expected JSON object.");
        }

        LOGGER.info(String.format("Command: %s, Data: %s", commandName, data));

        return routeCommand(commandName, data, session);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private JsonObject extractDataObject(JsonObject request, String commandName) {
        if (!request.has("data")) {
            return new JsonObject(); // empty object as default
        }

        JsonElement dataElement = request.get("data");
        if (!dataElement.isJsonObject()) {
            LOGGER.warning(String.format("Invalid data type for command %s: %s", commandName, dataElement));
            return null;
        }
        return dataElement.getAsJsonObject();
    }

    private WebSocketResponse<?> routeCommand(String commandName, JsonObject data, Session session) {
        WebSocketCommand cmd = WebSocketCommand.fromString(commandName);

        return switch (cmd) {
            case REGISTER       -> handleRegister(data);
            case START_GAME     -> handleStartGame(data);
            case CREATE_LOBBY   -> handleCreateLobby(data, session);
            case JOIN_LOBBY     -> handleJoinLobby(data, session);
            case LEAVE_LOBBY    -> handleLeaveLobby(data);
            case FOLD           -> handleFold(data);
            case CHECK          -> handleCheck(data);
            case CALL           -> handleCall(data);
            case RAISE          -> handleRaise(data);
            case ALL_IN         -> handleAllIn(data);
            case LEADERBOARD    -> handleLeaderboard(data);
            default             -> WebSocketHelper.errorResponse("Unknown command: ");
        };
    }
    
    private WebSocketResponse<RegisterPlayerDTO> handleRegister(JsonObject data) {
        String playerName = data.get("playerName").getAsString();
        int chips = data.has("chips") ? data.get("chips").getAsInt() : 1000;
        
        LOGGER.info(() -> String.format("Registering player: %s with chips: %d", playerName, chips));
        
        RegisterPlayerCommand cmd = new RegisterPlayerCommand(playerName, chips);
        RegisterPlayerDTO dto = pokerUseCases.getRegisterPlayer().execute(cmd);
        
        WebSocketResponse<RegisterPlayerDTO> response = new WebSocketResponse<>(
            EventTypeEnum.PLAYER_REGISTERED, 
            "Player registered successfully",
            true, 
            now,     
            dto
        );
        
        return response;
    }

    private WebSocketResponse<StartGameDTO> handleStartGame(JsonObject data) {
        StartGameRequest request = gson.fromJson(data, StartGameRequest.class);
        
        Blinds blinds = new Blinds(request.getSmallBlind(), request.getBigBlind());
        LobbyId lobbyId = LobbyId.from(request.getLobbyId());
        List<String> playerIds = request.getPlayerIds();

        StartGameCommand command = new StartGameCommand(playerIds, blinds, lobbyId);
        StartGameDTO dto = pokerUseCases.getStartGame().execute(command);
        
        WebSocketResponse<StartGameDTO> response = new WebSocketResponse<>(
            EventTypeEnum.GAME_STARTED,
            "Game started successfully",
            true,
            now,
            dto
        );

        // Add lobbyId to response for broadcasting
        return response;
    }

    private WebSocketResponse<LobbyDTO> handleCreateLobby(JsonObject data, Session session) {

        String lobbyName = data.get("lobbyName").getAsString();
        int maxPlayers = data.get("maxPlayers").getAsInt();
        String playerId = data.get("playerId").getAsString();

        CreateLobbyCommand command = new CreateLobbyCommand(lobbyName, maxPlayers, playerId);
        LobbyDTO dto = pokerUseCases.getCreateLobby().execute(command);
        
        // Infrastructure responsibility: Subscribe admin to lobby
        eventPublisher.subscribe(dto.lobbyId(), session, playerId);
        LOGGER.info(() -> String.format("Subscribed admin player %s to lobby %s", playerId, dto.lobbyId()));
        
        WebSocketResponse<LobbyDTO> response = new WebSocketResponse<>(
            EventTypeEnum.LOBBY_CREATED,
            "Lobby created successfully",
            true,
            now,
            dto
        );

        return response;
    }

    private WebSocketResponse<LobbyDTO> handleJoinLobby(JsonObject data, Session session) {
        String lobbyId = data.get("lobbyId").getAsString();
        String playerId = data.get("playerId").getAsString();

        JoinLobbyCommand command = new JoinLobbyCommand(lobbyId, playerId);
        LobbyDTO dto = pokerUseCases.getJoinLobby().execute(command);
        
        // Infrastructure responsibility: Subscribe player to lobby
        eventPublisher.subscribe(dto.lobbyId(), session, playerId);
        LOGGER.info(() -> String.format("Subscribed player %s to lobby %s", playerId, dto.lobbyId()));
        
        WebSocketResponse<LobbyDTO> response = new WebSocketResponse<>(
            EventTypeEnum.PLAYER_JOINED_LOBBY,
            "Player joined lobby successfully",
            true,
            now,
            dto
        );

        return response;
    }

    private WebSocketResponse<Void> handleLeaveLobby(JsonObject data) {
        String lobbyId = data.get("lobbyId").getAsString();
        String playerId = data.get("playerId").getAsString();
        
        LeaveLobbyCommand command = new LeaveLobbyCommand(lobbyId, playerId);
        pokerUseCases.getLeaveLobby().execute(command);
        
        WebSocketResponse<Void> response = new WebSocketResponse<>(
            EventTypeEnum.PLAYER_LEFT_LOBBY,
            "Player left lobby successfully",
            true,
            now,
            null
        );

        return response;
    }

    private WebSocketResponse<PlayerActionDTO> handleFold(JsonObject data) {
        String gameId = data.get("gameId").getAsString();
        String playerId = data.get("playerId").getAsString();

        return executePlayerAction(
            gameId,
            playerId, 
            PlayerAction.FOLD, 
            0, 
            EventTypeEnum.PLAYER_FOLD, 
            "Player folded successfully"
        );
    }

    private WebSocketResponse<PlayerActionDTO> handleCheck(JsonObject data) {
        String gameId = data.get("gameId").getAsString();
        String playerId = data.get("playerId").getAsString();
        
        return executePlayerAction(
            gameId, 
            playerId, 
            PlayerAction.CHECK, 
            0,
            EventTypeEnum.PLAYER_CHECK,
            "Player checked successfully"
        );
    }

    private WebSocketResponse<PlayerActionDTO> handleCall(JsonObject data) {
        String gameId = data.get("gameId").getAsString();
        String playerId = data.get("playerId").getAsString();
        int amount = data.get("amount").getAsInt();
        
        return executePlayerAction(
            gameId,
            playerId,
            PlayerAction.CALL,
            amount,
            EventTypeEnum.PLAYER_CALL,
            "Player called successfully"
        );
    }

    private WebSocketResponse<PlayerActionDTO> handleRaise(JsonObject data) {
        String gameId = data.get("gameId").getAsString();
        String playerId = data.get("playerId").getAsString();
        int amount = data.get("amount").getAsInt();
        
        return executePlayerAction(
            gameId,
            playerId,
            PlayerAction.RAISE,
            amount,
            EventTypeEnum.PLAYER_RAISE,
            "Player raised successfully"
        );
    }

    private WebSocketResponse<PlayerActionDTO> handleAllIn(JsonObject data) {
        String gameId = data.get("gameId").getAsString();
        String playerId = data.get("playerId").getAsString();
        
        return executePlayerAction(
            gameId, 
            playerId, 
            PlayerAction.ALL_IN, 
            0,
            EventTypeEnum.PLAYER_ALL_IN,
            "Player went all-in successfully"
        );
    }

    private WebSocketResponse<LeaderboardDTO> handleLeaderboard(JsonObject data) {
        int limit = data.has("limit") ? data.get("limit").getAsInt() : 10;

        GetLeaderboardCommand command = new GetLeaderboardCommand(limit);
        LeaderboardDTO dto = pokerUseCases.getGetLeaderboard().execute(command);
        
        WebSocketResponse<LeaderboardDTO> response = new WebSocketResponse<>(
            EventTypeEnum.LEADERBOARD_RETRIEVED,
            "Leaderboard retrieved successfully",
            true,
            now,
            dto
        );
        return response;
    }

    private WebSocketResponse<PlayerActionDTO> executePlayerAction(String gameId, String playerId, PlayerAction action, int amount, EventTypeEnum eventType, String message) {
        PlayerActionCommand command = new PlayerActionCommand(gameId, playerId, action, amount);
        PlayerActionDTO dto = pokerUseCases.getPlayerAction().execute(command);
        
        WebSocketResponse<PlayerActionDTO> response = new WebSocketResponse<>(
            eventType, 
            message,
            true, 
            now, 
            dto
        );
        
        return response;
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
