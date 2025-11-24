# Poker Game Flow Improvements

## Issues Identified from Testing

### 1. **Lobby System Lacks Admin/Owner**
**Problem:** Lobbies don't have an admin player who controls the lobby.
- Anyone can start a game
- No lobby ownership or control

**Solution:**
- Add `adminPlayerId` field to `Lobby` domain model
- Admin is the player who creates the lobby
- Only admin can start the game
- Add `LEAVE_LOBBY` command for players to exit

### 2. **START_GAME Should Orchestrate Full Game Flow**
**Problem:** Currently `START_GAME` only initializes the game in PRE_FLOP state, requiring manual commands for each phase.

**Current Flow (Manual):**
```
START_GAME → PRE_FLOP
DEAL_FLOP → FLOP
DEAL_TURN → TURN
DEAL_RIVER → RIVER
DETERMINE_WINNER → FINISHED
```

**Expected Flow (Automated):**
```
START_GAME should orchestrate:
1. Shuffle deck ✓ (already done)
2. Deal hole cards ✓ (already done)
3. Post blinds ✓ (already done)
4. PRE_FLOP betting round
5. DEAL_FLOP + betting round
6. DEAL_TURN + betting round
7. DEAL_RIVER + betting round
8. SHOWDOWN / DETERMINE_WINNER
9. Return to lobby or start new hand
```

**Changes Needed:**
- Add betting round enforcement between phases
- Prevent manual `DEAL_FLOP/TURN/RIVER` without completing betting
- Add betting round completion detection
- Implement automatic phase transitions

### 3. **Missing Dealer Button Minigame**
**Problem:** No mechanism to determine initial dealer.

**Solution:**
- Add "High Card" minigame when game starts
- Each player gets one card
- Player with highest card becomes first dealer
- For subsequent hands, dealer rotates clockwise

**Implementation:**
```java
// In Game.java
public PlayerId determineInitialDealer() {
    Map<PlayerId, Card> dealerCards = new HashMap<>();
    for (Player player : players) {
        dealerCards.put(player.getId(), deck.dealCard());
    }
    // Find highest card
    return dealerCards.entrySet().stream()
        .max(Comparator.comparing(e -> e.getValue().getRank()))
        .map(Map.Entry::getKey)
        .orElseThrow();
}
```

### 4. **No Game-End Options**
**Problem:** After `DETERMINE_WINNER`, players have no option to:
- Continue playing (new hand)
- Leave the lobby
- View final results

**Solution:**
- Add `READY_FOR_NEXT_HAND` command
- Add `LEAVE_GAME` command
- Add game state management for post-game lobby
- Return players to lobby after game ends

### 5. **No Real-Time Game Notifications**
**Problem:** Players subscribed to a lobby don't receive:
- Game state updates
- Community cards dealt
- Other players' actions
- Pot changes
- Their hole cards

**Solution:**
- Implement observer/pub-sub pattern for game events
- Send notifications to all connected clients in the same game
- Add `GET_GAME_STATE` command for current game info
- Add `GET_MY_CARDS` command to view hole cards

**Events to Broadcast:**
```java
// Game events
- PlayerJoinedLobby
- PlayerLeftLobby
- GameStarted
- CardsDealt (community only)
- PlayerActed (FOLD/CHECK/CALL/RAISE/ALL_IN)
- PotUpdated
- PhaseChanged (FLOP/TURN/RIVER/SHOWDOWN)
- WinnerDetermined
- GameEnded
```

### 6. **Duplicate Cards in Community Cards**
**Problem:** From your test output:
```
Community Cards: THREE♥ FOUR♥ FIVE♥ THREE♥ THREE♥
```
Same card appears 3 times! This indicates the deck isn't properly managing dealt cards.

**Issue:** The `Deck` class may not be removing cards after dealing.

**Solution:** Verify `Deck.dealCard()` removes cards from the deck.

### 7. **Betting Round Enforcement Missing**
**Problem:** Players can deal river without any betting rounds.

