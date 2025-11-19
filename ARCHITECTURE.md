# 🏛️ Architecture Guide

> A comprehensive guide to understanding the architectural patterns, principles, and design decisions in the Texas Hold'em Poker Server.

---

## 📖 Table of Contents

1. [Overview](#overview)
2. [Hexagonal Architecture](#hexagonal-architecture)
3. [Domain-Driven Design (DDD)](#domain-driven-design-ddd)
4. [Screaming Architecture](#screaming-architecture)
5. [Architectural Patterns](#architectural-patterns)
6. [Design Decisions](#design-decisions)
7. [Code Examples](#code-examples)

---

## Overview

This project demonstrates three complementary architectural approaches:

1. **Hexagonal Architecture** (Ports & Adapters) - Structure
2. **Domain-Driven Design** (DDD) - Modeling
3. **Screaming Architecture** - Organization

Together, they create a system that is:
- ✅ **Testable** - Business logic isolated from infrastructure
- ✅ **Maintainable** - Clear separation of concerns
- ✅ **Flexible** - Easy to swap implementations
- ✅ **Understandable** - Structure reveals intent

---

## Hexagonal Architecture

### What is Hexagonal Architecture?

**Hexagonal Architecture** (also called **Ports and Adapters**) is a software design pattern that isolates your application's core business logic from external concerns like databases, user interfaces, and third-party services.

### The Hexagon Metaphor

```
                    ┌─────────────────────┐
                    │   PRIMARY PORTS     │
                    │  (Driving/Input)    │
                    └──────────┬──────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        │     PRIMARY ADAPTERS │ SECONDARY ADAPTERS  │
        │     (TCP Server,     │  (SQLite, Redis,    │
        │      REST API)       │   Email, etc.)      │
        │                      │                      │
        └──────────┬───────────┼───────────┬──────────┘
                   │           │           │
            ┌──────▼───────────▼───────────▼──────┐
            │                                      │
            │      APPLICATION LAYER               │
            │         (Use Cases)                  │
            │                                      │
            └──────────────┬───────────────────────┘
                           │
                    ┌──────▼──────┐
                    │   DOMAIN    │
                    │    LAYER    │
                    │ (Pure Logic)│
                    └─────────────┘
                           │
            ┌──────────────┴───────────────┐
            │                              │
     ┌──────▼──────┐              ┌───────▼──────┐
     │ SECONDARY   │              │  SECONDARY   │
     │   PORTS     │              │    PORTS     │
     │(Repository) │              │ (Email, etc.)│
     └─────────────┘              └──────────────┘
```

### Key Concepts

#### 1. **The Domain (Hexagon Core)**

The center of the hexagon contains your **pure business logic**:
- No dependencies on frameworks
- No database code
- No HTTP/network code
- Just pure Java/business rules

**Example**:
```java
// ✅ Domain: Pure business logic
package com.poker.player.domain.model;

public class Player {
    private final PlayerId id;
    private Chips chips;
    private boolean folded;
    
    public void bet(Chips amount) {
        if (amount.isGreaterThan(chips)) {
            throw new InsufficientChipsException("Cannot bet more than you have");
        }
        chips = chips.subtract(amount);
    }
    
    // No SQL, no HTTP, just business rules
}
```

#### 2. **Ports (Interfaces)**

**Ports** are interfaces that define how the outside world interacts with your domain.

**Two types**:

**A. Primary Ports (Driving/Input Ports)**
- Define what your application **can do**
- Called by external actors (users, other systems)
- Located in the **application layer**

```java
// Primary Port: Use Case Interface
package com.poker.player.application;

public class RegisterPlayerUseCase {
    public RegisterPlayerResponse execute(RegisterPlayerCommand command) {
        // Business logic orchestration
    }
}
```

**B. Secondary Ports (Driven/Output Ports)**
- Define what your application **needs**
- Called by your application to talk to external systems
- Located in the **domain layer** (interfaces only!)

```java
// Secondary Port: Repository Interface
package com.poker.player.domain.repository;

public interface PlayerRepository {
    Optional<Player> findById(PlayerId id);
    void save(Player player);
    List<Player> findAll();
}
// Note: Interface in domain, implementation in infrastructure
```

#### 3. **Adapters (Implementations)**

**Adapters** connect the outside world to your ports.

**A. Primary Adapters (Driving)**
- Implement the driving side
- Examples: REST controllers, CLI commands, Socket handlers

```java
// Primary Adapter: Socket Server Handler
package com.poker.shared.infrastructure.socket;

public class ClientHandler implements Runnable {
    private final RegisterPlayerUseCase registerPlayer;
    
    @Override
    public void run() {
        String command = readFromSocket();
        if (command.startsWith("REGISTER")) {
            // Parse command
            var response = registerPlayer.execute(new RegisterPlayerCommand(...));
            // Send response back
        }
    }
}
```

**B. Secondary Adapters (Driven)**
- Implement the secondary ports
- Examples: SQLite repository, Redis cache, SMTP email sender

```java
// Secondary Adapter: SQLite Implementation
package com.poker.player.infrastructure.persistence;

public class SQLitePlayerRepository implements PlayerRepository {
    private final Connection connection;
    
    @Override
    public void save(Player player) {
        String sql = "INSERT INTO players VALUES (?, ?, ?)";
        // JDBC code here
    }
}
```

### The Dependency Rule

**Critical Rule**: Dependencies point **inward**

```
Infrastructure → Application → Domain
     ↓              ↓            ↑
  Adapters       Use Cases    Pure Logic
  (Depend on)   (Depend on)  (No dependencies)
```

**Why?**
- Domain has **zero** infrastructure dependencies
- Easy to test domain without database/network
- Can swap SQLite for PostgreSQL without touching domain
- Business logic survives technology changes

### Benefits of Hexagonal Architecture

| Benefit | Description | Example |
|---------|-------------|---------|
| **Testability** | Test business logic without database/network | Unit test `Player.bet()` without SQL |
| **Flexibility** | Swap implementations easily | SQLite → PostgreSQL, TCP → WebSocket |
| **Maintainability** | Changes isolated to specific layers | Add new database without touching domain |
| **Technology Independence** | Business logic survives framework changes | Spring → Quarkus without domain changes |
| **Parallel Development** | Teams work on layers independently | Frontend team doesn't wait for DB team |

---

## Domain-Driven Design (DDD)

### What is DDD?

**Domain-Driven Design** is an approach to software development that centers around building a deep understanding of the business domain and reflecting that understanding in code.

### Core Concepts

#### 1. **Ubiquitous Language**

Use the **same terms** in code as business stakeholders use.

```java
// ✅ GOOD: Uses poker terminology
public class Game {
    private Blinds blinds;
    private Pot pot;
    
    public void dealFlop() { ... }
    public void advanceDealer() { ... }
}

// ❌ BAD: Generic technical terms
public class Process {
    private int value1, value2;
    
    public void doStep1() { ... }
    public void moveCounter() { ... }
}
```

#### 2. **Value Objects**

**Definition**: Objects defined by their attributes, not identity. Immutable and interchangeable.

**Characteristics**:
- **Immutable** - Cannot change after creation
- **Compared by value** - Two objects with same values are equal
- **Self-validating** - Constructor enforces invariants
- **No identity** - Identity doesn't matter

**Example**:
```java
// Value Object: Chips
public record Chips(int amount) {
    public Chips {  // Compact constructor
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
    
    public boolean isGreaterThan(Chips other) {
        return this.amount > other.amount;
    }
}

// Usage
Chips chips1 = new Chips(100);
Chips chips2 = new Chips(100);
System.out.println(chips1.equals(chips2)); // true - compared by value

Chips newChips = chips1.add(new Chips(50)); // Immutable - returns new object
```

**More Value Objects in this project**:
- `Card` - Defined by rank and suit
- `PlayerId` - UUID wrapper
- `Hand` - List of cards
- `Blinds` - Small and big blind amounts

**When to use Value Objects?**
- Money amounts (Chips, Bet)
- Dates and times
- Email addresses, phone numbers
- Geographic coordinates
- Any concept measured or described, not tracked by identity

#### 3. **Entities**

**Definition**: Objects defined by their identity, not attributes. Mutable and tracked over time.

**Characteristics**:
- **Identity** - Has unique identifier
- **Mutable** - Can change over time
- **Lifecycle** - Created, modified, deleted
- **Continuity** - Same entity despite attribute changes

**Example**:
```java
// Entity: Player
public class Player {
    private final PlayerId id;  // ← Identity
    private String name;        // Can change
    private Chips chips;        // Can change
    private boolean folded;     // Can change
    
    // Identity-based equality
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Player other)) return false;
        return this.id.equals(other.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

// Usage
Player player1 = new Player(id, "Alice", new Chips(1000));
player1.bet(new Chips(50));  // Chips changed to 950

Player player2 = playerRepository.findById(id);  // Load from DB
System.out.println(player1.equals(player2)); // true - same identity
```

**More Entities in this project**:
- `Game` - Identified by GameId
- `Lobby` - Identified by LobbyId

**When to use Entities?**
- Objects with lifecycle (created, updated, deleted)
- Objects that change over time but remain "the same"
- Objects you need to track and retrieve
- Players, Orders, Accounts, Games

#### 4. **Aggregates**

**Definition**: Cluster of entities and value objects treated as a single unit for data changes.

**Characteristics**:
- **Aggregate Root** - One entity is the entry point
- **Boundary** - Defines what's inside/outside
- **Invariants** - Enforces business rules
- **Transaction** - Changed together or not at all

**Example**:
```java
// Aggregate Root: Game
public class Game {
    // Aggregate Root identity
    private final GameId id;
    
    // Internal entities/value objects
    private List<Player> players;
    private Pot pot;
    private List<Card> communityCards;
    private GameState state;
    private int dealerPosition;
    
    // ✅ Public methods enforce invariants
    public void placeBet(PlayerId playerId, Chips amount) {
        Player player = findPlayer(playerId);
        
        // Business rule: Can't bet when folded
        if (player.isFolded()) {
            throw new IllegalStateException("Folded players cannot bet");
        }
        
        // Business rule: Must be betting round
        if (!state.isBettingRound()) {
            throw new IllegalStateException("Not in betting round");
        }
        
        player.bet(amount);
        pot.add(amount);
    }
    
    // ❌ Private - no direct access to internals
    private Player findPlayer(PlayerId id) {
        return players.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElseThrow();
    }
}

// Usage
Game game = gameRepository.findById(gameId);
game.placeBet(playerId, new Chips(50));  // ✅ Through aggregate root
gameRepository.save(game);

// ❌ DON'T DO THIS: Direct access bypasses invariants
game.getPot().add(new Chips(50));  // No validation!
game.getPlayers().get(0).bet(new Chips(50));  // Could violate rules!
```

**Aggregate Design Rules**:
1. **Reference by ID** - Outside aggregates reference by ID, not direct reference
2. **One Repository** - Each aggregate has one repository
3. **Small Aggregates** - Keep them as small as possible
4. **Enforce Invariants** - All business rules enforced at boundary

**Aggregates in this project**:
- `Player` aggregate (root: Player)
- `Game` aggregate (root: Game, contains: Pot, Round, BettingRound)
- `Lobby` aggregate (root: Lobby, contains: player list)

#### 5. **Repositories**

**Definition**: Abstraction for collection-like access to aggregates.

**Purpose**: Provide the illusion of an in-memory collection of aggregates.

```java
// Repository Interface (in domain)
package com.poker.player.domain.repository;

public interface PlayerRepository {
    // Collection-like interface
    Optional<Player> findById(PlayerId id);
    List<Player> findAll();
    void save(Player player);
    void delete(PlayerId id);
    
    // Query methods using domain concepts
    List<Player> findByChipsGreaterThan(Chips amount);
    Optional<Player> findByName(String name);
}

// Implementation (in infrastructure)
package com.poker.player.infrastructure.persistence;

public class SQLitePlayerRepository implements PlayerRepository {
    private final Connection connection;
    
    @Override
    public void save(Player player) {
        // Translate domain object to database
        String sql = "INSERT INTO players VALUES (?, ?, ?) " +
                    "ON CONFLICT(id) DO UPDATE SET chips = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, player.getId().value());
            stmt.setString(2, player.getName());
            stmt.setInt(3, player.getChips().amount());
            stmt.executeUpdate();
        }
    }
    
    @Override
    public Optional<Player> findById(PlayerId id) {
        // Translate database row to domain object
        String sql = "SELECT * FROM players WHERE id = ?";
        // ... JDBC code ...
        return Optional.of(player);
    }
}
```

**Repository Pattern Rules**:
1. **One per Aggregate** - Each aggregate root has one repository
2. **Domain Interface** - Interface lives in domain layer
3. **Infrastructure Implementation** - Implementation in infrastructure
4. **Collection Metaphor** - Acts like an in-memory collection
5. **Domain Language** - Methods use domain terms, not SQL

#### 6. **Domain Services**

**Definition**: Operations that don't naturally belong to an entity or value object.

**When to use**:
- Operation involves multiple aggregates
- Pure transformation logic
- Algorithm without state

**Example**:
```java
// Domain Service: Hand Evaluator
package com.poker.game.domain.service;

public class HandEvaluator {
    public PokerHand evaluateHand(List<Card> holeCards, List<Card> communityCards) {
        List<Card> allCards = new ArrayList<>(holeCards);
        allCards.addAll(communityCards);
        
        // Complex algorithm to find best 5-card hand
        return findBestHand(allCards);
    }
    
    public Player determineWinner(List<Player> players, List<Card> communityCards) {
        return players.stream()
            .filter(p -> !p.isFolded())
            .max((p1, p2) -> {
                PokerHand hand1 = evaluateHand(p1.getHand(), communityCards);
                PokerHand hand2 = evaluateHand(p2.getHand(), communityCards);
                return hand1.compareTo(hand2);
            })
            .orElseThrow();
    }
}
```

**Domain Services in this project**:
- `HandEvaluator` - Evaluates poker hands
- `HandDetector` implementations - Detect specific hand types

### DDD Layers

```
┌─────────────────────────────────────┐
│     Presentation Layer              │  ← Socket handlers, REST controllers
│    (Primary Adapters)               │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     Application Layer               │  ← Use Cases, DTOs
│    (Orchestration)                  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     Domain Layer                    │  ← Entities, Value Objects, Domain Services
│   (Business Logic)                  │     Repositories (interfaces)
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Infrastructure Layer              │  ← SQLite, PostgreSQL, Redis
│  (Secondary Adapters)               │     Repository implementations
└─────────────────────────────────────┘
```

---

## Screaming Architecture

### What is Screaming Architecture?

**Screaming Architecture** means your folder structure should "scream" what your application does, not what frameworks it uses.

**Bad** (Framework-centric):
```
src/
├── controllers/      ← What is this? HTTP stuff?
├── services/         ← Generic term
├── repositories/     ← Database stuff?
└── models/           ← What kind of models?
```
**Looking at this, can you tell it's a poker application? No!**

**Good** (Domain-centric):
```
src/
├── player/           ← "I manage players!"
├── game/             ← "I run poker games!"
├── lobby/            ← "I handle game lobbies!"
└── ranking/          ← "I track rankings!"
```
**Looking at this, you immediately know: This is a poker application!**

### Our Structure

```
com/poker/
│
├── player/                           # PLAYER Management Feature
│   ├── domain/
│   │   ├── model/
│   │   │   └── Player.java          # Player aggregate
│   │   ├── valueobject/
│   │   │   ├── PlayerId.java
│   │   │   └── Chips.java
│   │   └── repository/
│   │       └── PlayerRepository.java # Port
│   │
│   ├── application/
│   │   ├── RegisterPlayerUseCase.java
│   │   └── GetLeaderboardUseCase.java
│   │
│   └── infrastructure/
│       └── persistence/
│           └── SQLitePlayerRepository.java  # Adapter
│
├── game/                             # GAME Management Feature
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Game.java            # Game aggregate
│   │   │   ├── Pot.java
│   │   │   └── Round.java
│   │   ├── evaluation/              # Hand evaluation subdomain
│   │   │   ├── HandEvaluator.java
│   │   │   ├── PokerHand.java
│   │   │   └── detectors/           # Chain of Responsibility
│   │   └── repository/
│   │       └── GameRepository.java
│   │
│   ├── application/
│   │   ├── StartGameUseCase.java
│   │   ├── PlayerActionUseCase.java
│   │   ├── DealCardsUseCase.java
│   │   └── DetermineWinnerUseCase.java
│   │
│   └── infrastructure/
│       └── persistence/
│           └── SQLiteGameRepository.java
│
├── lobby/                            # LOBBY Management Feature
│   ├── domain/
│   │   ├── model/
│   │   │   └── Lobby.java
│   │   └── repository/
│   │       └── LobbyRepository.java
│   │
│   ├── application/
│   │   ├── CreateLobbyUseCase.java
│   │   └── JoinLobbyUseCase.java
│   │
│   └── infrastructure/
│       └── persistence/
│           └── SQLiteLobbyRepository.java
│
└── shared/                           # SHARED Components
    ├── domain/
    │   ├── valueobject/
    │   │   ├── Card.java            # Used by all features
    │   │   ├── Rank.java
    │   │   ├── Suit.java
    │   │   └── Deck.java
    │   └── exception/
    │       └── DomainException.java
    │
    └── infrastructure/
        ├── database/
        │   └── DatabaseInitializer.java
        └── socket/
            ├── SocketServer.java
            ├── ClientHandler.java
            └── ProtocolHandler.java
```

### Benefits

| Benefit | Description |
|---------|-------------|
| **Discoverability** | New developers instantly understand what the system does |
| **Maintainability** | Changes to "player" feature isolated in `player/` folder |
| **Parallel Development** | Teams work on different features independently |
| **Microservices Ready** | Each feature can become a microservice |
| **Business Alignment** | Code structure matches business language |

### Comparison: Traditional vs Screaming

**Traditional Layered Architecture**:
```
Benefit: Organized by technical concern
Problem: Where is player registration logic?
         - PlayerController?
         - PlayerService?
         - PlayerRepository?
         Scattered across 3+ folders!
```

**Screaming Architecture**:
```
Benefit: Everything about players in one place
Solution: Need to modify player registration?
         Just open player/ folder - everything is there!
```

---

## Architectural Patterns

### 1. Repository Pattern

**Intent**: Abstract data access, provide collection-like interface.

**Structure**:
```
Domain Layer:
  └── PlayerRepository (interface)

Infrastructure Layer:
  └── SQLitePlayerRepository (implementation)
  └── PostgreSQLPlayerRepository (implementation)
  └── InMemoryPlayerRepository (for testing)
```

**Example**:
```java
// Domain defines what it needs
public interface PlayerRepository {
    Optional<Player> findById(PlayerId id);
    void save(Player player);
}

// Use case depends on interface
public class RegisterPlayerUseCase {
    private final PlayerRepository repository;  // ← Interface
    
    public RegisterPlayerResponse execute(RegisterPlayerCommand cmd) {
        Player player = Player.create(cmd.name(), cmd.chips());
        repository.save(player);
        return new RegisterPlayerResponse(player);
    }
}

// Infrastructure provides implementation
public class SQLitePlayerRepository implements PlayerRepository {
    @Override
    public void save(Player player) {
        // SQL code here
    }
}
```

**Benefits**:
- Swap databases without changing business logic
- Test with in-memory implementation
- No SQL in domain layer

### 2. Use Case Pattern

**Intent**: Encapsulate single business operation.

**Structure**:
```java
public class RegisterPlayerUseCase {
    
    // Command: Input DTO
    public record RegisterPlayerCommand(String name, int chips) {}
    
    // Response: Output DTO
    public record RegisterPlayerResponse(PlayerId id, String name, int chips) {}
    
    // Execute: Single public method
    public RegisterPlayerResponse execute(RegisterPlayerCommand command) {
        // 1. Validate input
        // 2. Execute business logic
        // 3. Return response
    }
}
```

**Benefits**:
- One class = one business operation
- Clear input/output contracts
- Easy to test
- Easy to find functionality

### 3. Strategy Pattern (in Hand Evaluation)

**Intent**: Define family of algorithms, make them interchangeable.

**Example**:
```java
// Strategy interface
public interface HandDetector {
    Optional<PokerHand> detect(List<Card> cards);
}

// Concrete strategies
public class StraightFlushDetector implements HandDetector {
    @Override
    public Optional<PokerHand> detect(List<Card> cards) {
        // Algorithm to detect straight flush
    }
}

public class FourOfAKindDetector implements HandDetector {
    @Override
    public Optional<PokerHand> detect(List<Card> cards) {
        // Algorithm to detect four of a kind
    }
}

// Context
public class TexasHoldemEvaluator {
    private final List<HandDetector> detectors = List.of(
        new StraightFlushDetector(),
        new FourOfAKindDetector(),
        new FullHouseDetector(),
        // ... all 9 detectors
    );
    
    public PokerHand evaluate(List<Card> cards) {
        for (HandDetector detector : detectors) {
            Optional<PokerHand> hand = detector.detect(cards);
            if (hand.isPresent()) return hand.get();
        }
        throw new IllegalStateException("No hand detected");
    }
}
```

### 4. Chain of Responsibility (in Hand Evaluation)

Combined with Strategy pattern above - each detector tries in order of hand strength.

---

## Design Decisions

### Decision 1: Why Hexagonal Architecture?

**Problem**: Tightly coupled business logic and infrastructure.

**Solution**: Hexagonal Architecture isolates domain from infrastructure.

**Benefits**:
- Test business logic without database
- Swap databases without changing domain
- Technology-independent domain

**Trade-off**: More interfaces, more files.

**Verdict**: ✅ Worth it for testability and flexibility.

### Decision 2: Why Feature-First Structure?

**Problem**: Traditional layers scatter feature code across folders.

**Solution**: Organize by business feature (player, game, lobby).

**Benefits**:
- New developers understand domain immediately
- Changes isolated to one folder
- Features can become microservices

**Trade-off**: Cross-feature dependencies need careful management.

**Verdict**: ✅ Aligns perfectly with business domain.

### Decision 3: Why Value Objects for Chips?

**Problem**: `int chips` allows negative values, no type safety.

**Solution**: `Chips` value object enforces invariants.

**Benefits**:
```java
// ✅ With Value Object
Chips chips = new Chips(100);
chips = chips.subtract(new Chips(50));  // Type-safe
// new Chips(-10);  ← Throws exception

// ❌ Without Value Object
int chips = 100;
chips = chips - 50;
chips = -10;  // ← Allowed! Bug!
```

**Verdict**: ✅ Prevents entire class of bugs.

### Decision 4: Why UUID for Player ID?

**Problem**: Auto-increment IDs don't work in distributed systems.

**Solution**: UUID-based `PlayerId`.

**Benefits**:
- Generate IDs in application, not database
- Merge data from multiple databases
- No ID collision in distributed system

**Trade-off**: 16 bytes vs 4 bytes (int).

**Verdict**: ✅ Future-proof for scaling.

### Decision 5: Why Reuse Hand Evaluation Code?

**Problem**: Existing hand evaluation code is complex.

**Solution**: Migrate as-is into new structure.

**Benefits**:
- Proven algorithm (already tested)
- Uses proper patterns (Strategy, Chain of Responsibility)
- Handles edge cases (Ace-low straight, tiebreakers)

**Verdict**: ✅ No need to rewrite working code.

---

## Code Examples

### Example 1: Creating a Value Object

```java
/**
 * Value Object: Chips
 * - Immutable
 * - Self-validating
 * - Compared by value
 */
public record Chips(int amount) {
    
    // Compact constructor: validates on creation
    public Chips {
        if (amount < 0) {
            throw new IllegalArgumentException("Chips cannot be negative");
        }
    }
    
    // Business methods return new instances (immutability)
    public Chips add(Chips other) {
        return new Chips(this.amount + other.amount);
    }
    
    public Chips subtract(Chips other) {
        if (other.amount > this.amount) {
            throw new InsufficientChipsException("Not enough chips");
        }
        return new Chips(this.amount - other.amount);
    }
    
    public boolean isGreaterThan(Chips other) {
        return this.amount > other.amount;
    }
}
```

### Example 2: Creating an Aggregate

```java
/**
 * Aggregate Root: Player
 * - Has identity (PlayerId)
 * - Contains value objects (Chips, PlayerHand)
 * - Enforces business rules
 */
public class Player {
    private final PlayerId id;
    private String name;
    private Chips chips;
    private PlayerHand hand;
    private boolean folded;
    
    // Factory method
    public static Player create(String name, int initialChips) {
        return new Player(
            PlayerId.generate(),
            name,
            new Chips(initialChips),
            PlayerHand.empty(),
            false
        );
    }
    
    // Business methods enforce invariants
    public void bet(Chips amount) {
        if (folded) {
            throw new IllegalStateException("Folded player cannot bet");
        }
        if (amount.isGreaterThan(chips)) {
            throw new InsufficientChipsException("Cannot bet more than available");
        }
        chips = chips.subtract(amount);
    }
    
    public void fold() {
        if (folded) {
            throw new IllegalStateException("Already folded");
        }
        folded = true;
    }
    
    public void receiveCard(Card card) {
        hand = hand.addCard(card);
    }
    
    public void receiveChips(Chips amount) {
        chips = chips.add(amount);
    }
    
    // Identity-based equality
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Player other)) return false;
        return this.id.equals(other.id);
    }
}
```

### Example 3: Creating a Use Case

```java
/**
 * Use Case: Register Player
 * - Single responsibility
 * - Clear input/output
 * - Orchestrates domain operations
 */
public class RegisterPlayerUseCase {
    private final PlayerRepository playerRepository;
    
    public RegisterPlayerUseCase(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
    
    // Command: Input DTO
    public record RegisterPlayerCommand(String name, int initialChips) {
        public RegisterPlayerCommand {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Name cannot be empty");
            }
            if (initialChips <= 0) {
                throw new IllegalArgumentException("Initial chips must be positive");
            }
        }
    }
    
    // Response: Output DTO
    public record RegisterPlayerResponse(
        String id,
        String name,
        int chips
    ) {}
    
    // Execute: Single public method
    public RegisterPlayerResponse execute(RegisterPlayerCommand command) {
        // 1. Check if player already exists
        Optional<Player> existing = playerRepository.findByName(command.name());
        if (existing.isPresent()) {
            throw new PlayerAlreadyExistsException(command.name());
        }
        
        // 2. Create player (domain logic)
        Player player = Player.create(command.name(), command.initialChips());
        
        // 3. Persist
        playerRepository.save(player);
        
        // 4. Return response DTO
        return new RegisterPlayerResponse(
            player.getId().value(),
            player.getName(),
            player.getChips().amount()
        );
    }
}
```

### Example 4: Implementing a Repository

```java
/**
 * Repository Implementation: SQLite
 * - Implements domain interface
 * - Translates between domain and database
 */
public class SQLitePlayerRepository implements PlayerRepository {
    private final Connection connection;
    
    @Override
    public Optional<Player> findById(PlayerId id) {
        String sql = "SELECT * FROM players WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.value());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Translate database row to domain object
                return Optional.of(new Player(
                    new PlayerId(rs.getString("id")),
                    rs.getString("name"),
                    new Chips(rs.getInt("chips")),
                    PlayerHand.empty(),
                    false
                ));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Error finding player", e);
        }
    }
    
    @Override
    public void save(Player player) {
        String sql = "INSERT INTO players (id, name, chips) VALUES (?, ?, ?) " +
                    "ON CONFLICT(id) DO UPDATE SET chips = excluded.chips";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Translate domain object to database row
            stmt.setString(1, player.getId().value());
            stmt.setString(2, player.getName());
            stmt.setInt(3, player.getChips().amount());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Error saving player", e);
        }
    }
    
    @Override
    public List<Player> findByChipsGreaterThan(Chips amount) {
        String sql = "SELECT * FROM players WHERE chips > ? ORDER BY chips DESC";
        List<Player> players = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, amount.amount());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                players.add(new Player(
                    new PlayerId(rs.getString("id")),
                    rs.getString("name"),
                    new Chips(rs.getInt("chips")),
                    PlayerHand.empty(),
                    false
                ));
            }
            return players;
        } catch (SQLException e) {
            throw new RepositoryException("Error querying players", e);
        }
    }
}
```

---

## Summary

This architecture combines three powerful approaches:

1. **Hexagonal Architecture** - Isolates business logic from infrastructure
2. **Domain-Driven Design** - Models the business domain accurately
3. **Screaming Architecture** - Makes the domain visible in the structure

Together, they create a system that is:
- ✅ **Testable** - Business logic can be tested in isolation
- ✅ **Maintainable** - Changes are localized and clear
- ✅ **Flexible** - Easy to swap implementations
- ✅ **Understandable** - Structure reveals business intent
- ✅ **Scalable** - Features can evolve independently

**For practical examples**, see the codebase at `src/main/java/com/poker/`.

**For contribution guidelines**, see the main [`README.md`](./README.md).

---

Made with ♠️ ♥️ ♣️ ♦️ | Texas Hold'em Poker Server
