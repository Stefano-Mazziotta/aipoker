# 🏛️ Architecture Documentation

> **Deep dive into the architectural decisions, patterns, and principles behind the AI Poker Server**

## Table of Contents

1. [Architecture Principles](#architecture-principles)
2. [Hexagonal Architecture](#hexagonal-architecture)
3. [Domain-Driven Design](#domain-driven-design)
4. [Event-Driven Architecture](#event-driven-architecture)
5. [Screaming Architecture](#screaming-architecture)
6. [SOLID Principles](#solid-principles)
7. [Design Patterns](#design-patterns)
8. [Package Structure](#package-structure)
9. [Layer Responsibilities](#layer-responsibilities)
10. [Data Flow](#data-flow)
11. [Testing Strategy](#testing-strategy)
12. [Performance Considerations](#performance-considerations)
13. [Trade-offs & Decisions](#trade-offs--decisions)

---

## Architecture Principles

This project follows these core principles:

1. **Business Logic First** - Domain drives design, not frameworks
2. **Separation of Concerns** - Clear boundaries between layers
3. **Dependency Inversion** - High-level modules independent of low-level details
4. **Testability** - Architecture enables easy testing
5. **Maintainability** - Code organized by business capability
6. **Scalability** - Loose coupling enables horizontal scaling
7. **Flexibility** - Easy to swap implementations

### Why These Principles?

Traditional layered architecture often leads to:
- ❌ Business logic scattered across layers
- ❌ Framework lock-in
- ❌ Difficult testing (need full stack)
- ❌ Tight coupling to database/UI

Our architecture solves these problems.

---

## Hexagonal Architecture

![Hexagonal Architecture](./resources/hexagonal-architecture.png)

### Concept

**Also known as:** Ports and Adapters Architecture

**Core Idea:** The application core (business logic) is isolated from external concerns through **ports** (interfaces) and **adapters** (implementations).

### Structure

```
┌─────────────────────────────────────────────────────────┐
│                    Application Core                      │
│  ┌───────────────────────────────────────────────────┐  │
│  │              Domain Layer                         │  │
│  │  • Entities (Game, Player, Deck, Card)           │  │
│  │  • Value Objects (PlayerId, Chips, GameId)       │  │
│  │  • Aggregates (Game is root)                     │  │
│  │  • Domain Services (HandEvaluator)               │  │
│  │  • Business Rules (betting logic, hand ranking)  │  │
│  │  • Domain Events                                 │  │
│  └───────────────────────────────────────────────────┘  │
│                          ↕                              │
│  ┌───────────────────────────────────────────────────┐  │
│  │          Application Layer                        │  │
│  │  • Use Cases (orchestrate domain logic)          │  │
│  │    - StartGameUseCase                            │  │
│  │    - PlayerActionUseCase                         │  │
│  │    - RegisterPlayerUseCase                       │  │
│  │  • DTOs (data transfer objects)                  │  │
│  │  • Ports (interfaces for external systems)       │  │
│  │    - GameRepository (port)                       │  │
│  │    - PlayerRepository (port)                     │  │
│  │    - DomainEventPublisher (port)                 │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
              ↑                              ↑
              │ Ports                        │ Ports
              │ (Interfaces)                 │ (Interfaces)
              ↓                              ↓
┌─────────────────────────┐      ┌─────────────────────────┐
│  Infrastructure Layer   │      │  Infrastructure Layer   │
│    (Left Adapters)      │      │    (Right Adapters)     │
│  • WebSocket Endpoint   │      │  • SQLite Repository    │
│  • REST Controllers     │      │  • EventBus Publisher   │
│  • CLI Interface        │      │  • Message Queue        │
│  (Driving Adapters)     │      │  (Driven Adapters)      │
└─────────────────────────┘      └─────────────────────────┘
```

### Implementation in Our Project

#### 1. Domain Layer (Pure Business Logic)

**Location:** `com.poker.game.domain.model`

```java
// Game.java - Aggregate Root
public class Game {
    private final GameId id;
    private final List<Player> players;
    private GameState state;
    private int pot;
    private List<Card> communityCards;
    private Round currentRound;
    
    // Pure business logic - NO infrastructure dependencies
    public void dealFlop() {
        if (state != GameState.PRE_FLOP) {
            throw new InvalidGameStateException();
        }
        
        deck.dealCard(); // Burn
        communityCards.add(deck.dealCard());
        communityCards.add(deck.dealCard());
        communityCards.add(deck.dealCard());
        
        this.state = GameState.FLOP;
        startNewBettingRound();
    }
    
    public Player determineWinner() {
        // Complex business logic here
        // NO database calls, NO WebSocket, NO framework code
    }
}
```

#### 2. Application Layer (Use Cases)

**Location:** `com.poker.game.application`

```java
// PlayerActionUseCase.java
public class PlayerActionUseCase {
    // Dependencies on PORTS (abstractions)
    private final GameRepository gameRepository;      // Port (interface)
    private final PlayerRepository playerRepository;  // Port (interface)
    private final DomainEventPublisher eventPublisher; // Port (interface)
    
    public PlayerActionUseCase(
        GameRepository gameRepository,
        PlayerRepository playerRepository,
        DomainEventPublisher eventPublisher
    ) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.eventPublisher = eventPublisher;
    }
    
    public void execute(PlayerActionCommand command) {
        // 1. Load from port
        Game game = gameRepository.findById(command.gameId())
            .orElseThrow(() -> new GameNotFoundException());
        
        Player player = playerRepository.findById(command.playerId())
            .orElseThrow(() -> new PlayerNotFoundException());
        
        // 2. Execute domain logic
        game.executePlayerAction(player, command.action(), command.amount());
        
        // 3. Persist through port
        gameRepository.save(game);
        
        // 4. Publish events through port
        eventPublisher.publishToScope(
            game.getId().toString(),
            new PlayerActionEvent(/*...*/)
        );
        
        // 5. Check for automatic progression
        if (isBettingRoundComplete(game)) {
            advanceGameState(game);
        }
    }
}
```

#### 3. Infrastructure Layer (Adapters)

**Location:** `com.poker.shared.infrastructure`

```java
// SQLiteGameRepository.java - Adapter implementing Port
public class SQLiteGameRepository implements GameRepository {
    
    @Override
    public Optional<Game> findById(GameId id) {
        // SQLite specific code
        String sql = "SELECT * FROM games WHERE id = ?";
        // ... SQL logic
        return Optional.of(reconstructGameFromRows(rows));
    }
    
    @Override
    public void save(Game game) {
        // SQLite specific code
        String sql = "INSERT OR REPLACE INTO games VALUES (?, ?, ?)";
        // ... SQL logic
    }
}

// WebSocketEventPublisher.java - Adapter implementing Port
public class WebSocketEventPublisher implements DomainEventPublisher {
    
    @Override
    public void publishToScope(String scope, DomainEvent<?> event) {
        // WebSocket specific code
        Set<Session> sessions = scopedSessions.get(scope);
        String json = gson.toJson(event);
        
        sessions.forEach(session -> {
            session.getBasicRemote().sendText(json);
        });
    }
}
```

### Benefits Realized

✅ **Framework Independence:** Business logic has ZERO dependencies on WebSocket, SQLite, Gson  
✅ **Testability:** Can test use cases with mocks (no need for database/WebSocket)  
✅ **Flexibility:** Can swap SQLite for PostgreSQL without touching business logic  
✅ **Clear Boundaries:** Dependencies flow INWARD (never outward from domain)

---

## Domain-Driven Design

![Domain-Driven Design](./resources/domain-driven-design.png)

### Strategic Design

#### Bounded Contexts

We've identified 4 bounded contexts:

```
┌──────────────────────────────────────────────────────────┐
│                    Poker System                          │
│                                                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │    Game     │  │   Player    │  │    Lobby    │    │
│  │  Context    │  │   Context   │  │   Context   │    │
│  │             │  │             │  │             │    │
│  │ • Game      │  │ • Player    │  │ • Lobby     │    │
│  │ • Deck      │  │ • Chips     │  │ • Seat      │    │
│  │ • Round     │  │ • Hand      │  │ • Table     │    │
│  └─────────────┘  └─────────────┘  └─────────────┘    │
│         │                │                 │            │
│         └────────────────┴─────────────────┘            │
│                          │                              │
│                  ┌───────────────┐                      │
│                  │    Shared     │                      │
│                  │   Kernel      │                      │
│                  │ • Events      │                      │
│                  │ • ValueObjs   │                      │
│                  └───────────────┘                      │
└──────────────────────────────────────────────────────────┘
```

**Bounded Context Mapping:**

| Context | Responsibility | Key Aggregates |
|---------|----------------|----------------|
| **Game** | Game lifecycle, betting rounds, showdown | `Game` (root) |
| **Player** | Player registration, chip management | `Player` (root) |
| **Lobby** | Matchmaking, table management | `Lobby` (root) |
| **Shared** | Cross-cutting concerns | Value Objects, Events |

#### Ubiquitous Language

We use **poker terminology** throughout:

| Business Term | Code Term | Explanation |
|---------------|-----------|-------------|
| Hole cards | `player.getHand()` | Private player cards |
| Community cards | `game.getCommunityCards()` | Shared cards |
| Small blind | `Blinds.small()` | Forced bet |
| Big blind | `Blinds.big()` | Larger forced bet |
| Pot | `game.getPot()` | Total bet pool |
| Muck | `player.fold()` | Discard hand |
| Showdown | `game.determineWinner()` | Reveal hands |

### Tactical Design

#### Entities vs Value Objects

**Entities:** Identity-based equality

```java
// Entity: Same ID = same entity
public class Game {
    private final GameId id; // Identity
    private GameState state; // Mutable state
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Game other)) return false;
        return id.equals(other.id); // Identity equality
    }
}
```

**Value Objects:** Value-based equality

```java
// Value Object: Same values = same object
public record Chips(int amount) {
    
    public Chips {
        if (amount < 0) throw new IllegalArgumentException();
    }
    
    public Chips add(int value) {
        return new Chips(amount + value); // Immutable
    }
    
    // Record automatically implements value equality
}
```

#### Aggregates

**Aggregate Root:** `Game`

```java
public class Game {
    // Root entity
    private final GameId id;
    
    // Child entities (managed by root)
    private final List<Player> players;
    private Round currentRound;
    private Deck deck;
    
    // Aggregate boundary: Can only modify through root
    public void executePlayerAction(Player player, Action action, int amount) {
        // Ensures consistency within aggregate
        validatePlayerInGame(player);
        currentRound.recordAction(player, action, amount);
        player.adjustChips(amount);
        pot += amount;
    }
    
    // NO public setters that bypass business rules!
}
```

**Why Aggregates?**
- ✅ Transactional consistency boundary
- ✅ Encapsulation of business rules
- ✅ Clear ownership hierarchy

#### Domain Services

Use when logic doesn't belong to a single entity:

```java
// HandEvaluationStrategy.java - Domain Service
public interface HandEvaluationStrategy {
    PokerHand evaluate(List<Card> cards);
}

public class TexasHoldemEvaluator implements HandEvaluationStrategy {
    
    @Override
    public PokerHand evaluate(List<Card> cards) {
        // Complex evaluation logic
        // Doesn't belong to Game or Player
        
        if (isRoyalFlush(cards)) return new RoyalFlush(cards);
        if (isStraightFlush(cards)) return new StraightFlush(cards);
        // ...
    }
}
```

#### Domain Events

```java
// Domain Event
public record GameStartedEvent(
    String gameId,
    List<String> playerIds,
    int smallBlind,
    int bigBlind,
    Instant occurredAt
) implements DomainEvent<GameStartedEventData> {
    
    public GameStartedEvent(String gameId, List<String> playerIds, 
                           int smallBlind, int bigBlind) {
        this(gameId, playerIds, smallBlind, bigBlind, Instant.now());
    }
    
    @Override
    public String getType() {
        return "GAME_STARTED";
    }
}
```

**When to Use:**
- Something significant happened in the domain
- Other bounded contexts need to know
- Enable loose coupling between aggregates

#### Repository Pattern

```java
// Port (interface in domain layer)
public interface GameRepository {
    Optional<Game> findById(GameId id);
    void save(Game game);
    List<Game> findActiveGames();
}

// Adapter (implementation in infrastructure layer)
public class SQLiteGameRepository implements GameRepository {
    // SQLite-specific implementation
}
```

**Why?**
- ✅ Domain layer doesn't know about SQLite
- ✅ Easy to mock for tests
- ✅ Can swap implementations

---

## Event-Driven Architecture

![Event-Driven Architecture](./resources/event-driven-architecture.png)

### Event Flow

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Client    │────>│  Use Case   │────>│   Domain    │────>│   Event     │
│  (Player)   │     │             │     │   Logic     │     │  Publisher  │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                                                                     │
                                                                     │
         ┌───────────────────────────────────────────────────────────┘
         │
         v
┌─────────────────────────────────────────────────────────────────────┐
│                        Event Distribution                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │  Scoped      │  │   Private    │  │   Public     │              │
│  │  Events      │  │   Events     │  │   Events     │              │
│  │ (Game room)  │  │ (1 player)   │  │ (All)        │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
└─────────────────────────────────────────────────────────────────────┘
         │                  │                   │
         v                  v                   v
    ┌─────────┐        ┌─────────┐         ┌─────────┐
    │Player 1 │        │Player 2 │         │Player 3 │
    └─────────┘        └─────────┘         └─────────┘
```

### Event Types

#### 1. Domain Events (Internal)

```java
// GameStartedEvent.java
public record GameStartedEvent(
    String gameId,
    List<String> playerIds,
    int smallBlind,
    int bigBlind,
    Instant occurredAt
) implements DomainEvent<GameStartedEventData> {
    
    @Override
    public String getType() {
        return "GAME_STARTED";
    }
}
```

#### 2. Integration Events (Cross-Boundary)

```java
// PlayerJoinedLobbyEvent.java
public record PlayerJoinedLobbyEvent(
    String lobbyId,
    String playerId,
    String playerName,
    Instant occurredAt
) implements DomainEvent<PlayerJoinedLobbyEventData> {
    
    @Override
    public String getType() {
        return "PLAYER_JOINED_LOBBY";
    }
}
```

#### 3. Private Events (Player-Specific)

```java
// PlayerCardsDealtEvent.java - Only sent to specific player
public record PlayerCardsDealtEvent(
    String gameId,
    String playerId,
    List<Card> cards,
    Instant occurredAt
) implements DomainEvent<PlayerCardsDealtEventData> {
    
    @Override
    public String getType() {
        return "PLAYER_CARDS_DEALT";
    }
    
    @Override
    public String getScope() {
        return "PRIVATE"; // Only to this player's WebSocket
    }
}
```

### Event Publisher (Port)

```java
// DomainEventPublisher.java - Interface (Port)
public interface DomainEventPublisher {
    void publishToScope(String scope, DomainEvent<?> event);
    void publishToPlayer(String playerId, DomainEvent<?> event);
    void publishGlobally(DomainEvent<?> event);
}
```

### Event Publisher (Adapter)

```java
// WebSocketEventPublisher.java - Implementation (Adapter)
public class WebSocketEventPublisher implements DomainEventPublisher {
    
    private final Map<String, Set<Session>> scopedSessions = new ConcurrentHashMap<>();
    private final Gson gson;
    
    @Override
    public void publishToScope(String scope, DomainEvent<?> event) {
        Set<Session> sessions = scopedSessions.get(scope);
        if (sessions == null) return;
        
        String json = gson.toJson(event);
        
        sessions.forEach(session -> {
            try {
                session.getBasicRemote().sendText(json);
            } catch (IOException e) {
                // Handle disconnected session
            }
        });
    }
    
    @Override
    public void publishToPlayer(String playerId, DomainEvent<?> event) {
        // Find session by player ID and send
    }
}
```

### Event Handling in Use Cases

```java
public class PlayerActionUseCase {
    
    public void execute(PlayerActionCommand command) {
        // 1. Execute domain logic
        game.executePlayerAction(player, action, amount);
        gameRepository.save(game);
        
        // 2. Publish action event
        eventPublisher.publishToScope(
            game.getId().toString(),
            new PlayerActionEvent(
                game.getId().toString(),
                player.getId().toString(),
                action,
                amount,
                game.getPot()
            )
        );
        
        // 3. Check for state change and publish
        if (isBettingRoundComplete(game)) {
            game.dealFlop();
            
            eventPublisher.publishToScope(
                game.getId().toString(),
                new GameStateChangedEvent(
                    game.getId().toString(),
                    GameState.FLOP,
                    game.getCurrentPlayer().getId().toString(),
                    game.getPot(),
                    game.getCurrentBet(),
                    game.getCommunityCards()
                )
            );
        }
    }
}
```

### Benefits

✅ **Loose Coupling:** Components communicate through events, not direct calls  
✅ **Scalability:** Can process events asynchronously  
✅ **Audit Trail:** Complete history of what happened  
✅ **Extensibility:** Add new event handlers without modifying existing code

---

## Screaming Architecture

![Screaming Architecture](./resources/screaming-architecture.png)

### Concept

> "Your architecture should scream about the use cases of the application, not about the frameworks."  
> — Robert C. Martin

When you look at the folder structure, you should immediately know **"This is a poker application!"**

### Traditional (Framework-Centric) ❌

```
src/
├── controllers/    ← What's this for? 🤷
├── services/       ← What does it do? 🤷
├── models/         ← What business? 🤷
├── repositories/
└── utils/
```

**Problem:** You can't tell what the application does.

### Screaming Architecture (Business-Centric) ✅

```
com.poker/
├── game/           ← "Handles poker games!"
│   ├── domain/
│   ├── application/
│   └── infrastructure/
├── player/         ← "Manages players!"
│   ├── domain/
│   ├── application/
│   └── infrastructure/
├── lobby/          ← "Handles lobbies!"
│   ├── domain/
│   ├── application/
│   └── infrastructure/
└── shared/         ← "Common utilities"
```

**Benefit:** Instantly understand the business domain.

### Package by Feature, Not by Layer

#### ❌ Package by Layer (Traditional)

```
com.poker/
├── controllers/
│   ├── GameController.java
│   ├── PlayerController.java
│   └── LobbyController.java
├── services/
│   ├── GameService.java
│   ├── PlayerService.java
│   └── LobbyService.java
└── repositories/
    ├── GameRepository.java
    └── PlayerRepository.java
```

**Problems:**
- Features scattered across packages
- Hard to find related code
- Difficult to enforce boundaries

#### ✅ Package by Feature (Screaming)

```
com.poker/
├── game/
│   ├── domain/
│   │   ├── model/Game.java
│   │   ├── repository/GameRepository.java
│   │   └── events/GameStartedEvent.java
│   ├── application/
│   │   ├── StartGameUseCase.java
│   │   └── PlayerActionUseCase.java
│   └── infrastructure/
│       └── persistence/SQLiteGameRepository.java
├── player/
│   └── ... (same structure)
└── lobby/
    └── ... (same structure)
```

**Benefits:**
- All game logic in one place
- Easy to understand boundaries
- Can extract to microservice easily

---

## SOLID Principles

![SOLID Principles](./resources/SOLID.png)

### 1. Single Responsibility Principle (SRP)

> "A class should have only one reason to change."

#### ❌ Violation

```java
public class Game {
    // Multiple responsibilities!
    public void dealCards() { /* game logic */ }
    public void saveToDatabase() { /* persistence */ }
    public void sendWebSocketUpdate() { /* communication */ }
}
```

#### ✅ Solution

```java
// Game.java - Single responsibility: Game logic
public class Game {
    public void dealCards() { /* game logic only */ }
}

// SQLiteGameRepository.java - Single responsibility: Persistence
public class SQLiteGameRepository implements GameRepository {
    public void save(Game game) { /* persistence only */ }
}

// WebSocketEventPublisher.java - Single responsibility: Communication
public class WebSocketEventPublisher implements DomainEventPublisher {
    public void publish(DomainEvent<?> event) { /* communication only */ }
}
```

### 2. Open/Closed Principle (OCP)

> "Open for extension, closed for modification."

#### Example: Hand Evaluation Strategy

```java
// Interface (abstraction) - Closed for modification
public interface HandEvaluationStrategy {
    PokerHand evaluate(List<Card> cards);
}

// Texas Hold'em implementation - Extension
public class TexasHoldemEvaluator implements HandEvaluationStrategy {
    @Override
    public PokerHand evaluate(List<Card> cards) {
        // Texas Hold'em specific logic
    }
}

// Omaha implementation - Extension (future)
public class OmahaEvaluator implements HandEvaluationStrategy {
    @Override
    public PokerHand evaluate(List<Card> cards) {
        // Omaha specific logic
    }
}

// Game uses abstraction
public class Game {
    private HandEvaluationStrategy evaluator;
    
    public Player determineWinner() {
        PokerHand hand = evaluator.evaluate(cards); // Works with any strategy!
    }
}
```

### 3. Liskov Substitution Principle (LSP)

> "Subtypes must be substitutable for their base types."

```java
// GameRepository interface
public interface GameRepository {
    Optional<Game> findById(GameId id);
    void save(Game game);
}

// SQLite implementation
public class SQLiteGameRepository implements GameRepository {
    @Override
    public Optional<Game> findById(GameId id) {
        // SQLite logic
    }
}

// In-Memory implementation (for tests)
public class InMemoryGameRepository implements GameRepository {
    private Map<GameId, Game> games = new HashMap<>();
    
    @Override
    public Optional<Game> findById(GameId id) {
        return Optional.ofNullable(games.get(id));
    }
}

// Use case doesn't care which implementation!
public class StartGameUseCase {
    private GameRepository repository; // Can be SQLite or InMemory
    
    public void execute(StartGameCommand command) {
        Game game = Game.create(/*...*/);
        repository.save(game); // Works with ANY implementation
    }
}
```

### 4. Interface Segregation Principle (ISP)

> "Clients shouldn't depend on interfaces they don't use."

#### ❌ Violation

```java
// Fat interface
public interface GameOperations {
    void dealCards();
    void evaluateHand();
    void saveToDatabase();
    void sendWebSocket();
    void logAction();
    void generateReport();
}
```

#### ✅ Solution

```java
// Segregated interfaces
public interface GameLogic {
    void dealCards();
    void evaluateHand();
}

public interface GamePersistence {
    void save(Game game);
    Game load(GameId id);
}

public interface GameNotifications {
    void notifyPlayers(DomainEvent<?> event);
}

// Clients use only what they need
public class Game implements GameLogic {
    // Only game logic methods
}

public class SQLiteGameRepository implements GamePersistence {
    // Only persistence methods
}
```

### 5. Dependency Inversion Principle (DIP)

> "Depend on abstractions, not concretions."

#### ❌ Violation

```java
public class StartGameUseCase {
    // Depends on concrete classes!
    private SQLiteGameRepository repository = new SQLiteGameRepository();
    private WebSocketEventPublisher publisher = new WebSocketEventPublisher();
}
```

#### ✅ Solution

```java
public class StartGameUseCase {
    // Depends on abstractions (interfaces)
    private final GameRepository repository;
    private final DomainEventPublisher publisher;
    
    // Dependencies injected
    public StartGameUseCase(
        GameRepository repository,
        DomainEventPublisher publisher
    ) {
        this.repository = repository;
        this.publisher = publisher;
    }
}

// Concrete implementations provided at runtime
GameRepository repository = new SQLiteGameRepository();
DomainEventPublisher publisher = new WebSocketEventPublisher();
StartGameUseCase useCase = new StartGameUseCase(repository, publisher);
```

---

## Design Patterns

### 1. Repository Pattern

**Purpose:** Abstract data persistence

```java
// Port (interface)
public interface GameRepository {
    Optional<Game> findById(GameId id);
    void save(Game game);
}

// Adapter
public class SQLiteGameRepository implements GameRepository { /* ... */ }
```

### 2. Strategy Pattern

**Purpose:** Interchangeable algorithms

```java
public interface HandEvaluationStrategy {
    PokerHand evaluate(List<Card> cards);
}

public class TexasHoldemEvaluator implements HandEvaluationStrategy { /* ... */ }
public class OmahaEvaluator implements HandEvaluationStrategy { /* ... */ }
```

### 3. Factory Pattern

**Purpose:** Object creation logic

```java
public class GameFactory {
    public static Game create(
        List<Player> players,
        Blinds blinds,
        LobbyId lobbyId
    ) {
        GameId id = GameId.generate();
        Deck deck = Deck.createShuffled();
        return new Game(id, players, deck, blinds, lobbyId);
    }
}
```

### 4. Observer Pattern (Event-Driven)

**Purpose:** Loose coupling through events

```java
// Subject
public class Game {
    public void start() {
        // Game logic
        eventPublisher.publish(new GameStartedEvent(/*...*/));
    }
}

// Observer
public class WebSocketEventPublisher implements DomainEventPublisher {
    @Override
    public void publish(DomainEvent<?> event) {
        // Notify all observers (WebSocket clients)
    }
}
```

### 5. Command Pattern

**Purpose:** Encapsulate requests

```java
public record PlayerActionCommand(
    String gameId,
    String playerId,
    Action action,
    int amount
) {}

public class PlayerActionUseCase {
    public void execute(PlayerActionCommand command) {
        // Execute command
    }
}
```

### 6. Value Object Pattern

**Purpose:** Immutable objects with value equality

```java
public record Chips(int amount) {
    public Chips {
        if (amount < 0) throw new IllegalArgumentException();
    }
    
    public Chips add(int value) {
        return new Chips(amount + value);
    }
}
```

### 7. Aggregate Pattern

**Purpose:** Transactional consistency boundary

```java
public class Game { // Aggregate Root
    private List<Player> players;  // Managed by root
    private Round currentRound;    // Managed by root
    
    // All modifications through root
    public void executePlayerAction(Player player, Action action, int amount) {
        // Ensures consistency
    }
}
```

---

## Package Structure

### Complete Structure with Explanations

```
com.poker/
│
├── game/                                   # Game Bounded Context
│   ├── domain/                             # Domain Layer
│   │   ├── model/                          # Entities & Aggregates
│   │   │   ├── Game.java                   # Aggregate Root
│   │   │   ├── Player.java                 # Entity (in game context)
│   │   │   ├── Deck.java                   # Entity
│   │   │   ├── Card.java                   # Value Object
│   │   │   ├── Round.java                  # Entity
│   │   │   └── BettingRound.java           # Value Object
│   │   ├── repository/                     # Repository Ports
│   │   │   └── GameRepository.java         # Interface
│   │   ├── events/                         # Domain Events
│   │   │   ├── GameStartedEvent.java
│   │   │   ├── PlayerActionEvent.java
│   │   │   ├── GameStateChangedEvent.java
│   │   │   └── WinnerDeterminedEvent.java
│   │   ├── evaluation/                     # Domain Services
│   │   │   ├── HandEvaluationStrategy.java # Interface
│   │   │   ├── TexasHoldemEvaluator.java
│   │   │   └── PokerHand.java              # Value Object
│   │   └── valueobject/                    # Value Objects
│   │       ├── GameId.java
│   │       ├── GameState.java (enum)
│   │       └── Blinds.java
│   │
│   ├── application/                        # Application Layer
│   │   ├── StartGameUseCase.java           # Use Case
│   │   ├── PlayerActionUseCase.java        # Use Case
│   │   ├── StartNewHandUseCase.java        # Use Case
│   │   ├── dto/                            # DTOs
│   │   │   ├── StartGameDTO.java
│   │   │   ├── PlayerActionDTO.java
│   │   │   └── GameStateDTO.java
│   │   └── command/                        # Commands
│   │       ├── StartGameCommand.java
│   │       └── PlayerActionCommand.java
│   │
│   └── infrastructure/                     # Infrastructure Layer
│       └── persistence/                    # Adapters
│           └── SQLiteGameRepository.java   # Repository Implementation
│
├── player/                                 # Player Bounded Context
│   ├── domain/
│   │   ├── model/
│   │   │   └── Player.java                 # Aggregate Root
│   │   ├── repository/
│   │   │   └── PlayerRepository.java       # Port
│   │   └── valueobject/
│   │       ├── PlayerId.java
│   │       └── PlayerName.java
│   ├── application/
│   │   ├── RegisterPlayerUseCase.java
│   │   └── dto/
│   │       └── RegisterPlayerDTO.java
│   └── infrastructure/
│       └── persistence/
│           └── SQLitePlayerRepository.java
│
├── lobby/                                  # Lobby Bounded Context
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Lobby.java                  # Aggregate Root
│   │   │   └── Seat.java                   # Entity
│   │   ├── repository/
│   │   │   └── LobbyRepository.java
│   │   └── valueobject/
│   │       └── LobbyId.java
│   ├── application/
│   │   ├── CreateLobbyUseCase.java
│   │   ├── JoinLobbyUseCase.java
│   │   └── dto/
│   └── infrastructure/
│       └── persistence/
│           └── SQLiteLobbyRepository.java
│
├── shared/                                 # Shared Kernel
│   ├── domain/
│   │   ├── events/                         # Event Infrastructure
│   │   │   ├── DomainEvent.java            # Base interface
│   │   │   └── DomainEventPublisher.java   # Port
│   │   └── valueobject/                    # Shared Value Objects
│   │       ├── Chips.java
│   │       └── Action.java (enum)
│   │
│   └── infrastructure/
│       ├── database/
│       │   └── DatabaseConnection.java
│       ├── events/
│       │   └── WebSocketEventPublisher.java # Adapter
│       └── websocket/
│           ├── PokerWebSocketEndpoint.java
│           ├── SessionManager.java
│           └── MessageRouter.java
│
└── PokerApplication.java                   # Main Entry Point
```

---

## Layer Responsibilities

### Domain Layer

**Responsibilities:**
- ✅ Business logic and rules
- ✅ Domain entities and value objects
- ✅ Domain services
- ✅ Domain events (definition)

**Dependencies:**
- ❌ ZERO dependencies on other layers
- ❌ NO framework dependencies
- ❌ NO infrastructure concerns

**Example:**
```java
// Pure business logic
public class Game {
    public void dealFlop() {
        if (state != GameState.PRE_FLOP) {
            throw new InvalidGameStateException();
        }
        // Business logic only
    }
}
```

### Application Layer

**Responsibilities:**
- ✅ Orchestrate domain objects
- ✅ Use cases (application logic)
- ✅ DTOs for data transfer
- ✅ Define ports (interfaces)

**Dependencies:**
- ✅ Depends on Domain Layer
- ❌ Does NOT depend on Infrastructure

**Example:**
```java
public class StartGameUseCase {
    private final GameRepository repository; // Port (interface)
    
    public StartGameDTO execute(StartGameCommand command) {
        // Orchestrate domain objects
        Game game = Game.create(/*...*/);
        game.start();
        repository.save(game);
        return StartGameDTO.fromDomain(game);
    }
}
```

### Infrastructure Layer

**Responsibilities:**
- ✅ Implement ports (adapters)
- ✅ Framework integration
- ✅ External system communication
- ✅ Persistence implementation

**Dependencies:**
- ✅ Depends on Application Layer (implements ports)
- ✅ Depends on Domain Layer (uses domain objects)

**Example:**
```java
public class SQLiteGameRepository implements GameRepository {
    @Override
    public void save(Game game) {
        // SQLite specific code
    }
}
```

---

## Data Flow

### Request Flow (Inbound)

```
┌──────────────┐
│   Client     │
│ (WebSocket)  │
└──────┬───────┘
       │ 1. JSON message
       v
┌─────────────────────────────┐
│   Infrastructure Layer       │
│ PokerWebSocketEndpoint.java │
└──────┬──────────────────────┘
       │ 2. Deserialize to Command
       v
┌─────────────────────────────┐
│   Application Layer          │
│ PlayerActionUseCase.java     │
└──────┬──────────────────────┘
       │ 3. Load aggregates
       v
┌─────────────────────────────┐
│   Domain Layer               │
│ Game.executePlayerAction()   │
└──────┬──────────────────────┘
       │ 4. Business logic
       │ 5. Emit domain events
       v
┌─────────────────────────────┐
│   Application Layer          │
│ Save & publish events        │
└──────┬──────────────────────┘
       │ 6. Persist & notify
       v
┌─────────────────────────────┐
│   Infrastructure Layer       │
│ SQLite + WebSocket           │
└──────┬──────────────────────┘
       │ 7. JSON events
       v
┌──────────────┐
│   Clients    │
│ (WebSocket)  │
└──────────────┘
```

### Event Flow (Outbound)

```
Domain Event → Use Case → Event Publisher (Port) → WebSocket Adapter → Clients
```

---

## Testing Strategy

### Testing Pyramid

```
          /\
         /  \        E2E Tests (4)
        /____\       - Full game integration
       /      \      - WebSocket communication
      /  Inte- \     
     /  gration \    Integration Tests (12)
    /   Tests    \   - Use cases with real repositories
   /______________\  - Database interactions
  /                \ 
 /   Unit Tests     \  Unit Tests (48)
/____________________\ - Domain logic
                       - Value objects
                       - Entities
```

### Test Examples

#### Domain Test (Unit)

```java
@Test
void shouldDealFlopWithThreeCommunityCards() {
    // Arrange
    Game game = createGameInPreFlopState();
    
    // Act
    game.dealFlop();
    
    // Assert
    assertEquals(3, game.getCommunityCards().size());
    assertEquals(GameState.FLOP, game.getState());
}
```

#### Use Case Test (Integration)

```java
@Test
void shouldStartGameAndPublishEvent() {
    // Arrange
    GameRepository repository = new InMemoryGameRepository();
    DomainEventPublisher publisher = mock(DomainEventPublisher.class);
    StartGameUseCase useCase = new StartGameUseCase(repository, publisher);
    
    // Act
    StartGameDTO result = useCase.execute(command);
    
    // Assert
    verify(publisher).publishToScope(any(), any(GameStartedEvent.class));
}
```

---

## Performance Considerations

### Optimizations

1. **Connection Pooling:** SQLite connection reuse
2. **Event Batching:** Group WebSocket messages
3. **Lazy Loading:** Load game data on demand
4. **Caching:** In-memory game state cache
5. **Async Processing:** Non-blocking event publishing

### Metrics

- **WebSocket Latency:** < 50ms
- **Game Action Processing:** < 100ms
- **Database Query Time:** < 20ms
- **Memory per Game:** ~2MB

---

## Trade-offs & Decisions

### Decision 1: Hexagonal Architecture

**Trade-off:** More classes and interfaces  
**Benefit:** Framework independence, testability  
**Conclusion:** ✅ Worth it for long-term maintainability

### Decision 2: SQLite vs PostgreSQL

**Trade-off:** SQLite is embedded but less scalable  
**Benefit:** Zero configuration, perfect for prototype  
**Conclusion:** ✅ SQLite now, easy to swap later (thanks to Repository pattern)

### Decision 3: Event-Driven Architecture

**Trade-off:** More complexity than direct method calls  
**Benefit:** Loose coupling, extensibility, audit trail  
**Conclusion:** ✅ Essential for real-time multiplayer game

### Decision 4: WebSocket vs HTTP

**Trade-off:** WebSocket requires persistent connections  
**Benefit:** Real-time bidirectional communication  
**Conclusion:** ✅ Required for poker game experience

### Decision 5: Value Objects (Records)

**Trade-off:** More classes for simple values  
**Benefit:** Type safety, immutability, validation  
**Conclusion:** ✅ Prevents bugs, makes code expressive

---

## Conclusion

This architecture enables:

✅ **Maintainability** - Clear boundaries and responsibilities  
✅ **Testability** - Easy to test with mocks  
✅ **Flexibility** - Swap implementations without breaking domain  
✅ **Scalability** - Loose coupling enables horizontal scaling  
✅ **Business Focus** - Code reflects business domain  

**The architecture is not about frameworks. It's about expressing business intent clearly.**

---

## References

- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/ddd/)
- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Implementing DDD - Vaughn Vernon](https://vaughnvernon.com/)
- [Enterprise Integration Patterns - Martin Fowler](https://martinfowler.com/eaaCatalog/)

---

<div align="center">
  <strong>Architecture crafted with precision and care</strong>
</div>
