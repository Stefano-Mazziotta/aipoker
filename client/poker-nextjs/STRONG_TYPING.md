# Strong Typing Implementation - WebSocket Communication

## Overview
This document describes the implementation of strong typing for WebSocket communication between the poker client (TypeScript/React) and server (Java/Jakarta WebSocket).

## Architecture

### Server-Side (Java)
The server uses:
- **Domain Events**: Java classes representing game state changes
- **DTOs**: Data Transfer Objects for command requests
- **JSON Protocol**: All messages are JSON with `{command, data}` structure

### Client-Side (TypeScript)
The client now mirrors the server's type system with:
- **ServerEvent types**: TypeScript interfaces matching Java domain events
- **Command types**: TypeScript interfaces matching Java DTOs
- **Type guards**: Functions for safe event discrimination

## Type System

### 1. Server Events (`server-events.ts`)

#### Domain Events (Game Business Logic)
These events represent important state changes in the poker game:

```typescript
// Lobby Events
interface PlayerJoinedLobbyEvent {
  eventType: 'PLAYER_JOINED_LOBBY';
  lobbyId: string;
  playerId: string;
  playerName: string;
  playerChips: number;
  currentPlayerCount: number;
  maxPlayers: number;
}

interface PlayerLeftLobbyEvent {
  eventType: 'PLAYER_LEFT_LOBBY';
  lobbyId: string;
  playerId: string;
  playerName: string;
  currentPlayerCount: number;
  maxPlayers: number;
}

// Game Events
interface GameStateChangedEvent {
  eventType: 'GAME_STATE_CHANGED';
  gameId: string;
  newState: string;
  currentPlayerId: string;
  currentPlayerName: string;
  pot: number;
}

interface PlayerActionEvent {
  eventType: 'PLAYER_ACTION';
  gameId: string;
  playerId: string;
  playerName: string;
  action: 'FOLD' | 'CHECK' | 'CALL' | 'RAISE' | 'ALL_IN';
  amount: number;
  newPot: number;
  currentBet: number;
}

interface CardsDealtEvent {
  eventType: 'CARDS_DEALT';
  gameId: string;
  phase: string;
  newCards: string[];
  allCommunityCards: string[];
}

interface WinnerDeterminedEvent {
  eventType: 'WINNER_DETERMINED';
  gameId: string;
  winnerId: string;
  winnerName: string;
  handRank: string;
  amountWon: number;
}
```

#### Response Events (Command Responses)
These events are responses to client commands:

```typescript
interface PlayerRegisteredEvent {
  eventType: 'PLAYER_REGISTERED';
  playerId: string;
  playerName: string;
  chips: number;
}

interface LobbyCreatedEvent {
  eventType: 'LOBBY_CREATED';
  lobbyId: string;
  adminId: string;
  players: PlayerInfo[];
  maxPlayers: number;
}

interface GameStartedEvent {
  eventType: 'GAME_STARTED';
  gameId: string;
  lobbyId: string;
  playerIds: string[];
  smallBlind: number;
  bigBlind: number;
}

interface ErrorEvent {
  eventType: 'ERROR';
  message: string;
  code?: string;
}
```

#### Type Guards
Safe event type discrimination:

```typescript
export function isPlayerActionEvent(event: ServerEvent): event is PlayerActionEvent {
  return event.eventType === 'PLAYER_ACTION';
}

export function isGameStartedEvent(event: ServerEvent): event is GameStartedEvent {
  return event.eventType === 'GAME_STARTED';
}

// ... more type guards for each event type
```

### 2. Commands (`commands.ts`)

#### Command Structure
All commands follow this pattern:

```typescript
interface WebSocketCommand<T> {
  command: string;
  data: T;
}
```

#### Command Data Types
```typescript
// Player Commands
interface RegisterPlayerData {
  playerName: string;
}

// Lobby Commands
interface CreateLobbyData {
  playerId: string;
  maxPlayers: number;
}

interface StartGameData {
  lobbyId: string;
  playerIds: string[];
  smallBlind: number;
  bigBlind: number;
}

// Game Commands
interface PlayerActionData {
  gameId: string;
  playerId: string;
  action: 'FOLD' | 'CHECK' | 'CALL' | 'RAISE' | 'ALL_IN';
  amount?: number;
}
```

#### Type-Safe Command Builders
```typescript
export function createStartGameCommand(
  lobbyId: string,
  playerIds: string[],
  smallBlind: number,
  bigBlind: number
): WebSocketCommand<StartGameData> {
  return {
    command: CommandType.START_GAME,
    data: { lobbyId, playerIds, smallBlind, bigBlind }
  };
}

export function createRaiseCommand(
  gameId: string,
  playerId: string,
  amount: number
): WebSocketCommand<PlayerActionData> {
  return {
    command: CommandType.RAISE,
    data: { gameId, playerId, action: 'RAISE', amount }
  };
}
```

