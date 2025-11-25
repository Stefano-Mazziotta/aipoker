package com.poker.lobby.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.lobby.infrastructure.persistence.SQLiteLobbyRepository;
import com.poker.player.application.RegisterPlayerUseCase;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.player.infrastructure.persistence.SQLitePlayerRepository;
import com.poker.shared.domain.events.NoOpEventPublisher;
import com.poker.shared.infrastructure.database.DatabaseInitializer;

/**
 * Tests for lobby use cases.
 */
public class LobbyUseCaseTest {

    private static LobbyRepository lobbyRepository;
    private static PlayerRepository playerRepository;
    private static CreateLobbyUseCase createLobby;
    private static JoinLobbyUseCase joinLobby;
    private static RegisterPlayerUseCase registerPlayer;

    @BeforeAll
    static void setup() {
        DatabaseInitializer.initialize();

        lobbyRepository = new SQLiteLobbyRepository();
        playerRepository = new SQLitePlayerRepository();

        createLobby = new CreateLobbyUseCase(lobbyRepository);
        joinLobby = new JoinLobbyUseCase(lobbyRepository, playerRepository, new NoOpEventPublisher());
        registerPlayer = new RegisterPlayerUseCase(playerRepository);
    }

    @Test
    void testCreateLobby() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Register admin player
        var admin = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Admin" + timestamp, 1000)
        );
        
        var response = createLobby.execute(
                new CreateLobbyUseCase.CreateLobbyCommand("Test Lobby" + timestamp, 6, admin.id())
        );

        assertNotNull(response.lobbyId());
        assertEquals("Test Lobby" + timestamp, response.name());
        assertEquals(6, response.maxPlayers());
        assertEquals(1, response.currentPlayers()); // Admin auto-joins
        assertTrue(response.isOpen());
        assertEquals(admin.id(), response.adminPlayerId());

        System.out.println("✓ Create lobby test passed!");
    }

    @Test
    void testJoinLobby() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Register admin player
        var admin = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Admin" + timestamp, 1000)
        );
        
        // Create lobby
        var lobby = createLobby.execute(
                new CreateLobbyUseCase.CreateLobbyCommand("Join Test" + timestamp, 4, admin.id())
        );

        // Register player
        var player = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("TestPlayer" + timestamp, 1000)
        );

        // Join lobby
        var response = joinLobby.execute(
                new JoinLobbyUseCase.JoinLobbyCommand(lobby.lobbyId(), player.id())
        );

        assertEquals(lobby.lobbyId(), response.lobbyId());
        assertEquals(2, response.currentPlayers()); // Admin + player
        assertTrue(response.isOpen());

        System.out.println("✓ Join lobby test passed!");
    }

    @Test
    void testLobbyFull() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Register admin player
        var admin = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Admin" + timestamp, 1000)
        );
        
        // Create small lobby (2 players max)
        var lobby = createLobby.execute(
                new CreateLobbyUseCase.CreateLobbyCommand("Full Test" + timestamp, 2, admin.id())
        );

        // Register 2 more players with unique names
        var p2 = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Player2" + timestamp, 1000)
        );
        var p3 = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Player3" + timestamp, 1000)
        );

        // Join with 1 player (admin already in)
        joinLobby.execute(new JoinLobbyUseCase.JoinLobbyCommand(lobby.lobbyId(), p2.id()));

        // Now lobby is full (admin + p2), this should throw exception
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            joinLobby.execute(new JoinLobbyUseCase.JoinLobbyCommand(lobby.lobbyId(), p3.id()));
        });
        assertNotNull(exception);
    }
}
