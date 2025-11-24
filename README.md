# 🎰 Texas Hold'em Poker Server

> A production-ready multiplayer Texas Hold'em poker server showcasing **Hexagonal Architecture**, **Domain-Driven Design**, and **Screaming Architecture** patterns.

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-blue.svg)](https://alistair.cockburn.us/hexagonal-architecture/)
[![DDD](https://img.shields.io/badge/DDD-Enabled-green.svg)](https://www.domainlanguage.com/ddd/)
[![Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen.svg)]()
[![Tests](https://img.shields.io/badge/Tests-57%20Passing-success.svg)]()

---

## ⚡ Quick Reference

```bash
# Build and test
mvn clean test                        # Run all tests (57 tests)
mvn test jacoco:report                # Generate coverage report

# Run server
docker compose up -d                  # Docker (recommended)
java -jar target/poker-server.jar     # Local build

# Connect as client
telnet localhost 8080                 # Test connection
python3 test_client.py                # Run test client

# Development
docker compose logs -f                # View server logs
mvn clean package                     # Build JAR file
```

---

## 📖 Table of Contents

- [What is This Project?](#-what-is-this-project)
- [Key Features](#-key-features)
- [Quick Start](#-quick-start)
- [Project Structure](#-project-structure)
- [Architecture Overview](#-architecture-overview)
- [Setup & Deployment](#-setup--deployment)
- [Testing](#-testing)
- [How to Contribute](#-how-to-contribute)
- [References](#-references)

---

## 🎯 What is This Project?

This is a **fully functional multiplayer Texas Hold'em poker server** that demonstrates enterprise-level software architecture principles. Originally a monolithic application, it has been completely refactored using:

- **Hexagonal Architecture** (Ports & Adapters)
- **Domain-Driven Design** (DDD)
- **Screaming Architecture** (Feature-first organization)
- **SOLID Principles**

### The Product

A TCP-based poker server supporting:
- ♠️ **Complete Texas Hold'em rules** - All 9 hand rankings, proper betting rounds
- 👥 **Multiplayer gameplay** - Network-based with concurrent games
- 🎮 **Lobby system** - Create and join game rooms
- 🏆 **Player rankings** - Leaderboards and statistics
- 💾 **Persistent state** - SQLite database for all game data
- 🧪 **Comprehensive testing** - 40+ test cases with 85%+ coverage

### Why This Architecture?

This project serves as a **learning resource** and **production template** for building maintainable, testable, and scalable applications. It demonstrates how to:

1. **Separate business logic from technical concerns**
2. **Make your codebase screams what it does, not how**
3. **Write testable code without mocking frameworks**
4. **Organize large applications by business features**
5. **Apply DDD patterns in real-world scenarios**

> **For Architecture Details**: See [`ARCHITECTURE.md`](./ARCHITECTURE.md) for in-depth explanations of Hexagonal Architecture, DDD, Screaming Architecture, and all patterns used.

---

## ✨ Key Features

### Game Features
- ✅ **Complete Poker Rules**: All betting actions (Call, Raise, Fold, Check, All-in)
- ✅ **Hand Evaluation**: Sophisticated algorithm for all 9 poker hands
- ✅ **Dealer Rotation**: Proper button advancement and blind posting
- ✅ **Pot Management**: Main pot, side pots, winner determination
- ✅ **Game States**: Pre-flop → Flop → Turn → River → Showdown

### System Features
- ✅ **Multiplayer Support**: TCP socket server with protocol handler
- ✅ **Player Management**: Registration, authentication, chip tracking
- ✅ **Lobby System**: Create rooms, join games, matchmaking
- ✅ **Leaderboard**: Player rankings by chips and statistics
- ✅ **Persistence**: SQLite database for all state
- ✅ **Real-time Updates**: Event-driven game state notifications

### Technical Features
- ✅ **Clean Architecture**: Perfect separation of concerns
- ✅ **Rich Domain Model**: DDD aggregates, value objects, repositories
- ✅ **Testability**: 40+ tests, 85%+ coverage, no mocking needed
- ✅ **Scalability**: Stateless use cases, concurrent connections
- ✅ **Maintainability**: Feature-first organization, SOLID principles

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (JDK 17 or higher) **← REQUIRED**
- **Maven 3.8+**
- **Docker & Docker Compose** (optional, for containerized setup)

> ⚠️ **Important**: This project requires **Java 17 or higher** due to modern language features (records, switch expressions).  
> If you have Java 11 or older, see [`JAVA17_SETUP.md`](./JAVA17_SETUP.md) for installation instructions.

### Option 1: Run with Docker (Recommended)

```bash
# Start the server (port 8080)
docker compose up -d

# View logs
docker compose logs -f

# Test connection
telnet localhost 8080

# Stop the server
docker compose down
```

**Configuration**: Edit `docker-compose.yml` to change ports or settings. Database persists in `./data/poker.db`.

### Option 2: Run Locally

```bash
# Clone and build
git clone https://github.com/yourusername/aipoker.git
cd aipoker
mvn clean package

# Run tests
mvn test

# Start server
java -jar target/poker-server.jar
```

### Connect as a Player

```bash
# Using telnet
telnet localhost 8080

# Example commands
REGISTER Alice 1000
> REGISTERED playerId=550e8400-e29b-41d4-a716-446655440000 name=Alice chips=1000

CREATE_LOBBY "High Stakes" 6
> LOBBY_CREATED lobbyId=abc123 name=High Stakes maxPlayers=6

HELP
> [Command list]
```

---

## 📁 Project Structure

### High-Level Organization

```
aipoker/
├── src/main/java/com/poker/          # Production code
│   ├── player/                        # Player management feature
│   ├── game/                          # Game logic feature
│   ├── lobby/                         # Lobby system feature
│   └── shared/                        # Shared infrastructure
│
├── src/test/java/com/poker/           # Test code
│   ├── integration/                   # End-to-end tests
│   ├── player/                        # Player tests
│   ├── game/                          # Game tests
│   └── lobby/                         # Lobby tests
│
├── ARCHITECTURE.md                    # Architecture deep-dive
├── README.md                          # This file
└── build.sh                           # Build script
```

### Feature-First Structure (Screaming Architecture)

Each feature follows the same internal structure:

```
feature/                               # e.g., player/, game/, lobby/
├── domain/                            # Pure business logic
│   ├── model/                         # Aggregates and entities
│   ├── valueobject/                   # Immutable value objects
│   ├── repository/                    # Repository interfaces (ports)
│   └── exception/                     # Domain exceptions
│
├── application/                       # Use cases (application services)
│   ├── RegisterPlayerUseCase.java    # Business operations
│   └── GetLeaderboardUseCase.java
│
└── infrastructure/                    # Adapters
    ├── persistence/                   # Database implementations
    └── socket/                        # Network adapters
```

> **Why this structure?** See the [Screaming Architecture](./ARCHITECTURE.md#screaming-architecture) section in ARCHITECTURE.md

---

## 🏗️ Architecture Overview

### The Big Picture

```
┌─────────────────────────────────────────────────────────┐
│                   PRIMARY ADAPTERS                      │
│              (Socket Server, REST API)                  │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  APPLICATION LAYER                      │
│                    (Use Cases)                          │
│  RegisterPlayer | StartGame | PlayerAction | etc.       │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                   DOMAIN LAYER                          │
│              (Business Logic - Pure Java)               │
│  Player | Game | Lobby | Card | Chips | etc.            │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                 SECONDARY ADAPTERS                      │
│           (SQLite, PostgreSQL, Redis, etc.)             │
└─────────────────────────────────────────────────────────┘
```

### Key Architectural Decisions

| Decision | Rationale | Benefit |
|----------|-----------|---------|
| **Hexagonal Architecture** | Isolate domain logic from infrastructure | Easy to test, swap implementations |
| **Feature-First Packages** | Organize by business capabilities | Easy to understand, reduced coupling |
| **Value Objects** | Immutable, self-validating types | Type safety, no invalid states |
| **Rich Domain Model** | Business logic in domain entities | Single source of truth, DRY |
| **Repository Pattern** | Abstract data access | Testable without database |
| **Use Case per Operation** | Single responsibility, explicit | Clear contracts, easy to test |

> **For detailed explanations**: Read [`ARCHITECTURE.md`](./ARCHITECTURE.md) - a complete guide covering Hexagonal Architecture, DDD, all patterns, and design decisions.

---

## 🛠️ Setup & Deployment

### Local Development Setup

1. **Install Java 17+**
   ```bash
   java -version  # Should show version 17 or higher
   ```

2. **Install Maven**
   ```bash
   brew install maven      # macOS
   sudo apt install maven  # Ubuntu
   ```

3. **Clone and Build**
   ```bash
   git clone https://github.com/yourusername/aipoker.git
   cd aipoker
   mvn clean package
   ```

4. **Run Development Server**
   ```bash
   java -jar target/poker-server.jar
   ```

### Docker Development

```bash
# Start server
docker compose up -d

# View logs
docker compose logs -f

# Run tests
docker compose run --rm poker-server mvn test

# Access container shell
docker compose exec poker-server bash

# Access database
docker compose exec poker-server sqlite3 /app/data/poker.db

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
