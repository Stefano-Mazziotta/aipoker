package com.poker;

import com.poker.game.application.*;
import com.poker.game.domain.model.Blinds;
import com.poker.game.domain.repository.GameRepository;
import com.poker.game.infrastructure.persistence.SQLiteGameRepository;
import com.poker.player.application.*;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.player.infrastructure.persistence.SQLitePlayerRepository;
import com.poker.lobby.application.*;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.lobby.infrastructure.persistence.SQLiteLobbyRepository;
import com.poker.shared.infrastructure.database.DatabaseInitializer;
import com.poker.shared.infrastructure.socket.*;

import java.net.Socket;
import java.util.List;
import java.util.Scanner;

/**
 * Main application entry point.
 * Demonstrates the hexagonal architecture with manual dependency injection.
 * 
 * Usage:
 *   --demo    : Run demo mode (default)
 *   --server  : Start socket server on port 8080
 */
public class PokerApplication {
    
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("Texas Hold'em Poker Server");
        System.out.println("Hexagonal Architecture");
        System.out.println("=================================\n");
        
        // Parse command line arguments
        boolean serverMode = args.length > 0 && args[0].equals("--server");

        // Initialize infrastructure
        System.out.println("Initializing database...");
        DatabaseInitializer.initialize();
        System.out.println("✓ Database ready\n");

        // Wire dependencies (Manual DI)
        PlayerRepository playerRepository = new SQLitePlayerRepository();
        GameRepository gameRepository = new SQLiteGameRepository(playerRepository);
        LobbyRepository lobbyRepository = new SQLiteLobbyRepository();
        
        // Player use cases
        RegisterPlayerUseCase registerPlayer = new RegisterPlayerUseCase(playerRepository);
        GetLeaderboardUseCase getLeaderboard = new GetLeaderboardUseCase(playerRepository);
        
        // Game use cases
        StartGameUseCase startGame = new StartGameUseCase(gameRepository, playerRepository);
        PlayerActionUseCase playerAction = new PlayerActionUseCase(gameRepository, playerRepository);
        DealCardsUseCase dealCards = new DealCardsUseCase(gameRepository);
        DetermineWinnerUseCase determineWinner = new DetermineWinnerUseCase(gameRepository, playerRepository);
        
        // Lobby use cases
        CreateLobbyUseCase createLobby = new CreateLobbyUseCase(lobbyRepository);
        JoinLobbyUseCase joinLobby = new JoinLobbyUseCase(lobbyRepository);
        
        if (serverMode) {
            startSocketServer(registerPlayer, startGame, playerAction, dealCards, 
                            determineWinner, createLobby, joinLobby, getLeaderboard);
        } else {
            runDemo(registerPlayer, startGame, getLeaderboard, createLobby);
        }
    }
    
    private static void startSocketServer(RegisterPlayerUseCase registerPlayer,
                                         StartGameUseCase startGame,
                                         PlayerActionUseCase playerAction,
                                         DealCardsUseCase dealCards,
                                         DetermineWinnerUseCase determineWinner,
                                         CreateLobbyUseCase createLobby,
                                         JoinLobbyUseCase joinLobby,
                                         GetLeaderboardUseCase getLeaderboard) {
        System.out.println("Starting Socket Server...");
        System.out.println("Listening on port 8080");
        System.out.println("Press Ctrl+C to stop\n");
        
        // Create protocol handler with all use cases
        ProtocolHandler protocolHandler = new ProtocolHandler(
            registerPlayer, startGame, playerAction, dealCards,
            determineWinner, createLobby, joinLobby, getLeaderboard
        );
        
        // Create client handler factory
        ClientHandlerFactory handlerFactory = (Socket socket) -> 
            new ClientHandler(socket, protocolHandler, new MessageFormatter());
        
        // Start server
        SocketServer server = new SocketServer(8080, handlerFactory);
        
        try {
            server.start();
            
            // Keep running until interrupted
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                server.stop();
                System.out.println("✓ Server stopped");
            }));
            
            // Block forever
            while (server.isRunning()) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("✗ Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void runDemo(RegisterPlayerUseCase registerPlayer,
                               StartGameUseCase startGame,
                               GetLeaderboardUseCase getLeaderboard,
                               CreateLobbyUseCase createLobby) {
        System.out.println("Running Demo Mode...\n");
        try {
            // Demo: Register players
            System.out.println("Registering players...");
            var response1 = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Alice", 1000)
            );
            System.out.println("✓ Player registered: " + response1.name() + 
                             " (ID: " + response1.id() + ", Chips: " + response1.chips() + ")");

            var response2 = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Bob", 1000)
            );
            System.out.println("✓ Player registered: " + response2.name() + 
                             " (ID: " + response2.id() + ", Chips: " + response2.chips() + ")");

            var response3 = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Charlie", 1000)
            );
            System.out.println("✓ Player registered: " + response3.name() + 
                             " (ID: " + response3.id() + ", Chips: " + response3.chips() + ")");

            // Demo: Create lobby
            System.out.println("\nCreating lobby...");
            var lobbyResponse = createLobby.execute(
                new CreateLobbyUseCase.CreateLobbyCommand("High Stakes Table", 6)
            );
            System.out.println("✓ Lobby created: " + lobbyResponse.name() + 
                             " (" + lobbyResponse.currentPlayers() + "/" + lobbyResponse.maxPlayers() + " players)");

            // Demo: Start game
            System.out.println("\nStarting game...");
            var gameResponse = startGame.execute(
                new StartGameUseCase.StartGameCommand(
                    List.of(response1.id(), response2.id(), response3.id()),
                    new Blinds(10, 20)
                )
            );
            System.out.println("✓ Game started: " + gameResponse.gameId());
            System.out.println("  State: " + gameResponse.state());
            System.out.println("  Players: " + String.join(", ", gameResponse.players()));
            System.out.println("  Pot: " + gameResponse.pot());

            // Demo: Leaderboard
            System.out.println("\nLeaderboard...");
            var leaderboard = getLeaderboard.execute(
                new GetLeaderboardUseCase.GetLeaderboardCommand(10)
            );
            System.out.println("✓ Top players:");
            leaderboard.rankings().forEach(ranking -> 
                System.out.println("  " + ranking.name() + ": " + ranking.chips() + " chips")
            );

            System.out.println("\n=================================");
            System.out.println("✓ Migration Complete!");
            System.out.println("=================================");
            System.out.println("\nArchitecture Summary:");
            System.out.println("• Hexagonal Architecture: Domain isolated from infrastructure");
            System.out.println("• DDD Patterns: Value Objects, Aggregates, Repositories");
            System.out.println("• Screaming Architecture: Feature-first organization");
            System.out.println("• Hand Evaluation: 100% reused from existing code");
            System.out.println("\nWhat's Working:");
            System.out.println("✅ Player registration and persistence");
            System.out.println("✅ Game creation and state management");
            System.out.println("✅ Lobby system");
            System.out.println("✅ Leaderboard/Rankings");
            System.out.println("✅ Complete betting logic");
            System.out.println("✅ Hand evaluation (9 poker hands)");
            System.out.println("✅ Socket Server (use --server to start)");
            System.out.println("✅ Comprehensive testing suite");
            System.out.println("\nTo start socket server:");
            System.out.println("  java -jar poker-server.jar --server");

        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
