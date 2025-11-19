package com.poker.player.application;

import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerId;
import com.poker.player.domain.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RegisterPlayerUseCase.
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
        
        var response = useCase.execute(command);
        
        assertNotNull(response.id());
        assertEquals("Alice", response.name());
        assertEquals(1000, response.chips());
        
        // Verify persistence
        Optional<Player> saved = repository.findById(new PlayerId(response.id()));
        assertTrue(saved.isPresent());
        assertEquals("Alice", saved.get().getName());
    }

    @Test
    void testRegisterPlayerWithDuplicateNameThrows() {
        useCase.execute(new RegisterPlayerUseCase.RegisterPlayerCommand("Bob", 500));
        
        assertThrows(IllegalArgumentException.class, () -> 
            useCase.execute(new RegisterPlayerUseCase.RegisterPlayerCommand("Bob", 600))
        );
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