**Current Issue:**
```
DEAL_FLOP 1afe0a41-e2c2-4e3f-b6c6-476c0b0945bf
DEAL_TURN 1afe0a41-e2c2-4e3f-b6c6-476c0b0945bf
DEAL_RIVER 1afe0a41-e2c2-4e3f-b6c6-476c0b0945bf
DETERMINE_WINNER 1afe0a41-e2c2-4e3f-b6c6-476c0b0945bf
```

**Solution:**
- Add `bettingRoundComplete` flag to each phase
- Require all players to act before allowing phase transitions
- Implement turn-based player action ordering

### 8. **No Player Order/Turn Management**
**Problem:** No enforcement of player turn order.

**Solution:**
- Add `currentPlayerIndex` to game state
- Enforce that only the current player can act
- Rotate turn after each action
- Skip folded players

## Implementation Priority

### Phase 1: Critical Fixes (Week 1)
1. Fix duplicate cards in deck ⚠️ CRITICAL
2. Add betting round enforcement
3. Add turn-based player ordering

### Phase 2: Core Features (Week 2)
4. Add lobby admin/owner
5. Implement dealer button minigame
6. Add automatic game flow orchestration

### Phase 3: User Experience (Week 3)
7. Implement real-time notifications
8. Add game-end options
9. Add GET_GAME_STATE and GET_MY_CARDS commands

### Phase 4: Polish (Week 4)
10. Add reconnection handling
11. Add spectator mode
12. Add chat functionality

## Recommended Architecture Changes

### 1. Game Event System
```java
// New interface
public interface GameEventListener {
    void onGameEvent(GameEvent event);
}

// In SocketServer
public class ConnectedClientsRegistry {
    private Map<String, Set<ClientHandler>> lobbyClients;
    private Map<String, Set<ClientHandler>> gameClients;
    
    public void broadcastToGame(String gameId, GameEvent event);
    public void broadcastToLobby(String lobbyId, LobbyEvent event);
}
```

### 2. Game State Machine
```java
public class GameFlowController {
    public void progressGame(Game game) {
        switch (game.getState()) {
            case PRE_FLOP -> handlePreFlopBetting(game);
            case FLOP -> handleFlopBetting(game);
            case TURN -> handleTurnBetting(game);
            case RIVER -> handleRiverBetting(game);
            case SHOWDOWN -> determineWinner(game);
        }
    }
    
    private boolean isBettingRoundComplete(Game game) {
        // Check all active players have acted
        // Check all bets are equal (or all-in)
    }
}
```

### 3. Enhanced Commands
```
# New Commands Needed
GET_GAME_STATE <gameId> <playerId>     - Get current game state
GET_MY_CARDS <gameId> <playerId>       - View your hole cards
LEAVE_LOBBY <lobbyId> <playerId>       - Exit a lobby
START_GAME_FROM_LOBBY <lobbyId>        - Start game (admin only)
READY_NEXT_HAND <gameId> <playerId>    - Ready for next hand
LEAVE_GAME <gameId> <playerId>         - Leave current game
```

## Testing Checklist

- [ ] Deck never deals duplicate cards
- [ ] Betting round must complete before phase change
- [ ] Only current player can act
- [ ] Turn rotates correctly
- [ ] Dealer button rotates after each hand
- [ ] Players receive notifications for all game events
- [ ] Players can see their hole cards
- [ ] Players can leave lobby/game
- [ ] Admin can start game from lobby
- [ ] Game returns to lobby after completion

## Database Schema Changes

```sql
-- Add admin to lobbies table
ALTER TABLE lobbies ADD COLUMN admin_player_id TEXT;

-- Add game events table for event sourcing (optional)
CREATE TABLE game_events (
    id TEXT PRIMARY KEY,
    game_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    event_data TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add connected clients table
CREATE TABLE connected_clients (
    session_id TEXT PRIMARY KEY,
    player_id TEXT NOT NULL,
    game_id TEXT,
    lobby_id TEXT,
    connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Notes

- Consider using WebSockets instead of raw TCP for easier real-time updates
- Consider adding a simple web UI for better visualization
- Add comprehensive integration tests for full game flows
- Add timeout handling for inactive players
