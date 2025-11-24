# WebSocket Migration - Real-Time Communication

## Overview
Migrated from TCP sockets to WebSocket protocol for better browser compatibility and real-time bidirectional communication in multiplayer poker games.

## Architecture

### Event-Driven System
Implemented a publish-subscribe pattern for broadcasting game events to all connected clients:

```
┌──────────────────┐
│   Game Use Cases │
│  (PlayerAction,  │
│   DealCards,     │
│   etc.)          │
└────────┬─────────┘
         │ publishes
         ↓
┌─────────────────────┐
│ GameEventPublisher  │ (Singleton)
│ - Game subscriptions│
│ - Lobby subscriptions│
└─────────┬───────────┘
          │ broadcasts
          ↓
┌──────────────────────┐
│ WebSocket Sessions   │
│ (Connected Clients)  │
└──────────────────────┘
```

### Event Types

1. **PlayerActionEvent** - Fired when a player performs an action
   ```json
   {
     "eventType": "PLAYER_ACTION",
     "gameId": "uuid",
     "playerId": "uuid",
     "playerName": "Alice",
     "action": "RAISE",
     "amount": 50,
     "newPot": 150,
     "currentBet": 50,
     "timestamp": "2025-11-24T16:00:00Z"
   }
   ```

2. **CardsDealtEvent** - Fired when community cards are dealt
   ```json
   {
     "eventType": "CARDS_DEALT",
     "gameId": "uuid",
     "phase": "FLOP",
     "newCards": ["AH", "KD", "QS"],
     "allCommunityCards": ["AH", "KD", "QS"],
     "timestamp": "2025-11-24T16:00:00Z"
   }
   ```

3. **GameStateChangedEvent** - Fired when game state changes
   ```json
   {
     "eventType": "GAME_STATE_CHANGED",
     "gameId": "uuid",
     "newState": "PRE_FLOP",
     "currentPlayerId": "uuid",
     "currentPlayerName": "Bob",
     "pot": 30,
     "timestamp": "2025-11-24T16:00:00Z"
   }
   ```

4. **WinnerDeterminedEvent** - Fired when winner is determined
   ```json
   {
     "eventType": "WINNER_DETERMINED",
     "gameId": "uuid",
     "winnerId": "uuid",
     "winnerName": "Alice",
     "handRank": "FULL_HOUSE",
     "amountWon": 200,
     "timestamp": "2025-11-24T16:00:00Z"
   }
   ```

## WebSocket Protocol

### Connection
```javascript
const ws = new WebSocket('ws://localhost:8081/ws/poker');
```

### Client Commands
Send JSON messages with a `command` field:

```json
{
  "command": "REGISTER alice 1000"
}
```

### Subscription Commands
Subscribe to receive real-time events:

```json
{
  "command": "SUBSCRIBE_GAME game-uuid"
}
```

```json
{
  "command": "SUBSCRIBE_LOBBY lobby-uuid"
}
```

### Server Responses
Responses are sent as JSON with `type` and `content` fields:

```json
{
  "type": "response",
  "content": "Player registered successfully"
}
```

### Real-Time Events
Game events are broadcast to all subscribed clients automatically.

## Implementation Details

### Dependencies
- **Jakarta WebSocket API 2.1.1** - Standard WebSocket API
- **Tyrus 2.1.5** - WebSocket server implementation
  - `tyrus-server` - Server core
  - `tyrus-container-grizzly-server` - Grizzly HTTP server integration
  - `tyrus-standalone-client-jdk` - Standalone client support
- **Gson 2.10.1** - JSON serialization/deserialization

### Key Classes

#### `GameEventPublisher`
- Singleton pattern for centralized event broadcasting
- Thread-safe with `ConcurrentHashMap` for subscriptions
- Manages game and lobby subscriptions separately
- Cleans up closed sessions automatically

#### `PokerWebSocketEndpoint`
- `@ServerEndpoint("/ws/poker")` - WebSocket endpoint
- Handles lifecycle events: `@OnOpen`, `@OnMessage`, `@OnClose`, `@OnError`
- Parses JSON commands and delegates to `ProtocolHandler`
- Sends responses back to clients

#### `WebSocketServer`
- Wraps Tyrus server with Grizzly container
- Configurable host and port (default: `localhost:8081`)
- Graceful start/stop/awaitTermination

### Use Case Integration

All game use cases now publish events:

1. **PlayerActionUseCase** - After each action (fold, check, call, raise, all-in)
2. **DealCardsUseCase** - After dealing flop, turn, or river
3. **StartGameUseCase** - When game starts
4. **DetermineWinnerUseCase** - When winner is determined

## Testing

### Manual Testing with HTML Client
1. Start the server:
   ```bash
   java -jar target/aipoker-server-1.0.0.jar
   ```

2. Open `websocket-client.html` in a browser

3. Connect to `ws://localhost:8081/ws/poker`

4. Try commands:
   - `REGISTER player1 1000`
   - `LEADERBOARD`
   - `HELP`

### Testing Real-Time Events
1. Open multiple browser tabs with the test client
2. Register players in each tab
3. Start a game with multiple players
4. Observe that all tabs receive real-time updates when:
   - A player performs an action
   - Community cards are dealt
   - Game state changes
   - Winner is determined

## Migration Benefits

### Before (TCP Sockets)
- ❌ Not browser-compatible
- ❌ Requires custom client implementation
- ❌ Limited to request-response pattern
- ❌ No built-in message framing

### After (WebSockets)
- ✅ Native browser support
- ✅ Works with standard WebSocket API
- ✅ Bidirectional real-time communication
- ✅ Built-in message framing and JSON support
- ✅ Event-driven architecture
- ✅ Automatic broadcasting to subscribed clients

## Future Enhancements

1. **Authentication** - Add JWT token validation for WebSocket connections
2. **Heartbeat** - Implement ping/pong for connection health monitoring
3. **Reconnection** - Add automatic reconnection logic on client side
4. **Compression** - Enable WebSocket message compression for bandwidth efficiency
5. **SSL/TLS** - Use `wss://` for encrypted WebSocket connections
6. **Rate Limiting** - Add rate limiting to prevent abuse
7. **Message Queue** - Add Redis pub/sub for horizontal scaling across multiple server instances

## Backward Compatibility

The old TCP socket server code has been removed. Clients must migrate to WebSocket protocol.

## Related Commits

1. `build: add WebSocket dependencies (Jakarta WebSocket API and Tyrus server)`
2. `feat: implement event-driven architecture for real-time game notifications`
3. `feat: implement WebSocket server infrastructure`
4. `refactor: migrate from TCP sockets to WebSocket server`
5. `feat: integrate event publishing into game use cases`
6. `test: add HTML WebSocket test client`
