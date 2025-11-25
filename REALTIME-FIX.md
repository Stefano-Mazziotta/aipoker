# Real-Time Event Propagation Fix

## Problem
When a player joined a lobby, other players in the same lobby didn't receive notifications. There was no real-time communication for lobby events.

## Root Cause
The backend had the event publishing infrastructure in place (`WebSocketEventPublisher`), but the `JoinLobbyUseCase` wasn't using it to publish events when players joined.

## Solution Overview
1. **Created Domain Event**: Added `PlayerJoinedLobbyEvent` class
2. **Updated Backend Use Case**: Modified `JoinLobbyUseCase` to publish events
3. **Added WebSocket Subscriptions**: Updated HTML client to subscribe to lobby events
4. **Implemented Event Handlers**: Added handlers for real-time lobby updates

## Changes Made

### Backend Changes

#### 1. Created `PlayerJoinedLobbyEvent.java`
```java
src/main/java/com/poker/lobby/domain/events/PlayerJoinedLobbyEvent.java
```
- Extends `DomainEvent` 
- Contains: lobbyId, playerId, currentPlayerCount, maxPlayers
- Event type: "PLAYER_JOINED_LOBBY"

#### 2. Updated `JoinLobbyUseCase.java`
- Injected `DomainEventPublisher` in constructor
- After adding player to lobby, publishes `PlayerJoinedLobbyEvent`
- Uses `publishToScope(lobbyId, event)` to send to all lobby subscribers

#### 3. Updated `PokerApplication.java`
- Modified JoinLobbyUseCase instantiation to pass `eventPublisher`

#### 4. Updated `LobbyUseCaseTest.java`
- Fixed test constructor to include `NoOpEventPublisher`

### Frontend Changes (poker-game-client.html)

#### 1. Added Lobby Subscriptions
When creating a lobby:
```javascript
sendCommand(`SUBSCRIBE_LOBBY ${lobbyId} ${playerId}`);
```

When joining a lobby:
```javascript
sendCommand(`SUBSCRIBE_LOBBY ${lobbyId} ${playerId}`);
```

#### 2. Enhanced Message Handler
- Added support for `eventType` field (from domain events)
- Added handler for `PLAYER_JOINED_LOBBY` event
- Added handlers for other domain events: `GAME_STATE_CHANGED`, `CARDS_DEALT`, `PLAYER_ACTION`, `WINNER_DETERMINED`

#### 3. New Event Handler Functions
```javascript
handlePlayerJoinedLobby(message)  // Shows notification when player joins
handleGameStateChanged(message)    // Game state updates
handleCardsDealt(message)          // Card dealing events
handlePlayerAction(message)        // Player action events
handleWinnerDetermined(message)    // Game winner events
```

## How It Works

### Event Flow:
1. **Player 1** creates lobby → subscribes to lobby events
2. **Player 2** joins lobby → triggers event in backend
3. **Backend** (`JoinLobbyUseCase`):
   - Adds player to lobby
   - Saves lobby
   - Publishes `PlayerJoinedLobbyEvent` to lobby scope
4. **WebSocketEventPublisher**:
   - Finds all sessions subscribed to that lobbyId
   - Sends JSON event to all subscribers
5. **All clients** in lobby receive:
   ```json
   {
     "eventId": "...",
     "eventType": "PLAYER_JOINED_LOBBY",
     "timestamp": "...",
     "data": {
       "lobbyId": "...",
       "playerId": "...",
       "currentPlayerCount": 2,
       "maxPlayers": 6
     }
   }
   ```
6. **Frontend** `handlePlayerJoinedLobby()`:
   - Shows notification: "👤 New player joined lobby (2/6 players)"
   - Updates lobby player count (if UI element exists)

## Testing

### Test with 2 Browser Tabs:

**Tab 1 (Admin):**
1. Open `poker-game-client.html`
2. Register as "Player1"
3. Click "CREATE LOBBY"
4. Copy Lobby ID
5. **Wait and watch** - you should see notification when Player2 joins!

**Tab 2 (Player):**
1. Open `poker-game-client.html` in new tab
2. Register as "Player2"  
3. Click "JOIN LOBBY"
4. Paste Lobby ID
5. Join lobby

**Expected Result:**
- Tab 1 sees: "👤 New player joined lobby (2/6 players)"
- Tab 2 sees: "🏠 Joined lobby - Waiting for admin to start..."
- Real-time notification appears immediately when Player2 joins

## Architecture Pattern

This follows the **Event-Driven Architecture** pattern:

```
Domain Layer (Use Case)
    ↓
  [Event Published]
    ↓
Infrastructure Layer (WebSocketEventPublisher)
    ↓
  [Broadcast to WebSocket Sessions]
    ↓
All Subscribed Clients
```

### Key Components:

1. **DomainEvent**: Abstract base class for events
2. **DomainEventPublisher**: Port interface for publishing
3. **WebSocketEventPublisher**: Infrastructure implementation
4. **Scope-based Subscriptions**: Players subscribe to specific lobby/game IDs
5. **Event Handlers**: Frontend handlers for each event type

## Benefits

✅ **Real-time Updates**: Players see lobby changes instantly
✅ **Scalable**: Easy to add new events (game events, player ready, etc.)
✅ **Decoupled**: Use cases don't know about WebSocket implementation
✅ **Testable**: Can use `NoOpEventPublisher` in tests
✅ **Scope Isolation**: Events only go to relevant players (lobby or game)

## Future Enhancements

- Add `PlayerLeftLobbyEvent` when players disconnect
- Add `PlayerReadyEvent` for ready-check system
- Add `LobbyUpdatedEvent` for settings changes
- Display full player list in lobby UI
- Add player status indicators (ready/not ready)
- Show player names in lobby (not just count)
