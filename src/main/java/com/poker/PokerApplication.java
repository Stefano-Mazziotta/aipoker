package com.poker;

import com.poker.game.application.GetGameStateUseCase;
import com.poker.game.application.GetPlayerCardsUseCase;
import com.poker.game.application.PlayerActionUseCase;
import com.poker.game.application.StartGameUseCase;
import com.poker.game.domain.repository.GameRepository;
import com.poker.game.infrastructure.persistence.SQLiteGameRepository;
import com.poker.lobby.application.CreateLobbyUseCase;
import com.poker.lobby.application.JoinLobbyUseCase;
import com.poker.lobby.application.LeaveLobbyUseCase;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.lobby.infrastructure.persistence.SQLiteLobbyRepository;
import com.poker.player.application.GetLeaderboardUseCase;
import com.poker.player.application.RegisterPlayerUseCase;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.player.infrastructure.persistence.SQLitePlayerRepository;
import com.poker.shared.application.dto.PokerUseCasesDTO;
import com.poker.shared.domain.events.DomainEventPublisher;
import com.poker.shared.infrastructure.database.DatabaseInitializer;
import com.poker.shared.infrastructure.events.WebSocketEventPublisher;
import com.poker.shared.infrastructure.websocket.PokerWebSocketEndpoint;
import com.poker.shared.infrastructure.websocket.ProtocolHandler;
import com.poker.shared.infrastructure.websocket.WebSocketServer;

/**
 * Main application entry point. Start WebSocket server.
 * Demonstrates 
 * - Hexagonal Architecture 
 * - Scream Architecture
 * - Domain Driven Design
 * - Event Driven Design
 * - SOLID Principles
 * 
 */
public class PokerApplication {
    
    public static void main(String[] args) {
        System.out.println("Texas Hold'em Poker Server - Taller de programación 3 - 2025");        

        // Initialize infrastructure
        System.out.println("Initializing database...");
        DatabaseInitializer.initialize();
        System.out.println("✓ Database ready\n");

        // Wire dependencies (Manual DI)
        PlayerRepository playerRepository = new SQLitePlayerRepository();
        GameRepository gameRepository = new SQLiteGameRepository(playerRepository);
        LobbyRepository lobbyRepository = new SQLiteLobbyRepository();
        
        // Event publisher (infrastructure adapter for domain events)
        DomainEventPublisher eventPublisher = WebSocketEventPublisher.getInstance();
        
        // Player use cases
        RegisterPlayerUseCase registerPlayer = new RegisterPlayerUseCase(playerRepository);
        GetLeaderboardUseCase getLeaderboard = new GetLeaderboardUseCase(playerRepository);
        
        // Game use cases (now with event publisher injected)
        StartGameUseCase startGame = new StartGameUseCase(gameRepository, playerRepository, eventPublisher);
        PlayerActionUseCase playerAction = new PlayerActionUseCase(gameRepository, eventPublisher);
        GetPlayerCardsUseCase getPlayerCards = new GetPlayerCardsUseCase(gameRepository);
        GetGameStateUseCase getGameState = new GetGameStateUseCase(gameRepository);
        
        // Lobby use cases
        CreateLobbyUseCase createLobby = new CreateLobbyUseCase(lobbyRepository, playerRepository);
        JoinLobbyUseCase joinLobby = new JoinLobbyUseCase(lobbyRepository, playerRepository, eventPublisher);
        LeaveLobbyUseCase leaveLobby = new LeaveLobbyUseCase(lobbyRepository, playerRepository, eventPublisher);
        
        PokerUseCasesDTO dto = new PokerUseCasesDTO(
            registerPlayer, 
            startGame,
            playerAction,
            createLobby,
            joinLobby,
            leaveLobby,
            getLeaderboard,
            getPlayerCards,
            getGameState
        );

        startWebSocketServer(dto);
    }
    
    private static void startWebSocketServer(PokerUseCasesDTO dto) {
        System.out.println("Starting WebSocket Server...");
        System.out.println("Listening on ws://localhost:8081/ws/poker");
        System.out.println("Press Ctrl+C to stop\n");
        
        // Create protocol handler with all use cases (using JSON protocol)
        ProtocolHandler protocolHandler = new ProtocolHandler(dto);
        
        // Configure WebSocket endpoint with handler
        PokerWebSocketEndpoint.setProtocolHandler(protocolHandler);
        
        // Start WebSocket server
        WebSocketServer server = new WebSocketServer("localhost", 8081);
        
        try {
            server.start();
            
            // Keep running until interrupted
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                server.stop();
                System.out.println("✓ Server stopped");
            }));
            
            // Keep main thread alive
            server.awaitTermination();
            
        } catch (RuntimeException e) {
            System.err.println("✗ Server runtime error: " + e.getMessage());
        }
    }
}
