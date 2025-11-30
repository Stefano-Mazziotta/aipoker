package com.poker.player.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.poker.player.application.dto.RegisterPlayerDTO;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerId;
import com.poker.player.domain.repository.PlayerRepository;

/**
 * Integration tests for RegisterPlayerUseCase.
 * 
 * These tests validate player registration behavior using DTOs,
 * ensuring the use case properly converts domain entities to DTOs
 * while maintaining business rules like unique player names.
 */
class RegisterPlayerUseCaseTest {

    private RegisterPlayerUseCase useCase;
    private InMemoryPlayerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPlayerRepository();
        useCase = new RegisterPlayerUseCase(repository);
    }

    @Test
    void testRegisterPlayer() {
        var command = new RegisterPlayerUseCase.RegisterPlayerCommand("Alice", 1000);

        // Execute returns RegisterPlayerDTO, not domain entity
        RegisterPlayerDTO dto = useCase.execute(command);

        // Assert on DTO fields - test works with DTOs, not domain entities
        assertNotNull(dto.playerId());
        assertEquals("Alice", dto.playerName());
        assertEquals(1000, dto.chips());

        // Verify persistence by querying repository (domain layer check)
        Optional<Player> saved = repository.findById(PlayerId.from(dto.playerId()));
        assertTrue(saved.isPresent());
        assertEquals("Alice", saved.get().getName());
    }

    @Test
    void testRegisterPlayerWithDuplicateNameThrows() {
        useCase.execute(new RegisterPlayerUseCase.RegisterPlayerCommand("Bob", 500));

        // Business rule: duplicate names should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, ()
                -> useCase.execute(new RegisterPlayerUseCase.RegisterPlayerCommand("Bob", 600))
        );
        assertNotNull(exception);
    }

    // Simple in-memory repository for testing
    static class InMemoryPlayerRepository implements PlayerRepository {

        private final Map<PlayerId, Player> players = new HashMap<>();

        @Override
        public void save(Player player) {
            players.put(player.getId(), player);
        }

        @Override
        public Optional<Player> findById(PlayerId id) {
            return Optional.ofNullable(players.get(id));
        }

        @Override
        public Optional<Player> findByName(String name) {
            return players.values().stream()
                    .filter(p -> p.getName().equals(name))
                    .findFirst();
        }

        @Override
        public List<Player> findAll() {
            return new ArrayList<>(players.values());
        }

        @Override
        public boolean exists(PlayerId id) {
            return players.containsKey(id);
        }

        @Override
        public void delete(PlayerId id) {
            players.remove(id);
        }

        @Override
        public List<Player> findTopByChips(int limit) {
            return players.values().stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getChipsAmount(), p1.getChipsAmount()))
                    .limit(limit)
                    .toList();
        }
    }
}
