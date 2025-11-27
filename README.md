# 🎰 Texas Hold'em Poker - Full Stack Application

> A production-ready multiplayer Texas Hold'em poker game with **Next.js 14 + TypeScript frontend** and **Java backend** showcasing **Hexagonal Architecture**, **Domain-Driven Design**, **Screaming Architecture**, and **Event-Driven Architecture** patterns.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Next.js](https://img.shields.io/badge/Next.js-14-black.svg)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue.svg)](https://www.typescriptlang.org/)
[![WebSocket](https://img.shields.io/badge/WebSocket-Jakarta%202.1-blue.svg)](https://jakarta.ee/specifications/websocket/)
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-blue.svg)](https://alistair.cockburn.us/hexagonal-architecture/)
[![DDD](https://img.shields.io/badge/DDD-Enabled-green.svg)](https://www.domainlanguage.com/ddd/)
[![Tests](https://img.shields.io/badge/Tests-57%20Passing-success.svg)]()

---

## ⚡ Quick Start

```bash
# Backend (Java WebSocket Server)
mvn clean package                          # Build
java -jar target/poker-server.jar --server # Run on port 8081

# Frontend (Next.js)
cd client/poker-nextjs
npm install                                # Install dependencies
npm run dev                                # Run on http://localhost:3000

# Testing
mvn clean test                             # Backend tests (57 tests)
mvn test jacoco:report                     # Coverage report
cd client/poker-nextjs && npm test         # Frontend tests

# Docker (Full Stack)
docker compose up -d                       # Start all services
docker compose logs -f                     # View logs
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

This is a **full-stack multiplayer Texas Hold'em poker application** with a modern web UI and enterprise-level backend architecture. It consists of:

### Frontend (Next.js 14 + TypeScript)
- ⚛️ **Modern React** with Server/Client components
- 🎨 **Tailwind CSS** for styling
- 🔌 **Real-time WebSocket** communication
- 📱 **Responsive Design** for mobile/desktop
- 🎮 **Interactive Game Table** with 9 player seats
- 🃏 **Card animations** and smooth UX

### Backend (Java + Hexagonal Architecture)
- 🏗️ **Hexagonal Architecture** (Ports & Adapters)
- 🎯 **Domain-Driven Design** (DDD)
- 📢 **Event-Driven Architecture** (Real-time updates)
- 🔐 **Type-safe JSON Protocol**
- 💾 **SQLite Persistence**
- 🧪 **57+ Tests** with high coverage

### The Product

A complete poker platform supporting:
- ♠️ **Complete Texas Hold'em rules** - All 9 hand rankings, proper betting rounds
- 👥 **Multiplayer gameplay** - Real-time WebSocket communication
- 🎮 **Lobby system** - Create and join game rooms
- 🏆 **Player rankings** - Leaderboards and statistics
- 💾 **Persistent state** - All game data saved
- 🧪 **Comprehensive testing** - Backend and frontend tested

### Why This Architecture?

This project serves as a **learning resource** and **production template** for building maintainable, testable, and scalable full-stack applications. It demonstrates:

1. **Clean separation** between frontend and backend
2. **Type-safe communication** via JSON WebSocket protocol
3. **Business logic isolation** from technical concerns
4. **Real-time multiplayer** with event-driven design
5. **Modern UI/UX** with Next.js and Tailwind CSS
6. **Production-ready** code organization and testing

> **For Architecture Details**: See [`ARCHITECTURE.md`](./ARCHITECTURE.md) for in-depth explanations of Hexagonal Architecture, DDD, Screaming Architecture, Event-Driven Architecture, and all patterns used.

---

## ✨ Key Features

### Game Features
- ✅ **Complete Poker Rules**: All betting actions (Call, Raise, Fold, Check, All-in)
- ✅ **Hand Evaluation**: Sophisticated algorithm for all 9 poker hands
- ✅ **Dealer Rotation**: Proper button advancement and blind posting
- ✅ **Pot Management**: Main pot, side pots, winner determination
- ✅ **Game States**: Pre-flop → Flop → Turn → River → Showdown

### System Features
- ✅ **WebSocket Server**: Real-time bidirectional communication (Jakarta WebSocket API 2.1.1)
- ✅ **JSON Protocol**: Type-safe communication with structured DTOs
- ✅ **Next.js Frontend**: Modern React with TypeScript and Tailwind CSS
- ✅ **Real-time UI**: Instant updates for all game events
- ✅ **Player Management**: Registration, authentication, chip tracking
- ✅ **Lobby System**: Create rooms, join games, real-time player updates
- ✅ **Leaderboard**: Player rankings by chips and statistics
- ✅ **Persistence**: SQLite database for all state
- ✅ **Responsive Design**: Works on desktop and mobile devices
- ✅ **Event-Driven**: Domain events for game state changes and notifications

### Technical Features
- ✅ **Clean Architecture**: Perfect separation of concerns
- ✅ **Rich Domain Model**: DDD aggregates, value objects, repositories
- ✅ **Testability**: 40+ tests, 85%+ coverage, no mocking needed
- ✅ **Scalability**: Stateless use cases, concurrent connections
- ✅ **Maintainability**: Feature-first organization, SOLID principles

---

## 🚀 Quick Start

### Prerequisites
- **Java 21** (JDK 21) **← REQUIRED**
- **Maven 3.8+**
- **Docker & Docker Compose** (optional, for containerized setup)

> ⚠️ **Important**: This project requires **Java 21** with WebSocket support (Jakarta WebSocket API 2.1.1).

### Option 1: Run with Docker (Recommended)

```bash
# Start all services (backend + frontend)
docker compose up -d

# View logs
docker compose logs -f

# Stop all services
docker compose down
```

**Ports:**
- Backend: `ws://localhost:8081/ws/poker`
- Frontend: `http://localhost:3000`
- Database: `./data/poker.db` (SQLite)

### Option 2: Run Locally

**Backend (Java):**
```bash
# Build
mvn clean package

# Run server (port 8081)
java -jar target/poker-server.jar --server

# Run tests
mvn test
mvn test jacoco:report  # Coverage report
```

**Frontend (Next.js):**
```bash
cd client/poker-nextjs

# Install dependencies
npm install

# Run development server (port 3000)
npm run dev

# Build for production
npm run build
npm start
```

### Play the Game

1. **Open browser:** `http://localhost:3000`
2. **Register:** Enter name and starting chips
3. **Create/Join Lobby:** Start a new game or join existing
4. **Play:** Make your moves (CHECK, CALL, RAISE, FOLD)

---

## 🔌 WebSocket Protocol

### Communication Format

**Client → Server (Commands)**
```json
{
  "command": "REGISTER Alice 1000"
}
```

**Server → Client (Events)**
```json
{
  "type": "PLAYER_REGISTERED",
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Alice",
    "chips": 1000
  },
  "timestamp": 1732664400000
}
```

### Available Commands

**Player Management:**
- `REGISTER <name> <chips>` - Register a new player
- `LEADERBOARD` - Get player rankings

**Lobby Operations:**
- `CREATE_LOBBY <name> <maxPlayers> <playerId>` - Create lobby
- `JOIN_LOBBY <lobbyId> <playerId>` - Join lobby
- `LEAVE_LOBBY <lobbyId> <playerId>` - Leave lobby

**Game Actions:**
- `START_GAME <playerIds...> <smallBlind> <bigBlind>` - Start game
- `CHECK <gameId> <playerId>` - Check
- `CALL <gameId> <playerId>` - Call current bet
- `RAISE <gameId> <playerId> <amount>` - Raise bet
- `FOLD <gameId> <playerId>` - Fold hand
- `ALL_IN <gameId> <playerId>` - Go all-in

**Game State:**
- `GET_GAME_STATE <gameId>` - Get current game state
- `GET_MY_CARDS <gameId> <playerId>` - Get player's cards
- `HELP` - List all commands

### Event Types

**Player Events:**
- `PLAYER_REGISTERED` - Player successfully registered

**Lobby Events:**
- `LOBBY_CREATED` - New lobby created
- `LOBBY_JOINED` - Player joined lobby
- `PLAYER_JOINED_LOBBY` - Another player joined
- `PLAYER_LEFT_LOBBY` - Player left lobby

**Game Events:**
- `GAME_STARTED` - Game begins
- `GAME_STATE` - Current game state update
- `PLAYER_ACTION` - Player made an action
- `GAME_ENDED` - Game finished, winner determined

**System Events:**
- `WELCOME` - Connection established
- `SUCCESS` - Operation successful
- `ERROR` - Error occurred
  },
  "timestamp": "2024-11-26T22:00:00.123Z",
  "success": true
}
```

**Available Commands:**
- `REGISTER <name>` - Register a new player
- `CREATE_LOBBY <name> <maxPlayers> <adminPlayerId>` - Create a new lobby
- `JOIN_LOBBY <lobbyId> <playerId>` - Join an existing lobby
- `START_GAME <lobbyId>` - Start the game (admin only)
- `PLAYER_ACTION <gameId> <playerId> <action> [amount]` - Perform game action
- `GET_GAME_STATE <gameId>` - Get current game state
- `HELP` - List all available commands

---

## 📁 Project Structure

### High-Level Organization

```
aipoker/
├── client/poker-nextjs/               # Next.js Frontend
│   ├── src/
│   │   ├── app/                       # Next.js app router
│   │   │   ├── page.tsx               # Registration + Lobby
│   │   │   ├── layout.tsx             # Root layout
│   │   │   └── game/[lobbyId]/        # Game page
│   │   ├── components/                # React components
│   │   │   ├── auth/                  # Registration, connection
│   │   │   ├── lobby/                 # Lobby management
│   │   │   └── game/                  # Game table, cards, actions
│   │   ├── contexts/                  # React Context providers
│   │   │   ├── WebSocketContext.tsx   # WebSocket connection
│   │   │   ├── AuthContext.tsx        # Player state
│   │   │   ├── GameContext.tsx        # Game state
│   │   │   └── LobbyContext.tsx       # Lobby state
│   │   ├── lib/                       # Utilities & types
│   │   │   ├── websocket/             # WebSocket client & commands
│   │   │   └── types/                 # TypeScript types
│   │   └── hooks/                     # Custom React hooks
│   └── public/                        # Static assets
│
├── src/main/java/com/poker/           # Java Backend
│   ├── player/                        # Player management
│   │   ├── domain/                    # Player entities
│   │   ├── application/               # Player use cases
│   │   └── infrastructure/            # Player persistence
│   ├── game/                          # Game logic
│   │   ├── domain/                    # Game entities
│   │   ├── application/               # Game use cases
│   │   └── infrastructure/            # Game persistence
│   ├── lobby/                         # Lobby system
│   │   ├── domain/                    # Lobby entities
│   │   ├── application/               # Lobby use cases
│   │   └── infrastructure/            # Lobby persistence
│   └── shared/                        # Shared infrastructure
│       ├── domain/                    # Common domain objects
│       └── infrastructure/            # WebSocket, events, DB
│
├── src/test/java/com/poker/           # Backend tests
│   ├── integration/                   # End-to-end tests
│   ├── player/                        # Player tests
│   ├── game/                          # Game tests
│   └── lobby/                         # Lobby tests
│
├── ARCHITECTURE.md                    # Architecture deep-dive
├── README.md                          # This file
└── docker-compose.yml                 # Docker orchestration
```

### Feature-First Structure (Backend)

Each backend feature follows hexagonal architecture:

```
feature/                               # e.g., player/, game/, lobby/
├── domain/                            # Pure business logic (no dependencies)
│   ├── model/                         # Aggregates and entities
│   ├── valueobject/                   # Immutable value objects
│   ├── repository/                    # Repository interfaces (ports)
│   └── exception/                     # Domain exceptions
│
├── application/                       # Use cases (orchestration)
│   ├── RegisterPlayerUseCase.java    # Business operations
│   ├── dto/                           # Data Transfer Objects
│   └── GetLeaderboardUseCase.java
│
└── infrastructure/                    # Adapters (implementations)
    └── persistence/                   # Database adapters
        └── SQLitePlayerRepository.java
```

### Frontend Structure (Client-Side Rendering)

All game components use `'use client'` directive for real-time WebSocket communication:

```
src/
├── app/                               # Next.js App Router (SSR + CSR)
│   ├── page.tsx                       # Main page (CSR)
│   └── game/[lobbyId]/page.tsx        # Game table (CSR)
│
├── components/                        # All CSR components
│   ├── auth/RegisterForm.tsx          # Player registration form
│   ├── lobby/LobbyControls.tsx        # Create/Join/Leave lobby
│   └── game/GameTable.tsx             # Interactive game table
│
├── contexts/                          # Global state management
│   ├── WebSocketContext.tsx           # WebSocket connection & events
│   ├── AuthContext.tsx                # Player authentication
│   └── GameContext.tsx                # Game state updates
│
└── lib/
    ├── websocket/
    │   ├── client.ts                  # WebSocket client (JSON protocol)
    │   └── commands.ts                # Command builders
    └── types/
        ├── events.ts                  # WebSocket event types
        ├── game.ts                    # Game-related types
        └── player.ts                  # Player DTOs
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

1. **Install Java 21**
   ```bash
   java -version  # Should show version 21
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

### Running Unit Tests

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

### Local Development Testing - 2 Player Game Flow

Test the complete game with real WebSocket connections between 2 players.

#### Option 1: Using Docker Compose (Recommended)

**Step 1: Start Server**
```bash
# Start server in background
docker compose up -d

# View logs
docker compose logs -f
```

**Step 2: Connect Two Clients**

Open two terminals for the Python clients:

**Terminal 2 (Player 1 - Alice):**
```bash
python3 poker-client.py Alice
```

**Terminal 3 (Player 2 - Bob):**
```bash
python3 poker-client.py Bob
```

Alternatively, open `websocket-client.html` in two browser tabs.

**Step 3: Play a Complete Game**

In Alice's terminal:
```
Alice> REGISTER Alice 1000
Alice> info   # Note your Player ID
```

In Bob's terminal:
```
Bob> REGISTER Bob 1000
Bob> info     # Note your Player ID
```

In Alice's terminal (replace with actual IDs):
```
Alice> START_GAME <alice-id> <bob-id> 10 20
Alice> GET_MY_CARDS <game-id> <alice-id>
Alice> CALL <game-id> <alice-id> 10
```

In Bob's terminal:
```
Bob> GET_MY_CARDS <game-id> <bob-id>
Bob> CHECK <game-id> <bob-id>
```

Progress the game:
```
# Either player can deal
DEAL_FLOP <game-id>
DEAL_TURN <game-id>
DEAL_RIVER <game-id>
DETERMINE_WINNER <game-id>
```

**Available Commands in Python Client:**
- `help` - Show all server commands
- `quick` - Show quick commands with your current IDs
- `info` - Show your Player/Game/Lobby IDs
- `exit` - Disconnect

**Stop Server:**
```bash
docker compose down
```

#### Option 2: Using Local JAR

**Terminal 1 (Server):**
```bash
java -jar target/poker-server.jar --server
```

**Terminal 2 & 3:** Same as above (Python or HTML clients)

#### Option 3: Quick Start Script

```bash
# Automated setup
./start-docker-test.sh
```

This script will:
- Check dependencies
- Start Docker Compose
- Show testing instructions
- Display logs

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

### WebSocket Commands Reference

| Command | Description | Example |
|---------|-------------|---------|
| `REGISTER <name> <chips>` | Register player | `REGISTER Alice 1000` |
| `START_GAME <id1> <id2> <sb> <bb>` | Start game | `START_GAME alice-id bob-id 10 20` |
| `GET_MY_CARDS <game> <player>` | View hole cards | `GET_MY_CARDS game-id player-id` |
| `CHECK <game> <player>` | Check | `CHECK game-id player-id` |
| `CALL <game> <player> <amt>` | Call bet | `CALL game-id player-id 50` |
| `RAISE <game> <player> <amt>` | Raise | `RAISE game-id player-id 100` |
| `FOLD <game> <player>` | Fold | `FOLD game-id player-id` |
| `ALL_IN <game> <player>` | All-in | `ALL_IN game-id player-id` |
| `DEAL_FLOP <game>` | Deal flop | `DEAL_FLOP game-id` |
| `DEAL_TURN <game>` | Deal turn | `DEAL_TURN game-id` |
| `DEAL_RIVER <game>` | Deal river | `DEAL_RIVER game-id` |
| `DETERMINE_WINNER <game>` | Show winner | `DETERMINE_WINNER game-id` |
| `GET_GAME_STATE <game>` | View state | `GET_GAME_STATE game-id` |
| `LEADERBOARD [n]` | Top players | `LEADERBOARD 10` |
| `HELP` | Show commands | `HELP` |

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
