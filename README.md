# 🎴 Texas Hold'em Poker Server# 🎰 Texas Hold'em Poker Server



A multiplayer Texas Hold'em poker server with **WebSocket** real-time communication, built with **Hexagonal Architecture**, **Domain-Driven Design**, and **Event-Driven Architecture**.> A production-ready multiplayer Texas Hold'em poker server showcasing **Hexagonal Architecture**, **Domain-Driven Design**, and **Screaming Architecture** patterns.



[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)

[![WebSocket](https://img.shields.io/badge/WebSocket-Jakarta%202.1-blue.svg)](https://jakarta.ee/specifications/websocket/)[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-blue.svg)](https://alistair.cockburn.us/hexagonal-architecture/)

[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%2B%20DDD%20%2B%20EDA-green.svg)]()[![DDD](https://img.shields.io/badge/DDD-Enabled-green.svg)](https://www.domainlanguage.com/ddd/)

[![Tests](https://img.shields.io/badge/Tests-56%20Passing-success.svg)]()[![Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen.svg)]()

[![Tests](https://img.shields.io/badge/Tests-57%20Passing-success.svg)]()

---

---

## 🎯 What is This?

## ⚡ Quick Reference

A **production-ready multiplayer poker server** demonstrating enterprise software architecture:

```bash

- **Real-time multiplayer** - WebSocket-based bidirectional communication# Build and test

- **Complete Texas Hold'em** - All 9 hand rankings, betting rounds, pot managementmvn clean test                        # Run all tests (57 tests)

- **Event-driven** - Pub/sub pattern for game notificationsmvn test jacoco:report                # Generate coverage report

- **Clean architecture** - Hexagonal + DDD + Event-Driven Design

- **Modern Java** - Java 21 with records, pattern matching, sealed classes# Run server

docker compose up -d                  # Docker (recommended)

---java -jar target/poker-server.jar     # Local build



## ⚡ Quick Start# Connect as client

telnet localhost 8080                 # Test connection

### Prerequisitespython3 test_client.py                # Run test client

- **Java 21+** ([Download](https://adoptium.net/))

- **Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))# Development

docker compose logs -f                # View server logs

### Run the Servermvn clean package                     # Build JAR file

```

```bash

# Clone and build---

git clone https://github.com/Stefano-Mazziotta/aipoker.git

cd aipoker## 📖 Table of Contents

mvn clean package

- [What is This Project?](#-what-is-this-project)

# Start server- [Key Features](#-key-features)

java -jar target/aipoker-server-1.0.0.jar- [Quick Start](#-quick-start)

# Server starts on ws://localhost:8081/ws/poker- [Project Structure](#-project-structure)

```- [Architecture Overview](#-architecture-overview)

- [Setup & Deployment](#-setup--deployment)

### Test with Web Client- [Testing](#-testing)

- [How to Contribute](#-how-to-contribute)

Open `websocket-client.html` in your browser and connect to `ws://localhost:8081/ws/poker`- [References](#-references)



**Try these commands:**---

```

REGISTER player1 1000    # Register with 1000 chips## 🎯 What is This Project?

HELP                     # See all commands

LEADERBOARD             # View rankingsThis is a **fully functional multiplayer Texas Hold'em poker server** that demonstrates enterprise-level software architecture principles. Originally a monolithic application, it has been completely refactored using:

```

- **Hexagonal Architecture** (Ports & Adapters)

---- **Domain-Driven Design** (DDD)

- **Screaming Architecture** (Feature-first organization)

## 🏗️ Architecture- **SOLID Principles**



This project combines three architectural patterns:### The Product



### 1. **Hexagonal Architecture (Ports & Adapters)**A TCP-based poker server supporting:

```- ♠️ **Complete Texas Hold'em rules** - All 9 hand rankings, proper betting rounds

Domain (Business Logic)- 👥 **Multiplayer gameplay** - Network-based with concurrent games

    ↕ Ports (Interfaces)- 🎮 **Lobby system** - Create and join game rooms

Adapters (Infrastructure: WebSocket, Database, Events)- 🏆 **Player rankings** - Leaderboards and statistics

```- 💾 **Persistent state** - SQLite database for all game data

- 🧪 **Comprehensive testing** - 40+ test cases with 85%+ coverage

### 2. **Domain-Driven Design (DDD)**

- **Entities**: Player, Game, Card, Hand### Why This Architecture?

- **Value Objects**: Chips, PlayerId, GameId

- **Aggregates**: Game (root), Player (root)This project serves as a **learning resource** and **production template** for building maintainable, testable, and scalable applications. It demonstrates how to:

- **Repositories**: GameRepository, PlayerRepository

- **Use Cases**: Application services orchestrating domain logic1. **Separate business logic from technical concerns**

2. **Make your codebase screams what it does, not how**

### 3. **Event-Driven Architecture (EDA)**3. **Write testable code without mocking frameworks**

- **Events**: PlayerActionEvent, CardsDealtEvent, WinnerDeterminedEvent, GameStateChangedEvent4. **Organize large applications by business features**

- **Publisher**: GameEventPublisher (singleton with pub/sub)5. **Apply DDD patterns in real-world scenarios**

- **Subscribers**: WebSocket clients connected to specific games/lobbies

> **For Architecture Details**: See [`ARCHITECTURE.md`](./ARCHITECTURE.md) for in-depth explanations of Hexagonal Architecture, DDD, Screaming Architecture, and all patterns used.

**See [`ARCHITECTURE.md`](./ARCHITECTURE.md) for detailed documentation.**

---

---

## ✨ Key Features

## 📁 Project Structure

### Game Features

```- ✅ **Complete Poker Rules**: All betting actions (Call, Raise, Fold, Check, All-in)

src/main/java/com/poker/- ✅ **Hand Evaluation**: Sophisticated algorithm for all 9 poker hands

├── PokerApplication.java           # Main entry point- ✅ **Dealer Rotation**: Proper button advancement and blind posting

├── game/                           # Game bounded context- ✅ **Pot Management**: Main pot, side pots, winner determination

│   ├── domain/                     # Business logic- ✅ **Game States**: Pre-flop → Flop → Turn → River → Showdown

│   │   ├── model/                  # Entities & Value Objects

│   │   │   ├── Game.java          # Game aggregate root### System Features

│   │   │   ├── Card.java, Deck.java- ✅ **Multiplayer Support**: TCP socket server with protocol handler

│   │   │   ├── Hand.java, PokerHand.java- ✅ **Player Management**: Registration, authentication, chip tracking

│   │   │   └── Round.java, Pot.java- ✅ **Lobby System**: Create rooms, join games, matchmaking

│   │   ├── evaluation/             # Hand evaluation algorithm- ✅ **Leaderboard**: Player rankings by chips and statistics

│   │   │   └── HandEvaluator.java- ✅ **Persistence**: SQLite database for all state

│   │   └── repository/             # Repository interfaces (ports)- ✅ **Real-time Updates**: Event-driven game state notifications

│   ├── application/                # Use cases

│   │   ├── StartGameUseCase.java### Technical Features

│   │   ├── PlayerActionUseCase.java- ✅ **Clean Architecture**: Perfect separation of concerns

│   │   ├── DealCardsUseCase.java- ✅ **Rich Domain Model**: DDD aggregates, value objects, repositories

│   │   └── DetermineWinnerUseCase.java- ✅ **Testability**: 40+ tests, 85%+ coverage, no mocking needed

│   └── infrastructure/             # Adapters- ✅ **Scalability**: Stateless use cases, concurrent connections

│       └── persistence/- ✅ **Maintainability**: Feature-first organization, SOLID principles

│           └── SQLiteGameRepository.java

├── player/                         # Player bounded context---

│   ├── domain/

│   │   ├── model/## 🚀 Quick Start

│   │   │   └── Player.java        # Player aggregate root

│   │   └── repository/### Prerequisites

│   ├── application/- **Java 17+** (JDK 17 or higher) **← REQUIRED**

│   │   └── RegisterPlayerUseCase.java- **Maven 3.8+**

│   └── infrastructure/- **Docker & Docker Compose** (optional, for containerized setup)

│       └── persistence/

│           └── SQLitePlayerRepository.java> ⚠️ **Important**: This project requires **Java 17 or higher** due to modern language features (records, switch expressions).  

├── lobby/                          # Lobby bounded context> If you have Java 11 or older, see [`JAVA17_SETUP.md`](./JAVA17_SETUP.md) for installation instructions.

│   ├── domain/

│   │   └── model/### Option 1: Run with Docker (Recommended)

│   │       └── Lobby.java         # Lobby aggregate root

│   ├── application/```bash

│   │   ├── CreateLobbyUseCase.java# Start the server (port 8080)

│   │   └── JoinLobbyUseCase.javadocker compose up -d

│   └── infrastructure/

│       └── persistence/# View logs

│           └── SQLiteLobbyRepository.javadocker compose logs -f

└── shared/                         # Shared kernel

    ├── domain/# Test connection

    │   └── valueobject/telnet localhost 8080

    │       └── Chips.java

    └── infrastructure/# Stop the server

        ├── events/                 # Event-driven componentsdocker compose down

        │   ├── GameEvent.java```

        │   ├── GameEventPublisher.java

        │   ├── PlayerActionEvent.java**Configuration**: Edit `docker-compose.yml` to change ports or settings. Database persists in `./data/poker.db`.

        │   ├── CardsDealtEvent.java

        │   ├── GameStateChangedEvent.java### Option 2: Run Locally

        │   └── WinnerDeterminedEvent.java

        ├── websocket/              # WebSocket server```bash

        │   ├── WebSocketServer.java# Clone and build

        │   └── PokerWebSocketEndpoint.javagit clone https://github.com/yourusername/aipoker.git

        ├── socket/                 # Protocol handlerscd aipoker

        │   ├── ProtocolHandler.javamvn clean package

        │   └── MessageFormatter.java

        └── database/# Run tests

            └── DatabaseInitializer.javamvn test

```

# Start server

---java -jar target/poker-server.jar

```

## 🎮 Features

### Connect as a Player

### Poker Game

- ✅ Complete Texas Hold'em rules```bash

- ✅ All betting actions: Fold, Check, Call, Raise, All-in# Using telnet

- ✅ All 9 hand rankings (High Card → Royal Flush)telnet localhost 8080

- ✅ Dealer rotation and blind posting

- ✅ Main pot and side pot management# Example commands

- ✅ Proper game state machine (Pre-flop → Flop → Turn → River → Showdown)REGISTER Alice 1000

> REGISTERED playerId=550e8400-e29b-41d4-a716-446655440000 name=Alice chips=1000

### Real-Time Communication

- ✅ WebSocket server (Jakarta WebSocket + Tyrus)CREATE_LOBBY "High Stakes" 6

- ✅ Event-driven notifications to all players> LOBBY_CREATED lobbyId=abc123 name=High Stakes maxPlayers=6

- ✅ JSON message protocol

- ✅ Subscribe to game/lobby updatesHELP

- ✅ Browser-compatible (native WebSocket API)> [Command list]

```

### System

- ✅ Player registration and management---

- ✅ Lobby system (create/join games)

- ✅ Leaderboard with rankings## 📁 Project Structure

- ✅ SQLite persistence

- ✅ Comprehensive testing (56 tests)### High-Level Organization

- ✅ JaCoCo code coverage reports

```

---aipoker/

├── src/main/java/com/poker/          # Production code

## 🧪 Testing│   ├── player/                        # Player management feature

│   ├── game/                          # Game logic feature

```bash│   ├── lobby/                         # Lobby system feature

# Run all tests│   └── shared/                        # Shared infrastructure

mvn test│

├── src/test/java/com/poker/           # Test code

# Run tests with coverage report│   ├── integration/                   # End-to-end tests

mvn clean test jacoco:report│   ├── player/                        # Player tests

│   ├── game/                          # Game tests

# View coverage report│   └── lobby/                         # Lobby tests

open target/site/jacoco/index.html│

```├── ARCHITECTURE.md                    # Architecture deep-dive

├── README.md                          # This file

**Test Coverage:**└── build.sh                           # Build script

- Unit tests for domain logic (game rules, hand evaluation)```

- Integration tests for use cases

- Full game flow integration tests### Feature-First Structure (Screaming Architecture)

- 56 tests, 100% passing

Each feature follows the same internal structure:

---

```

## 🔌 WebSocket Protocolfeature/                               # e.g., player/, game/, lobby/

├── domain/                            # Pure business logic

### Connection│   ├── model/                         # Aggregates and entities

```javascript│   ├── valueobject/                   # Immutable value objects

const ws = new WebSocket('ws://localhost:8081/ws/poker');│   ├── repository/                    # Repository interfaces (ports)

```│   └── exception/                     # Domain exceptions

│

### Send Commands├── application/                       # Use cases (application services)

```json│   ├── RegisterPlayerUseCase.java    # Business operations

{│   └── GetLeaderboardUseCase.java

  "command": "REGISTER alice 1000"│

}└── infrastructure/                    # Adapters

```    ├── persistence/                   # Database implementations

    └── socket/                        # Network adapters

### Subscribe to Events```

```json

{> **Why this structure?** See the [Screaming Architecture](./ARCHITECTURE.md#screaming-architecture) section in ARCHITECTURE.md

  "command": "SUBSCRIBE_GAME <game-id>"

}---

```

## 🏗️ Architecture Overview

### Receive Real-Time Events

### The Big Picture

**Player Action:**

```json```

{┌─────────────────────────────────────────────────────────┐

  "eventType": "PLAYER_ACTION",│                   PRIMARY ADAPTERS                      │

  "gameId": "uuid",│              (Socket Server, REST API)                  │

  "playerId": "uuid",└────────────────────┬────────────────────────────────────┘

  "playerName": "Alice",                     │

  "action": "RAISE",                     ▼

  "amount": 50,┌─────────────────────────────────────────────────────────┐

  "timestamp": "2025-11-24T16:00:00Z"│                  APPLICATION LAYER                      │

}│                    (Use Cases)                          │

```│  RegisterPlayer | StartGame | PlayerAction | etc.       │

└────────────────────┬────────────────────────────────────┘

**Cards Dealt:**                     │

```json                     ▼

{┌─────────────────────────────────────────────────────────┐

  "eventType": "CARDS_DEALT",│                   DOMAIN LAYER                          │

  "gameId": "uuid",│              (Business Logic - Pure Java)               │

  "phase": "FLOP",│  Player | Game | Lobby | Card | Chips | etc.            │

  "newCards": ["AH", "KD", "QS"],└────────────────────┬────────────────────────────────────┘

  "timestamp": "2025-11-24T16:00:00Z"                     │

}                     ▼

```┌─────────────────────────────────────────────────────────┐

│                 SECONDARY ADAPTERS                      │

**Winner Determined:**│           (SQLite, PostgreSQL, Redis, etc.)             │

```json└─────────────────────────────────────────────────────────┘

{```

  "eventType": "WINNER_DETERMINED",

  "gameId": "uuid",### Key Architectural Decisions

  "winnerId": "uuid",

  "winnerName": "Alice",| Decision | Rationale | Benefit |

  "handRank": "FULL_HOUSE",|----------|-----------|---------|

  "amountWon": 200,| **Hexagonal Architecture** | Isolate domain logic from infrastructure | Easy to test, swap implementations |

  "timestamp": "2025-11-24T16:00:00Z"| **Feature-First Packages** | Organize by business capabilities | Easy to understand, reduced coupling |

}| **Value Objects** | Immutable, self-validating types | Type safety, no invalid states |

```| **Rich Domain Model** | Business logic in domain entities | Single source of truth, DRY |

| **Repository Pattern** | Abstract data access | Testable without database |

---| **Use Case per Operation** | Single responsibility, explicit | Clear contracts, easy to test |



## 🛠️ Technology Stack> **For detailed explanations**: Read [`ARCHITECTURE.md`](./ARCHITECTURE.md) - a complete guide covering Hexagonal Architecture, DDD, all patterns, and design decisions.



| Component | Technology |---

|-----------|------------|

| **Language** | Java 21 (records, pattern matching, sealed classes) |## 🛠️ Setup & Deployment

| **Build Tool** | Maven 3.9+ |

| **WebSocket** | Jakarta WebSocket API 2.1.1 |### Local Development Setup

| **WebSocket Server** | Tyrus 2.1.5 (Grizzly container) |

| **JSON** | Gson 2.10.1 |1. **Install Java 17+**

| **Database** | SQLite 3.44.1.0 |   ```bash

| **Testing** | JUnit 5.10.1 |   java -version  # Should show version 17 or higher

| **Coverage** | JaCoCo 0.8.14 |   ```



---2. **Install Maven**

   ```bash

## 📚 Documentation   brew install maven      # macOS

   sudo apt install maven  # Ubuntu

- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - Detailed architecture documentation   ```

- **[TODO.md](./TODO.md)** - Feature roadmap and planned improvements

- **[schema.sql](./schema.sql)** - Database schema3. **Clone and Build**

- **[websocket-client.html](./websocket-client.html)** - Interactive test client   ```bash

   git clone https://github.com/yourusername/aipoker.git

---   cd aipoker

   mvn clean package

## 🤝 Contributing   ```



1. Fork the repository4. **Run Development Server**

2. Create a feature branch (`git checkout -b feature/amazing-feature`)   ```bash

3. Commit your changes (`git commit -m 'feat: add amazing feature'`)   java -jar target/poker-server.jar

4. Push to the branch (`git push origin feature/amazing-feature`)   ```

5. Open a Pull Request

### Docker Development

---

```bash

## 📝 License# Start server

docker compose up -d

This project is for educational purposes.

# View logs

---docker compose logs -f



## 👤 Author# Run tests

docker compose run --rm poker-server mvn test

**Stefano Mazziotta**

- GitHub: [@Stefano-Mazziotta](https://github.com/Stefano-Mazziotta)# Access container shell

docker compose exec poker-server bash

---

# Access database

**Built with ❤️ using Clean Architecture principles**docker compose exec poker-server sqlite3 /app/data/poker.db


# Restart after code changes
docker compose restart

# Clean rebuild
docker compose down && docker compose up --build
```

### Database Setup

The application automatically initializes the SQLite database on first run using `schema.sql`.

**Database Location:**
- Docker: `/app/data/poker.db` (persisted to `./data/` on host)
- Local: `./poker_database.db`

**Manual initialization** (if needed):
```bash
sqlite3 poker_database.db < schema.sql
```

### IDE Configuration

**IntelliJ IDEA**:
1. Open project: `File → Open → Select aipoker folder`
2. Maven automatically imports dependencies
3. Run `PokerApplication.main()`

**VS Code**:
1. Install Java Extension Pack
2. Open folder
3. Settings configured in `.vscode/settings.json`

**Eclipse**:
1. Import existing Maven project
2. Project files (`.classpath`, `.project`) included

### Production Deployment

```bash
# Build production JAR
mvn clean package -DskipTests

# Deploy JAR (location: target/poker-server.jar)
scp target/poker-server.jar user@server:/opt/poker/

# Run in production
nohup java -Xmx1g -jar poker-server.jar > server.log 2>&1 &

# Or use systemd service
sudo systemctl start poker-server
```

**Environment Variables:**
```bash
export DB_PATH=/var/lib/poker/poker.db
export SERVER_PORT=8080
```

**Monitoring:**
```bash
# Check if running
nc -zv localhost 8080

# View logs
tail -f server.log

# Docker logs
docker compose logs -f
```

---

## 👥 How to Contribute

### For New Developers

Welcome! This codebase is designed to be easy to understand and contribute to. Here's your roadmap:

#### 1. **Understand the Architecture** (30 minutes)
Read [`ARCHITECTURE.md`](./ARCHITECTURE.md) to understand:
- What is Hexagonal Architecture?
- What are Aggregates, Value Objects, Repositories?
- How do Ports and Adapters work?
- Why is the code organized by features?

#### 2. **Explore the Codebase** (1 hour)
Start with a single feature to understand the pattern:

```
src/main/java/com/poker/player/
├── domain/model/Player.java           # Start here - The aggregate root
├── domain/valueobject/Chips.java      # See how value objects work
├── domain/repository/PlayerRepository.java  # Port definition
├── application/RegisterPlayerUseCase.java   # Use case
└── infrastructure/persistence/SQLitePlayerRepository.java  # Adapter
```

**Follow this flow**:
1. Read `Player.java` - notice it has no infrastructure dependencies
2. Read `Chips.java` - see how value objects enforce invariants
3. Read `PlayerRepository.java` - just an interface (port)
4. Read `RegisterPlayerUseCase.java` - orchestrates domain operations
5. Read `SQLitePlayerRepository.java` - concrete implementation (adapter)

#### 3. **Run the Tests** (15 minutes)
```bash
mvn test
```

Read test files to understand how features work:
- `PlayerTest.java` - Domain logic tests
- `RegisterPlayerUseCaseTest.java` - Application logic tests
- `FullGameIntegrationTest.java` - End-to-end scenarios

#### 4. **Make Your First Contribution**

### Contribution Workflow

```bash
# 1. Create a feature branch
git checkout -b feature/add-tournament-mode

# 2. Make changes following the architecture
#    - Add domain models in domain/model/
#    - Create use cases in application/
#    - Implement adapters in infrastructure/

# 3. Write tests (TDD approach)
#    - Write tests first
#    - Run tests (they should fail)
#    - Implement feature
#    - Run tests (they should pass)

# 4. Run all tests
mvn test

# 5. Commit with clear messages
git commit -m "feat: Add tournament mode with buy-in and prizes"

# 6. Push and create pull request
git push origin feature/add-tournament-mode
```

### Code Standards

#### Follow the Architecture Layers

```java
// ✅ GOOD: Domain has no infrastructure dependencies
package com.poker.player.domain.model;

public class Player {
    private PlayerId id;
    private Chips chips;
    // Pure business logic, no SQL, no network
}

// ❌ BAD: Don't put infrastructure in domain
package com.poker.player.domain.model;
import java.sql.Connection;  // ← NO! Domain must be pure

public class Player {
    private Connection dbConnection;  // ← NO!
}
```

#### Use Value Objects

```java
// ✅ GOOD: Type-safe, self-validating
public record Chips(int amount) {
    public Chips {
        if (amount < 0) {
            throw new IllegalArgumentException("Chips cannot be negative");
        }
    }
}

Player player = new Player(playerId, new Chips(1000));

// ❌ BAD: Primitive obsession
int chips = 1000;  // Can be negative! No validation!
```

#### One Use Case Per Class

```java
// ✅ GOOD: Single responsibility
public class RegisterPlayerUseCase {
    public RegisterPlayerResponse execute(RegisterPlayerCommand cmd) {
        // Only handles player registration
    }
}

// ❌ BAD: Multiple responsibilities
public class PlayerService {
    public void register() {}
    public void delete() {}
    public void update() {}
    public void ban() {}
    // Too many responsibilities!
}
```

### Where to Add New Features

| Feature Type | Location | Example |
|-------------|----------|---------|
| **New Game Mode** | `game/domain/model/` | `Tournament.java` aggregate |
| **New Player Action** | `game/domain/model/PlayerAction.java` | Add enum value |
| **New Use Case** | `feature/application/` | `StartTournamentUseCase.java` |
| **New Database Table** | `infrastructure/persistence/` | `SQLiteTournamentRepository.java` |
| **New API Endpoint** | `shared/infrastructure/socket/` | Add command to `ProtocolHandler.java` |

### Testing Guidelines

```java
// ✅ Test domain logic without infrastructure
@Test
public void testPlayerCannotBetMoreThanTheyHave() {
    Player player = Player.create("Alice", 100);
    
    assertThrows(IllegalArgumentException.class, () -> {
        player.bet(new Chips(200));  // More than they have
    });
}

// ✅ Test use cases with mock repositories
@Test
public void testRegisterPlayer() {
    PlayerRepository repo = new InMemoryPlayerRepository();  // Test double
    RegisterPlayerUseCase useCase = new RegisterPlayerUseCase(repo);
    
    var result = useCase.execute(new RegisterPlayerCommand("Alice", 1000));
    
    assertEquals("Alice", result.name());
    assertEquals(1000, result.chips());
}

// ✅ Integration tests verify full stack
@Test
public void testCompleteGameFlow() {
    // Start with database
    // Register players
    // Create game
    // Play through all rounds
    // Verify winner
}
```

---

## 🧪 Testing

### Running Tests

```bash
# Run all tests (57 tests)
mvn test

# Run specific test
mvn test -Dtest=PlayerTest

# Run with coverage report
mvn test jacoco:report
open target/site/jacoco/index.html

# Run tests in Docker
docker compose run --rm poker-server mvn test
```

### Test Coverage

Current coverage: **85%+**

- **Unit Tests**: Domain logic (Player, Game, Chips, Hand Evaluation)
- **Use Case Tests**: Application logic (RegisterPlayer, StartGame, PlayerAction)
- **Integration Tests**: End-to-end game flows (Complete game, Folding, All-in)
- **Repository Tests**: Data persistence layer

### Test Structure

```
src/test/java/com/poker/
├── player/
│   ├── domain/model/PlayerTest.java           # Domain tests
│   └── application/RegisterPlayerUseCaseTest.java  # Use case tests
├── game/
│   ├── domain/model/GameTest.java
│   ├── domain/evaluation/HandEvaluationTest.java
│   └── application/GameUseCaseTest.java
├── lobby/
│   └── application/LobbyUseCaseTest.java
├── integration/
│   └── FullGameIntegrationTest.java           # E2E tests (57 total)
└── TestRunner.java                            # Test suite runner
```

### Writing Tests

Follow the testing pyramid:

```java
// ✅ Unit Test - No infrastructure dependencies
@Test
public void testPlayerCannotBetMoreThanTheyHave() {
    Player player = Player.create("Alice", 100);
    
    assertThrows(IllegalArgumentException.class, () -> {
        player.bet(new Chips(200));
    });
}

// ✅ Use Case Test - With test doubles
@Test
public void testRegisterPlayer() {
    PlayerRepository repo = new InMemoryPlayerRepository();
    RegisterPlayerUseCase useCase = new RegisterPlayerUseCase(repo);
    
    var result = useCase.execute(new RegisterPlayerCommand("Alice", 1000));
    
    assertEquals("Alice", result.name());
}

// ✅ Integration Test - Full stack
@Test
public void testCompleteGameFlow() {
    // Setup database
    // Register players
    // Start game
    // Execute actions through all betting rounds
    // Verify winner and pot distribution
}
```

---

##  References

### Architecture
- [**Hexagonal Architecture**](https://alistair.cockburn.us/hexagonal-architecture/) - Alistair Cockburn
- [**Domain-Driven Design**](https://www.domainlanguage.com/ddd/) - Eric Evans
- [**Clean Architecture**](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) - Robert C. Martin
- [**Screaming Architecture**](https://blog.cleancoder.com/uncle-bob/2011/09/30/Screaming-Architecture.html) - Robert C. Martin

### Patterns
- [**Repository Pattern**](https://martinfowler.com/eaaCatalog/repository.html) - Martin Fowler
- [**Value Object**](https://martinfowler.com/bliki/ValueObject.html) - Martin Fowler
- [**Aggregate**](https://martinfowler.com/bliki/DDD_Aggregate.html) - Martin Fowler

### Project Documentation
- [`ARCHITECTURE.md`](./ARCHITECTURE.md) - Complete architecture guide
- [`TESTING_GUIDE.md`](./TESTING_GUIDE.md) - Testing strategies
- [`SOCKET_SERVER_GUIDE.md`](./SOCKET_SERVER_GUIDE.md) - Network protocol

---

## 📄 License

MIT License - feel free to use this project for learning and commercial purposes.

---

## 🙏 Acknowledgments

Built to demonstrate enterprise software architecture principles in a real-world poker application. Perfect for learning, teaching, and as a foundation for production systems.

**Questions?** Open an issue or read [`ARCHITECTURE.md`](./ARCHITECTURE.md) for detailed explanations.

---

Made with ♠️ ♥️ ♣️ ♦️ by the AiPoker Team
