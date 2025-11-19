---
description: 'Context about the poker project architecture and structure'
---

# Texas Hold'em Poker Server - Project Context

## Project Overview
A multiplayer Texas Hold'em poker server using Hexagonal Architecture and Domain-Driven Design.

## Tech Stack
- **Language**: Java 17+
- **Database**: SQLite
- **Network**: Java Sockets (TCP)
- **Build**: Maven
- **Architecture**: Hexagonal (Ports & Adapters) + DDD + Screaming Architecture

## Package Structure (Feature-First)

```
com.poker/
├── game/           # Everything about poker games
├── player/         # Everything about players  
├── lobby/          # Everything about game lobbies
├── ranking/        # Everything about leaderboards
└── shared/         # Shared domain models & infrastructure
```

Each feature has:
- `domain/` - Business logic (no external dependencies)
- `application/` - Use cases (orchestration)
- `infrastructure/` - Adapters (DB, network, etc.)

## Key Domain Models

### Shared (`shared/domain/valueobject/`)
- `Card` - Playing card (Rank + Suit)
- `Rank` - Card rank enum (TWO to ACE)
- `Suit` - Card suit enum (♥ ♦ ♣ ♠)
- `Deck` - 52-card deck with shuffle

### Player (`player/domain/model/`)
- `Player` - Aggregate root (player entity)
- `PlayerId` - UUID-based identifier
- `Chips` - Immutable money value object
- `PlayerHand` - Hole cards (max 2)
- `PlayerAction` - Enum (FOLD, CHECK, CALL, RAISE, ALL_IN)

### Game (`game/domain/model/`)
- `Game` - Aggregate root (game entity)
- `GameId` - UUID-based identifier
- `GameState` - Enum (WAITING, PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN, FINISHED)
- `Pot` - Immutable pot value object
- `Blinds` - Small/big blind configuration

### Hand Evaluation (`game/domain/evaluation/`)
- `TexasHoldemEvaluator` - Evaluates best 5-card hand from 7 cards
- `PokerHand` - Immutable hand with comparison
- `HandRank` - Enum of 9 poker hands
- 9 Detector classes (Chain of Responsibility pattern)

## Core Principles

1. **Domain Purity**: Domain layer has NO infrastructure dependencies
2. **Immutability**: All value objects are immutable
3. **Validation**: Domain objects validate themselves
4. **Repository Pattern**: Interfaces in domain, implementations in infrastructure
5. **Use Cases**: Application layer orchestrates domain logic

## Database
SQLite schema in `schema.sql` with tables:
- players, games, game_players, lobbies, lobby_players, player_stats, rankings

## Important Files
- `schema.sql` - Database schema
- `README.md` - Architecture documentation
- `pom.xml` - Maven dependencies (if exists)
