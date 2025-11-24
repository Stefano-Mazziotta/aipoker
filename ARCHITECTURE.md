# 🏛️ Architecture Guide

> Comprehensive guide to the architectural patterns used in the Texas Hold'em Poker Server: **Hexagonal Architecture**, **Domain-Driven Design**, **Screaming Architecture**, and **Event-Driven Architecture**.

---

## 📖 Table of Contents

1. [Overview](#overview)
2. [Hexagonal Architecture](#hexagonal-architecture-ports--adapters)
3. [Domain-Driven Design (DDD)](#domain-driven-design-ddd)
4. [Screaming Architecture](#screaming-architecture)
5. [Event-Driven Architecture (EDA)](#event-driven-architecture-eda)
6. [How They Work Together](#how-they-work-together)
7. [Code Examples](#code-examples)

---

## Overview

This project combines four complementary architectural approaches:

| Pattern | Purpose | Benefit |
|---------|---------|---------|
| **Hexagonal Architecture** | Structure & Isolation | Testable, framework-independent core |
| **Domain-Driven Design** | Business Modeling | Rich domain model, ubiquitous language |
| **Screaming Architecture** | Organization | Self-documenting structure |
| **Event-Driven Architecture** | Communication | Decoupled, scalable real-time updates |

Together, they create a system that is:
- ✅ **Testable** - Business logic isolated from infrastructure
- ✅ **Maintainable** - Clear separation of concerns
- ✅ **Scalable** - Event-driven for real-time multiplayer
- ✅ **Flexible** - Easy to swap implementations
- ✅ **Understandable** - Structure reveals intent

---

## Hexagonal Architecture (Ports & Adapters)

### Concept

**Hexagonal Architecture** isolates core business logic from external concerns (database, UI, network).

```
┌─────────────────────────────────────────┐
│         PRIMARY ADAPTERS                │
│   (WebSocket, REST, CLI, etc.)          │
└──────────────┬──────────────────────────┘
               │
        ┌──────▼───────┐
        │  USE CASES   │  ← Application Layer
        │  (Ports In)  │
        └──────┬───────┘
               │
        ┌──────▼───────┐
        │    DOMAIN    │  ← Pure Business Logic
        │  (Game, Player)│
        └──────┬───────┘
               │
        ┌──────▼────────┐
        │  REPOSITORIES │  ← Ports Out
        │  (Interfaces) │
        └──────┬────────┘
               │
┌──────────────▼──────────────────────────┐
│      SECONDARY ADAPTERS                 │
│  (SQLite, PostgreSQL, Redis, etc.)      │
└─────────────────────────────────────────┘
```

### Layers

#### 1. Domain Layer (Core)
- **Pure business logic** - No framework dependencies
- **Entities**: `Player`, `Game`, `Card`, `Hand`
- **Value Objects**: `Chips`, `PlayerId`, `GameId`
- **Domain Services**: `HandEvaluator`

```java
// Domain Entity - No infrastructure dependencies
public class Player {
    private final PlayerId id;
    private Chips chips;
    private boolean folded;
    
    public void bet(Chips amount) {
        if (amount.isGreaterThan(chips)) {
            throw new IllegalArgumentException("Insufficient chips");
        }
        chips = chips.subtract(amount);
    }
}
```

#### 2. Application Layer (Use Cases)
- **Orchestrates** domain objects
- **Defines** ports (interfaces)
- **No infrastructure** implementation details

```java
// Use Case - Coordinates domain logic
public class PlayerActionUseCase {
    private final GameRepository gameRepository;  // Port (interface)
    private final GameEventPublisher eventPublisher;
    
    public ActionResponse execute(PlayerActionCommand command) {
        Game game = gameRepository.findById(command.gameId());
        game.playerAction(command.action(), command.amount());
        gameRepository.save(game);
        
        // Publish event
        eventPublisher.publishToGame(new PlayerActionEvent(...));
        return new ActionResponse(...);
    }
}
```

#### 3. Infrastructure Layer (Adapters)
- **Implements** ports
- **Handles** technical details (database, network, events)

```java
// Adapter - Implements repository interface
public class SQLiteGameRepository implements GameRepository {
    @Override
    public Optional<Game> findById(GameId id) {
        // SQL query, result mapping
    }
    
    @Override
    public void save(Game game) {
        // SQL insert/update
    }
}
```

### Benefits
- ✅ **Testable**: Test domain logic without database
- ✅ **Flexible**: Swap SQLite → PostgreSQL without changing domain
- ✅ **Independent**: Core logic doesn't depend on frameworks

---

## Domain-Driven Design (DDD)

### Concept

**DDD** focuses on modeling complex business domains with rich, expressive code that reflects real-world concepts.

### Strategic Design

#### Bounded Contexts
Separate models that have clear boundaries:

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   GAME      │     │   PLAYER    │     │   LOBBY     │
│  Context    │────▶│   Context   │◀────│   Context   │
│             │     │             │     │             │
│ - Game      │     │ - Player    │     │ - Lobby     │
│ - Hand      │     │ - Chips     │     │ - Room      │
│ - Card      │     │ - Action    │     │ - Join      │
└─────────────┘     └─────────────┘     └─────────────┘
```

In this project:
- **Game Context**: Game rules, hand evaluation, betting
- **Player Context**: Player registration, chip management
- **Lobby Context**: Room creation, matchmaking

#### Shared Kernel
Common concepts used across contexts:
- `Chips` (Value Object)
- `PlayerId`, `GameId` (identifiers)

### Tactical Design

#### Entities
Objects with **identity** that persist over time:

```java
// Entity - Has identity (id), mutable state
public class Game {
    private final GameId id;  // Identity
    private GameState state;
    private Pot pot;
    private List<Card> communityCards;
    
    // Business methods
    public void dealFlop() { ... }
    public Player determineWinner() { ... }
}
```

#### Value Objects
Objects defined by their **attributes**, immutable:

```java
// Value Object - No identity, immutable
public record Chips(int amount) {
    public Chips {
        if (amount < 0) {
            throw new IllegalArgumentException("Chips cannot be negative");
        }
    }
    
    public Chips add(Chips other) {
        return new Chips(this.amount + other.amount);
    }
    
    public Chips subtract(Chips other) {
        return new Chips(this.amount - other.amount);
    }
}
```

#### Aggregates
Cluster of objects treated as a unit:

```java
// Game is an Aggregate Root
// Controls access to Round, Pot, Deck
public class Game {
    private final GameId id;  // Root entity
    private Round currentRound;  // Part of aggregate
    private Pot pot;  // Part of aggregate
    private Deck deck;  // Part of aggregate
    
    // All modifications go through the root
    public void playerBet(PlayerId playerId, Chips amount) {
        currentRound.recordBet(playerId, amount);
        pot.add(amount);
    }
}
```

#### Repositories
Provide **collection-like** access to aggregates:

```java
// Repository interface (in domain layer)
public interface GameRepository {
    Optional<Game> findById(GameId id);
    void save(Game game);
    List<Game> findAllActive();
}
```

#### Domain Events
Capture **significant business events**:

```java
// Domain Event - Something that happened
public class PlayerActionEvent extends GameEvent {
    private final String playerId;
    private final PlayerAction action;
    private final int amount;
    private final Instant timestamp;
}
```

### Ubiquitous Language
Terms used by **both developers and domain experts**:

| Term | Meaning |
|------|---------|
| **Flop** | First 3 community cards |
| **Turn** | 4th community card |
| **River** | 5th community card |
| **Showdown** | Revealing hands to determine winner |
| **Pot** | Total chips bet in current game |
| **Blinds** | Forced bets (small blind, big blind) |

---

## Screaming Architecture

### Concept

**Screaming Architecture** means your code structure **screams what the application does**, not what frameworks it uses.

### Bad vs Good

#### ❌ Framework-Centric (Bad)
```
src/
├── controllers/
├── services/
├── models/
├── repositories/
└── views/
```
*This screams "I'm a web app!" but not "I'm a poker game!"*

#### ✅ Feature-Centric (Good)
```
src/com/poker/
├── game/          # "I handle poker games!"
├── player/        # "I manage players!"
├── lobby/         # "I handle lobbies!"
└── ranking/       # "I rank players!"
```
*This screams "I'm a poker application!"*

### Our Structure

```
com/poker/
├── game/                    ← "Game" feature
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Game.java
│   │   │   ├── Card.java
│   │   │   └── Hand.java
│   │   ├── evaluation/
│   │   │   └── HandEvaluator.java
│   │   └── repository/
│   │       └── GameRepository.java
│   ├── application/
│   │   ├── StartGameUseCase.java
│   │   ├── PlayerActionUseCase.java
│   │   └── DealCardsUseCase.java
│   └── infrastructure/
│       └── persistence/
│           └── SQLiteGameRepository.java
│
├── player/                  ← "Player" feature
│   ├── domain/
│   │   ├── model/
│   │   │   └── Player.java
│   │   └── repository/
│   │       └── PlayerRepository.java
│   ├── application/
│   │   └── RegisterPlayerUseCase.java
│   └── infrastructure/
│       └── persistence/
│           └── SQLitePlayerRepository.java
│
└── lobby/                   ← "Lobby" feature
    ├── domain/
    ├── application/
    └── infrastructure/
```

When a new developer sees this, they immediately understand:
- **"This is a poker application"**
- **"It has games, players, and lobbies"**
- **"Each feature is self-contained"**

---

## Event-Driven Architecture (EDA)

### Concept

**Event-Driven Architecture** enables **asynchronous communication** between components through events, making the system scalable and loosely coupled.

### Architecture

```
┌─────────────────────────────────────────────────────┐
│                 USE CASES                           │
│  (StartGame, PlayerAction, DealCards, etc.)         │
└────────────────────┬────────────────────────────────┘
                     │ publishes
                     ▼
           ┌─────────────────────┐
           │  GameEventPublisher │  ← Singleton
           │                     │
           │ - gameSubscriptions │
           │ - lobbySubscriptions│
           └──────────┬──────────┘
                      │ broadcasts
                      ▼
        ┌─────────────────────────────┐
        │   WebSocket Sessions        │
        │  (Connected Players)        │
        └─────────────────────────────┘
```

### Event Types

#### 1. PlayerActionEvent
Fired when a player makes an action:

```java
public class PlayerActionEvent extends GameEvent {
    private final String playerId;
    private final String playerName;
    private final PlayerAction action;  // FOLD, CALL, RAISE, etc.
    private final int amount;
    private final int newPot;
    private final int currentBet;
}
```

**When**: After `playerAction()` in `PlayerActionUseCase`
**Who**: All players subscribed to the game
**Why**: Real-time notification of player actions

#### 2. CardsDealtEvent
Fired when community cards are dealt:

```java
public class CardsDealtEvent extends GameEvent {
    private final String phase;  // FLOP, TURN, RIVER
    private final List<String> newCards;
    private final List<String> allCommunityCards;
}
```

**When**: After `dealFlop()`, `dealTurn()`, `dealRiver()`
**Who**: All players in the game
**Why**: Everyone needs to see the new cards

#### 3. GameStateChangedEvent
Fired when game state transitions:

```java
public class GameStateChangedEvent extends GameEvent {
    private final String newState;  // PRE_FLOP, FLOP, etc.
    private final String currentPlayerId;
    private final String currentPlayerName;
    private final int pot;
}
```

**When**: Game start, turn changes, phase transitions
**Who**: All players in the game
**Why**: UI updates, whose turn indication

#### 4. WinnerDeterminedEvent
Fired when game ends and winner is determined:

```java
public class WinnerDeterminedEvent extends GameEvent {
    private final String winnerId;
    private final String winnerName;
    private final String handRank;  // FULL_HOUSE, ROYAL_FLUSH, etc.
    private final int amountWon;
}
```

**When**: After `determineWinner()` at showdown
**Who**: All players in the game
**Why**: Announce winner, update UI

### Event Publisher

Singleton pattern managing subscriptions:

```java
public class GameEventPublisher {
    private static GameEventPublisher instance;
    
    // gameId -> Set<WebSocket Sessions>
    private final Map<String, Set<Session>> gameSubscriptions;
    
    // lobbyId -> Set<WebSocket Sessions>
    private final Map<String, Set<Session>> lobbySubscriptions;
    
    public void subscribeToGame(String gameId, Session session, String playerId) {
        gameSubscriptions.computeIfAbsent(gameId, k -> new CopyOnWriteArraySet<>())
            .add(session);
    }
    
    public void publishToGame(GameEvent event) {
        String gameId = event.getGameId();
        Set<Session> sessions = gameSubscriptions.get(gameId);
        
        if (sessions != null) {
            String json = event.toJson();
            for (Session session : sessions) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(json);
                }
            }
        }
    }
}
```

### Benefits

✅ **Loose Coupling**: Use cases don't know about WebSocket clients
✅ **Scalability**: Broadcast to multiple clients efficiently
✅ **Real-time**: Immediate updates to all players
✅ **Extensibility**: Easy to add new event types
✅ **Observability**: All game actions are observable events

---

## How They Work Together

### Request Flow Example: Player Makes a Bet

```
1. WebSocket Client
   └─► "RAISE 50"
        │
2. PokerWebSocketEndpoint (Primary Adapter)
   └─► Parses command, calls use case
        │
3. PlayerActionUseCase (Application Layer)
   └─► game.playerAction(RAISE, 50)
        │
4. Game Domain (Domain Layer)
   └─► Business logic: validate, update pot, change state
        │
5. GameRepository (Port)
   └─► gameRepository.save(game)
        │
6. SQLiteGameRepository (Secondary Adapter)
   └─► SQL UPDATE statement
        │
7. PlayerActionUseCase (Application Layer)
   └─► eventPublisher.publishToGame(new PlayerActionEvent(...))
        │
8. GameEventPublisher (Event Infrastructure)
   └─► Broadcasts JSON event to all WebSocket sessions subscribed to game
        │
9. All Connected Clients
   └─► Receive real-time update: "Alice raised 50"
```

### Architectural Layers Visualization

```
┌─────────────────────────────────────────────────────────┐
│           PRIMARY ADAPTERS (Input)                      │
│  WebSocketEndpoint, REST API, CLI                       │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│          APPLICATION LAYER (Use Cases)                  │
│  StartGameUseCase, PlayerActionUseCase, etc.            │
│  ┌──────────────┐          ┌─────────────────┐         │
│  │ Coordinates  │──events─▶│ EventPublisher  │         │
│  │ Domain Logic │          │  (Broadcasts)   │         │
│  └──────────────┘          └─────────────────┘         │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              DOMAIN LAYER (Business Logic)              │
│  Game, Player, Hand, Card (Entities)                    │
│  Chips, PlayerId (Value Objects)                        │
│  HandEvaluator (Domain Service)                         │
│  GameRepository, PlayerRepository (Ports/Interfaces)    │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│        INFRASTRUCTURE LAYER (Secondary Adapters)        │
│  SQLiteGameRepository, SQLitePlayerRepository           │
│  WebSocketServer, DatabaseInitializer                   │
│  GameEventPublisher, Event Classes                      │
└─────────────────────────────────────────────────────────┘
```

---

## Code Examples

### Complete Feature: Player Betting

#### 1. Domain Model
```java
// Pure business logic
public class Game {
    private final GameId id;
    private GameState state;
    private Pot pot;
    private List<Player> players;
    private int currentPlayerIndex;
    private int currentBet;
    
    public void playerRaise(Player player, Chips amount) {
        // Domain rules
        if (!player.equals(getCurrentPlayer())) {
            throw new IllegalStateException("Not your turn");
        }
        
        int raiseAmount = amount.getAmount();
        if (raiseAmount <= currentBet) {
            throw new IllegalArgumentException("Raise must be higher than current bet");
        }
        
        player.bet(amount);
        pot.add(amount);
        currentBet = raiseAmount;
        advanceToNextPlayer();
    }
    
    private Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    private void advanceToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
}
```

#### 2. Use Case
```java
public class PlayerActionUseCase {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameEventPublisher eventPublisher;
    
    public ActionResponse execute(PlayerActionCommand command) {
        // Load aggregates
        Game game = gameRepository.findById(GameId.from(command.gameId()))
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        Player player = playerRepository.findById(PlayerId.from(command.playerId()))
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        
        // Execute domain logic
        switch (command.action()) {
            case RAISE -> game.playerRaise(player, new Chips(command.amount()));
            case CALL -> game.playerCall(player);
            case FOLD -> game.playerFold(player);
            // ... other actions
        }
        
        // Persist
        gameRepository.save(game);
        playerRepository.save(player);
        
        // Publish event for real-time updates
        PlayerActionEvent event = new PlayerActionEvent(
            command.gameId(),
            command.playerId(),
            player.getName(),
            command.action(),
            command.amount(),
            game.getCurrentPot().getAmount(),
            game.getCurrentBet()
        );
        eventPublisher.publishToGame(event);
        
        return new ActionResponse(game.getState().name(), game.getCurrentPot().getAmount());
    }
}
```

#### 3. Repository Interface (Port)
```java
public interface GameRepository {
    Optional<Game> findById(GameId id);
    void save(Game game);
    List<Game> findAllActive();
}
```

#### 4. Repository Implementation (Adapter)
```java
public class SQLiteGameRepository implements GameRepository {
    private final Connection connection;
    
    @Override
    public Optional<Game> findById(GameId id) {
        String sql = "SELECT * FROM games WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.getValue().toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapToGame(rs));
            }
        }
        return Optional.empty();
    }
    
    @Override
    public void save(Game game) {
        String sql = "INSERT OR REPLACE INTO games (id, state, pot, ...) VALUES (?, ?, ?, ...)";
        // SQL implementation
    }
}
```

#### 5. Event Publishing
```java
public class GameEventPublisher {
    private final Map<String, Set<Session>> gameSubscriptions = new ConcurrentHashMap<>();
    
    public void publishToGame(GameEvent event) {
        String gameId = event.getGameId();
        Set<Session> sessions = gameSubscriptions.get(gameId);
        
        if (sessions != null) {
            String json = event.toJson();
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        // Handle error
                    }
                }
            }
        }
    }
}
```

#### 6. WebSocket Endpoint (Primary Adapter)
```java
@ServerEndpoint("/ws/poker")
public class PokerWebSocketEndpoint {
    private static ProtocolHandler protocolHandler;
    private static GameEventPublisher eventPublisher;
    