### 3. WebSocket Client (`client.ts`)

#### Strongly Typed Interface
```typescript
export type MessageHandler = (event: ServerEvent) => void;

export class WebSocketClient {
  send(command: WebSocketCommand<unknown>): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      const message = JSON.stringify(command);
      this.ws.send(message);
    }
  }

  onMessage(handler: MessageHandler): () => void {
    this.messageHandlers.add(handler);
    return () => this.messageHandlers.delete(handler);
  }
}
```

#### Message Parsing
```typescript
this.ws.onmessage = (event) => {
  const response = JSON.parse(event.data);
  const eventType = response.type || response.eventType;
  const message: ServerEvent = {
    eventType: eventType?.toUpperCase() || 'UNKNOWN',
    timestamp: response.timestamp || Date.now(),
    ...response
  } as ServerEvent;
  
  this.notifyMessageHandlers(message);
};
```

### 4. Context Integration

#### WebSocketContext
```typescript
interface WebSocketContextType {
  status: WebSocketStatus;
  isConnected: boolean;
  sendCommand: (command: WebSocketCommand<unknown>) => void;
  subscribe: (handler: (event: ServerEvent) => void) => () => void;
  commands: typeof commands;
}
```

#### GameContext Example
```typescript
useEffect(() => {
  const unsubscribe = subscribe((event: ServerEvent) => {
    if (isGameStartedEvent(event)) {
      console.log('Game started:', event.gameId);
      setGameId(event.gameId);
      sendCommand(commands.getGameState(event.gameId));
    } else if (isPlayerActionEvent(event)) {
      console.log('Player action:', event.action, event.amount);
      // Update UI accordingly
    } else if (isWinnerDeterminedEvent(event)) {
      console.log('Winner:', event.winnerName, event.handRank);
      // Show winner screen
    }
  });
  return unsubscribe;
}, [subscribe, sendCommand, commands]);
```

## Benefits

### 1. Compile-Time Type Safety
- **No runtime type errors**: TypeScript catches type mismatches at compile time
- **Autocomplete**: IDE provides suggestions for all event properties
- **Refactoring safety**: Renaming fields is safe across the codebase

### 2. Self-Documenting Code
- **Clear contracts**: Types document what data is expected
- **API discovery**: Developers can explore available commands and events via types
- **Reduced bugs**: Less guesswork about data structure

### 3. Maintainability
- **Single source of truth**: Types defined once, used everywhere
- **Consistent naming**: Events match between client and server
- **Easy to extend**: Adding new events/commands is straightforward

### 4. Better Developer Experience
- **IntelliSense**: Full IDE support with type hints
- **Error prevention**: Catches mistakes before runtime
- **Code navigation**: Jump to definitions, find usages

## Usage Examples

### Sending Commands
```typescript
// Type-safe: compiler checks all parameters
sendCommand(commands.startGame(lobbyId, playerIds, 50, 100));
sendCommand(commands.raise(gameId, playerId, 200));
sendCommand(commands.fold(gameId, playerId));
```

### Handling Events
```typescript
subscribe((event: ServerEvent) => {
  // Type guard narrows the type
  if (isPlayerActionEvent(event)) {
    // event is now PlayerActionEvent
    console.log(event.action); // ✅ Valid
    console.log(event.lobbyId); // ❌ Compile error - not in PlayerActionEvent
  }
  
  if (isLobbyCreatedEvent(event)) {
    // event is now LobbyCreatedEvent
    console.log(event.lobbyId, event.maxPlayers); // ✅ Valid
  }
});
```

## Server-Client Type Mapping

| Java Class | TypeScript Interface | Purpose |
|------------|---------------------|---------|
| `PlayerJoinedLobbyEvent` | `PlayerJoinedLobbyEvent` | Player joins lobby |
| `GameStateChangedEvent` | `GameStateChangedEvent` | Game phase changes |
| `PlayerActionEvent` | `PlayerActionEvent` | Player performs action |
| `StartGameRequest` | `StartGameData` | Start game command |
| `PlayerActionUseCase` params | `PlayerActionData` | Player action command |

## Future Improvements

1. **Runtime Validation**: Add Zod or similar for runtime type checking
2. **Code Generation**: Generate TypeScript types from Java classes automatically
3. **Shared Schema**: Use JSON Schema or Protocol Buffers for single source of truth
4. **Error Types**: Add discriminated unions for different error categories
5. **Event Versioning**: Add version field to support protocol evolution

## Conclusion

The strong typing implementation ensures that the poker game's WebSocket communication is:
- **Type-safe**: Compile-time guarantees prevent runtime errors
- **Self-documenting**: Types serve as living documentation
- **Maintainable**: Easy to refactor and extend
- **Developer-friendly**: Great IDE support and autocomplete

This foundation enables rapid development with confidence that client-server communication contracts are respected.
