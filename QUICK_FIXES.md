# Quick Fixes for Immediate Testing

## 1. Fix Duplicate Cards Issue

### Investigation
The duplicate cards `THREE♥ FOUR♥ FIVE♥ THREE♥ THREE♥` might be caused by:
- Game being restored from database with stale deck state
- Multiple games sharing the same deck instance
- Deck not being properly serialized/deserialized

### Quick Fix
Ensure each game always creates a fresh deck:
```java
// In Game.reconstitute() method
// Always create a new shuffled deck when reconstituting from DB
game.deck = new Deck();
game.deck.shuffle();
// Remove already dealt cards
int cardsToDeal = communityCards.size() + (players.size() * 2);
for (int i = 0; i < cardsToDeal + (game.state.ordinal()); i++) {
    if (!game.deck.isEmpty()) {
        game.deck.dealCard(); // Remove cards to sync state
    }
}
```

## 2. Add Betting Round Validation

### Modify DealCardsUseCase
Add betting round check before dealing:

```java
public CardsResponse dealFlop(DealCardsCommand command) {
    Game game = loadGame(command.gameId());
    
    // VALIDATION: Check if pre-flop betting is complete
    if (!isBettingRoundComplete(game)) {
        throw new IllegalStateException(
            "Cannot deal flop: Pre-flop betting round not complete"
        );
    }
    
    game.dealFlop();
    gameRepository.save(game);
    return createResponse(game);
}

private boolean isBettingRoundComplete(Game game) {
    // For now, simple check: all active players must have acted
    // In full implementation, check all bets are equal
    List<Player> activePlayers = game.getCurrentRound()
        .getActivePlayers();
    
    // Minimum: at least one action per active player
    // Better: track player actions and verify all players acted
    return activePlayers.size() <= 1; // If only 1 player, round is over
}
```

## 3. Add Lobby Admin

### Update Lobby.java
```java
public class Lobby {
    private final LobbyId id;
    private final String name;
    private final List<PlayerId> players;
    private final int maxPlayers;
    private PlayerId adminPlayerId; // NEW FIELD
    private boolean started;

    public Lobby(LobbyId id, String name, int maxPlayers, PlayerId adminPlayerId) {
        this.id = id;
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.adminPlayerId = adminPlayerId;
        this.players = new ArrayList<>();
        this.players.add(adminPlayerId); // Admin auto-joins
        this.started = false;
    }

    public static Lobby create(String name, int maxPlayers, PlayerId adminPlayerId) {
        return new Lobby(LobbyId.generate(), name, maxPlayers, adminPlayerId);
    }

    public boolean isAdmin(PlayerId playerId) {
        return adminPlayerId.equals(playerId);
    }

    public void start(PlayerId requestingPlayerId) {
        if (!isAdmin(requestingPlayerId)) {
            throw new IllegalStateException("Only admin can start the game");
        }
        if (players.size() < 2) {
            throw new IllegalStateException("Need at least 2 players to start");
        }
        this.started = true;
    }

    public PlayerId getAdminPlayerId() {
        return adminPlayerId;
    }
}
```

### Update CreateLobbyUseCase
```java
public LobbyResponse execute(CreateLobbyCommand command) {
    if (command.maxPlayers() < 2 || command.maxPlayers() > 9) {
        throw new IllegalArgumentException("Max players must be between 2 and 9");
    }

    PlayerId adminId = PlayerId.from(command.adminPlayerId());
    Lobby lobby = Lobby.create(command.name(), command.maxPlayers(), adminId);
    
    lobbyRepository.save(lobby);

    return new LobbyResponse(
        lobby.getId().getValue(),
        lobby.getName(),
        lobby.getPlayers().size(),
        lobby.getMaxPlayers(),
        lobby.isOpen(),
        adminId.getValue().toString() // Include admin in response
    );
}

public record CreateLobbyCommand(
    String name, 
    int maxPlayers, 
    String adminPlayerId // NEW PARAMETER
) {}

public record LobbyResponse(
    String lobbyId,
    String name,
    int currentPlayers,
    int maxPlayers,
    boolean isOpen,
    String adminPlayerId // NEW FIELD
) {}
```

### Update ProtocolHandler
```java
private String handleCreateLobby(String[] parts) {
    if (parts.length < 4) {
        return formatter.formatError(
            "Usage: CREATE_LOBBY <name> <maxPlayers> <adminPlayerId>"
        );
    }
    
    String name = parts[1];
    int maxPlayers = Integer.parseInt(parts[2]);
    String adminPlayerId = parts[3];
    
    var response = createLobby.execute(
        new CreateLobbyUseCase.CreateLobbyCommand(name, maxPlayers, adminPlayerId)
    );
    
    return formatter.formatLobbyCreated(response);
}
```

## 4. Add Player Cards Visibility

