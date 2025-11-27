package com.poker.lobby.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.poker.lobby.application.dto.LobbyDTO;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.lobby.infrastructure.persistence.SQLiteLobbyRepository;
import com.poker.player.application.RegisterPlayerUseCase;
import com.poker.player.application.dto.RegisterPlayerDTO;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.player.infrastructure.persistence.SQLitePlayerRepository;
import com.poker.shared.domain.events.NoOpEventPublisher;
import com.poker.shared.infrastructure.database.DatabaseInitializer;

/**
 * Tests for lobby use cases.
 * 
 * These tests validate the behavior of the application layer use cases,
 * working exclusively with DTOs to decouple tests from domain implementation details.
 * This approach ensures tests remain stable even when domain models evolve.
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

        createLobby = new CreateLobbyUseCase(lobbyRepository, playerRepository);
        joinLobby = new JoinLobbyUseCase(lobbyRepository, playerRepository, new NoOpEventPublisher());
        registerPlayer = new RegisterPlayerUseCase(playerRepository);
    }

    @Test
    void testCreateLobby() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Register admin player - receives RegisterPlayerDTO
        RegisterPlayerDTO admin = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Admin" + timestamp, 1000)
        );
        
        // Create lobby - receives LobbyDTO
        LobbyDTO lobbyDTO = createLobby.execute(
                new CreateLobbyUseCase.CreateLobbyCommand("Test Lobby" + timestamp, 6, admin.id())
        );

        // Assert on DTO fields - tests work with DTOs, not domain entities
        assertNotNull(lobbyDTO.lobbyId());
        assertEquals("Test Lobby" + timestamp, lobbyDTO.name());
        assertEquals(6, lobbyDTO.maxPlayers());
        assertEquals(1, lobbyDTO.currentPlayers()); // Admin auto-joins
        assertTrue(lobbyDTO.isOpen());
        assertEquals(admin.id(), lobbyDTO.adminPlayerId());

        System.out.println("✓ Create lobby test passed!");
    }

    @Test
    void testJoinLobby() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Register admin player - receives RegisterPlayerDTO
        RegisterPlayerDTO admin = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Admin" + timestamp, 1000)
        );
        
        // Create lobby - receives LobbyDTO
        LobbyDTO lobbyDTO = createLobby.execute(
                new CreateLobbyUseCase.CreateLobbyCommand("Join Test" + timestamp, 4, admin.id())
        );

        // Register player - receives RegisterPlayerDTO
        RegisterPlayerDTO player = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("TestPlayer" + timestamp, 1000)
        );

        // Join lobby - receives LobbyDTO
        LobbyDTO joinedLobbyDTO = joinLobby.execute(
                new JoinLobbyUseCase.JoinLobbyCommand(lobbyDTO.lobbyId(), player.id())
        );

        // Assert on DTO fields
        assertEquals(lobbyDTO.lobbyId(), joinedLobbyDTO.lobbyId());
        assertEquals(2, joinedLobbyDTO.currentPlayers()); // Admin + player
        assertTrue(joinedLobbyDTO.isOpen());

        System.out.println("✓ Join lobby test passed!");
    }

    @Test
    void testLobbyFull() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Register admin player - receives RegisterPlayerDTO
        RegisterPlayerDTO admin = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Admin" + timestamp, 1000)
        );
        
        // Create small lobby (2 players max) - receives LobbyDTO
        LobbyDTO lobbyDTO = createLobby.execute(
                new CreateLobbyUseCase.CreateLobbyCommand("Full Test" + timestamp, 2, admin.id())
        );

        // Register 2 more players with unique names - receives RegisterPlayerDTO
        RegisterPlayerDTO p2 = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Player2" + timestamp, 1000)
        );
        RegisterPlayerDTO p3 = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Player3" + timestamp, 1000)
        );

        // Join with 1 player (admin already in) - receives LobbyDTO
        joinLobby.execute(new JoinLobbyUseCase.JoinLobbyCommand(lobbyDTO.lobbyId(), p2.id()));

        // Now lobby is full (admin + p2), this should throw exception
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            joinLobby.execute(new JoinLobbyUseCase.JoinLobbyCommand(lobbyDTO.lobbyId(), p3.id()));
        });
        assertNotNull(exception);
    }
}