    @OnMessage
    public void onMessage(String message, Session session) {
        JsonObject json = JsonParser.parseString(message).getAsJsonObject();
        String command = json.get("command").getAsString();
        
        if (command.startsWith("SUBSCRIBE_GAME")) {
            String gameId = command.split(" ")[1];
            eventPublisher.subscribeToGame(gameId, session, "player-id");
            return;
        }
        
        // Delegate to protocol handler
        String response = protocolHandler.handle(command);
        session.getBasicRemote().sendText(response);
    }
}
```

---

## Key Takeaways

### Hexagonal Architecture
- ✅ Isolate business logic from infrastructure
- ✅ Define clear interfaces (ports)
- ✅ Implement adapters for external systems

### Domain-Driven Design
- ✅ Model the domain with rich objects
- ✅ Use ubiquitous language
- ✅ Define bounded contexts
- ✅ Protect invariants in aggregates

### Screaming Architecture
- ✅ Organize by feature, not framework
- ✅ Make structure reveal intent
- ✅ Self-documenting code organization

### Event-Driven Architecture
- ✅ Publish domain events for significant actions
- ✅ Decouple components through events
- ✅ Enable real-time updates
- ✅ Scale horizontally with pub/sub

---

## Further Reading

- **Hexagonal Architecture**: [Alistair Cockburn's Article](https://alistair.cockburn.us/hexagonal-architecture/)
- **DDD**: "Domain-Driven Design" by Eric Evans
- **Clean Architecture**: "Clean Architecture" by Robert C. Martin
- **Event-Driven**: "Building Event-Driven Microservices" by Adam Bellemare
- **Screaming Architecture**: [Uncle Bob's Blog](https://blog.cleancoder.com/uncle-bob/2011/09/30/Screaming-Architecture.html)

---

**This architecture enables building complex systems that remain maintainable, testable, and understandable over time.**