### New Use Case: GetPlayerCardsUseCase
```java
package com.poker.game.application;

public class GetPlayerCardsUseCase {
    private final GameRepository gameRepository;

    public GetPlayerCardsUseCase(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public PlayerCardsResponse execute(PlayerCardsCommand command) {
        Game game = gameRepository.findById(GameId.from(command.gameId()))
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        Player player = game.getPlayers().stream()
            .filter(p -> p.getId().getValue().toString().equals(command.playerId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Player not in game"));

        List<Card> holeCards = player.getHand().getCards();
        String cardsStr = holeCards.stream()
            .map(card -> card.getRank().name() + card.getSuit().getSymbol())
            .reduce((a, b) -> a + " " + b)
            .orElse("No cards");

        return new PlayerCardsResponse(
            player.getName(),
            cardsStr,
            holeCards.size()
        );
    }

    public record PlayerCardsCommand(String gameId, String playerId) {}
    
    public record PlayerCardsResponse(
        String playerName,
        String cards,
        int cardCount
    ) {}
}
```

### Add to ProtocolHandler
```java
private String handleGetMyCards(String[] parts) {
    if (parts.length < 3) {
        return formatter.formatError("Usage: GET_MY_CARDS <gameId> <playerId>");
    }
    
    var response = getPlayerCards.execute(
        new GetPlayerCardsUseCase.PlayerCardsCommand(parts[1], parts[2])
    );
    
    return formatter.formatPlayerCards(response);
}
```

### Add to MessageFormatter
```java
public String formatPlayerCards(GetPlayerCardsUseCase.PlayerCardsResponse response) {
    return """
        SUCCESS: Your cards
        Player: %s
        Cards: %s
        Count: %d
        """.formatted(response.playerName(), response.cards(), response.cardCount());
}
```

## 5. Update HELP Command

```java
private String handleHelp() {
    return """
        Available commands:
        
        Player Management:
          REGISTER <name> <chips>                          - Register a new player
          LEADERBOARD [limit]                              - Show top players
        
        Lobby Management:
          CREATE_LOBBY <name> <maxPlayers> <adminPlayerId> - Create a new lobby
          JOIN_LOBBY <lobbyId> <playerId>                  - Join existing lobby
          LEAVE_LOBBY <lobbyId> <playerId>                 - Leave lobby
        
        Game Management:
          START_GAME <playerIds...> <sb> <bb>              - Start a new game
          GET_MY_CARDS <gameId> <playerId>                 - View your hole cards
          GET_GAME_STATE <gameId>                          - View current game state
          DEAL_FLOP <gameId>                               - Deal flop (auto after betting)
          DEAL_TURN <gameId>                               - Deal turn (auto after betting)
          DEAL_RIVER <gameId>                              - Deal river (auto after betting)
          DETERMINE_WINNER <gameId>                        - Determine winner
        
        Player Actions:
          FOLD <gameId> <playerId>                         - Fold current hand
          CHECK <gameId> <playerId>                        - Check
          CALL <gameId> <playerId> <amount>                - Call bet
          RAISE <gameId> <playerId> <amount>               - Raise bet
          ALL_IN <gameId> <playerId>                       - Go all-in
        
        Other:
          HELP                                             - Show this help
          QUIT                                             - Disconnect
        """;
}
```

## Testing the Fixes

### Test Scenario 1: Lobby with Admin
```bash
# Terminal 1 (Admin)
telnet localhost 8081
REGISTER alice 1000
CREATE_LOBBY my-game 4 <alice-id>
# Wait for others to join
START_GAME ...

# Terminal 2 (Player)
telnet localhost 8081
REGISTER bob 1000
JOIN_LOBBY <lobby-id> <bob-id>
```

### Test Scenario 2: View Cards
```bash
START_GAME <player1> <player2> 10 20
GET_MY_CARDS <game-id> <player1-id>
# Should show your 2 hole cards
```

### Test Scenario 3: Betting Required
```bash
START_GAME <player1> <player2> 10 20
# Try to deal flop without betting
DEAL_FLOP <game-id>
# Should error: "Pre-flop betting round not complete"

# Complete betting first
CALL <game-id> <player1-id> 20
CALL <game-id> <player2-id> 20

# Now flop should work
DEAL_FLOP <game-id>
```

## Database Migration

Run this SQL to add admin field:
```sql
ALTER TABLE lobbies ADD COLUMN admin_player_id TEXT;
UPDATE lobbies SET admin_player_id = (
    SELECT player_id FROM lobby_players WHERE lobby_id = lobbies.id LIMIT 1
) WHERE admin_player_id IS NULL;
```

## Next Steps

1. Apply these quick fixes
2. Test thoroughly with multiple clients
3. Add proper turn management
4. Implement automatic game flow
5. Add real-time notifications (bigger change)
