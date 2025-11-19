package com.poker.shared.infrastructure.socket;

import com.poker.game.application.*;
import com.poker.lobby.application.*;
import com.poker.player.application.*;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.game.domain.repository.GameRepository;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.player.infrastructure.persistence.SQLitePlayerRepository;
import com.poker.game.infrastructure.persistence.SQLiteGameRepository;
import com.poker.lobby.infrastructure.persistence.SQLiteLobbyRepository;
import com.poker.shared.infrastructure.database.DatabaseInitializer;
import org.junit.*;
import static org.junit.Assert.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Integration tests for Socket Server.
 */
public class SocketServerTest {
    
    private static SocketServer server;
    private static ExecutorService executor;
    private static final int TEST_PORT = 8181;

    @BeforeClass
    public static void startServer() throws Exception {
        DatabaseInitializer.initialize();
        
        PlayerRepository playerRepo = new SQLitePlayerRepository();
        GameRepository gameRepo = new SQLiteGameRepository(playerRepo);
        LobbyRepository lobbyRepo = new SQLiteLobbyRepository();
        
        RegisterPlayerUseCase registerPlayer = new RegisterPlayerUseCase(playerRepo);
        StartGameUseCase startGame = new StartGameUseCase(gameRepo, playerRepo);
        PlayerActionUseCase playerAction = new PlayerActionUseCase(gameRepo);
        DealCardsUseCase dealCards = new DealCardsUseCase(gameRepo);
        DetermineWinnerUseCase determineWinner = new DetermineWinnerUseCase(gameRepo, playerRepo);
        CreateLobbyUseCase createLobby = new CreateLobbyUseCase(lobbyRepo);
        JoinLobbyUseCase joinLobby = new JoinLobbyUseCase(lobbyRepo);
        GetLeaderboardUseCase getLeaderboard = new GetLeaderboardUseCase(playerRepo);
        
        server = new SocketServer(
            TEST_PORT,
            registerPlayer,
            startGame,
            playerAction,
            dealCards,
            determineWinner,
            createLobby,
            joinLobby,
            getLeaderboard
        );
        
        // Start server in background
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        // Wait for server to start
        Thread.sleep(1000);
    }

    @AfterClass
    public static void stopServer() {
        if (server != null) {
            server.stop();
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    public void testServerConnection() throws IOException {
        try (Socket socket = new Socket("localhost", TEST_PORT)) {
            assertTrue(socket.isConnected());
            assertFalse(socket.isClosed());
            
            System.out.println("✓ Server connection test passed!");
        }
    }

    @Test
    public void testRegisterPlayerCommand() throws IOException {
        try (Socket socket = new Socket("localhost", TEST_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            // Send register command
            out.println("REGISTER TestPlayer 1000");
            
            // Read response
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("REGISTERED") || response.contains("SUCCESS"));
            
            System.out.println("✓ Register player command test passed!");
            System.out.println("  Response: " + response);
        }
    }

    @Test
    public void testCreateLobbyCommand() throws IOException {
        try (Socket socket = new Socket("localhost", TEST_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            // Send create lobby command
            out.println("CREATE_LOBBY TestLobby 6");
            
            // Read response
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("LOBBY_CREATED") || response.contains("SUCCESS"));
            
            System.out.println("✓ Create lobby command test passed!");
            System.out.println("  Response: " + response);
        }
    }

    @Test
    public void testMultipleClients() throws Exception {
        ExecutorService clients = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        
        for (int i = 0; i < 3; i++) {
            final int clientId = i;
            clients.submit(() -> {
                try (Socket socket = new Socket("localhost", TEST_PORT);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    
                    out.println("REGISTER Client" + clientId + " 1000");
                    String response = in.readLine();
                    assertNotNull(response);
                    
                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        clients.shutdown();
        
        System.out.println("✓ Multiple clients test passed!");
    }

    @Test
    public void testInvalidCommand() throws IOException {
        try (Socket socket = new Socket("localhost", TEST_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            // Send invalid command
            out.println("INVALID_COMMAND test");
            
            // Read response
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("ERROR") || response.contains("INVALID"));
            
            System.out.println("✓ Invalid command test passed!");
            System.out.println("  Response: " + response);
        }
    }
}
