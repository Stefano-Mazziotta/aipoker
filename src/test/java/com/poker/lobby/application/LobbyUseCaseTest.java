package com.poker.lobby.application;

import com.poker.lobby.domain.model.*;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.lobby.infrastructure.persistence.SQLiteLobbyRepository;
import com.poker.player.application.RegisterPlayerUseCase;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.player.infrastructure.persistence.SQLitePlayerRepository;
import com.poker.shared.infrastructure.database.DatabaseInitializer;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests for lobby use cases.
 */
public class LobbyUseCaseTest {
    
    private static LobbyRepository lobbyRepository;
    private static PlayerRepository playerRepository;
    private static CreateLobbyUseCase createLobby;
    private static JoinLobbyUseCase joinLobby;
    private static RegisterPlayerUseCase registerPlayer;

    @BeforeClass
    public static void setup() {
        DatabaseInitializer.initialize();
        
        lobbyRepository = new SQLiteLobbyRepository();
        playerRepository = new SQLitePlayerRepository();
        
        createLobby = new CreateLobbyUseCase(lobbyRepository);
        joinLobby = new JoinLobbyUseCase(lobbyRepository);
        registerPlayer = new RegisterPlayerUseCase(playerRepository);
    }

    @Test
    public void testCreateLobby() {
        var response = createLobby.execute(
            new CreateLobbyUseCase.CreateLobbyCommand("Test Lobby", 6)
        );

        assertNotNull(response.lobbyId());
        assertEquals("Test Lobby", response.name());
        assertEquals(6, response.maxPlayers());
        assertEquals(0, response.currentPlayers());
        assertTrue(response.isOpen());
        
        System.out.println("✓ Create lobby test passed!");
    }

    @Test
    public void testJoinLobby() {
        // Create lobby
        var lobby = createLobby.execute(
            new CreateLobbyUseCase.CreateLobbyCommand("Join Test", 4)
        );

        // Register player
        var player = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand("TestPlayer", 1000)
        );

        // Join lobby
        var response = joinLobby.execute(
            new JoinLobbyUseCase.JoinLobbyCommand(lobby.lobbyId(), player.id())
        );

        assertEquals(lobby.lobbyId(), response.lobbyId());
        assertEquals(1, response.currentPlayers());
        assertTrue(response.isOpen());
        
        System.out.println("✓ Join lobby test passed!");
    }

    @Test(expected = IllegalStateException.class)
    public void testLobbyFull() {
        // Create small lobby
        var lobby = createLobby.execute(
            new CreateLobbyUseCase.CreateLobbyCommand("Full Test", 2)
        );

        // Register 3 players
        var p1 = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand("Player1", 1000)
        );
        var p2 = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand("Player2", 1000)
        );
        var p3 = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand("Player3", 1000)
        );

        // Join with 2 players
        joinLobby.execute(new JoinLobbyUseCase.JoinLobbyCommand(lobby.lobbyId(), p1.id()));
        joinLobby.execute(new JoinLobbyUseCase.JoinLobbyCommand(lobby.lobbyId(), p2.id()));

        // This should throw exception
        joinLobby.execute(new JoinLobbyUseCase.JoinLobbyCommand(lobby.lobbyId(), p3.id()));
    }
}
