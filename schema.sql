-- Texas Hold'em Poker Server - SQLite Schema
-- Hexagonal Architecture Migration
-- Created: 2025-11-18

-- ====================
-- PLAYERS
-- ====================
CREATE TABLE IF NOT EXISTS players (
    id TEXT PRIMARY KEY,              -- UUID as string
    name TEXT NOT NULL UNIQUE,        -- Player username
    chips INTEGER NOT NULL,           -- Current chip count
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_players_name ON players(name);

-- ====================
-- GAMES
-- ====================
CREATE TABLE IF NOT EXISTS games (
    id TEXT PRIMARY KEY,              -- UUID as string
    state TEXT NOT NULL CHECK(state IN ('WAITING', 'PRE_FLOP', 'FLOP', 'TURN', 'RIVER', 'SHOWDOWN', 'FINISHED')),
    small_blind INTEGER NOT NULL,
    big_blind INTEGER NOT NULL,
    pot INTEGER DEFAULT 0,
    dealer_position INTEGER,
    community_card_1 TEXT,            -- Serialized card (e.g., "A♥")
    community_card_2 TEXT,
    community_card_3 TEXT,
    community_card_4 TEXT,
    community_card_5 TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    finished_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_games_state ON games(state);

-- ====================
-- GAME PLAYERS (Many-to-Many)
-- ====================
CREATE TABLE IF NOT EXISTS game_players (
    game_id TEXT NOT NULL,
    player_id TEXT NOT NULL,
    position INTEGER NOT NULL,         -- Seat position (0-8)
    chips_at_start INTEGER NOT NULL,   -- Chips when joined game
    current_chips INTEGER NOT NULL,    -- Current chips in game
    is_folded BOOLEAN DEFAULT 0,
    is_all_in BOOLEAN DEFAULT 0,
    current_bet INTEGER DEFAULT 0,     -- Current round bet
    hole_card_1 TEXT,                  -- Serialized card (e.g., "A♥")
    hole_card_2 TEXT,
    PRIMARY KEY (game_id, player_id),
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_game_players_game ON game_players(game_id);
CREATE INDEX IF NOT EXISTS idx_game_players_player ON game_players(player_id);

-- ====================
-- GAME HISTORY (Audit Trail)
-- ====================
CREATE TABLE IF NOT EXISTS game_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id TEXT NOT NULL,
    round TEXT NOT NULL,               -- PRE_FLOP, FLOP, TURN, RIVER
    player_id TEXT NOT NULL,
    action TEXT NOT NULL,              -- FOLD, CHECK, CALL, RAISE, ALL_IN
    amount INTEGER DEFAULT 0,
    pot_after_action INTEGER,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_game_history_game ON game_history(game_id);

-- ====================
-- LOBBIES
-- ====================
CREATE TABLE IF NOT EXISTS lobbies (
    id TEXT PRIMARY KEY,              -- UUID as string
    name TEXT NOT NULL UNIQUE,        -- Lobby name (e.g., "High Stakes", "Beginners")
    max_players INTEGER NOT NULL CHECK(max_players BETWEEN 2 AND 9),
    buy_in INTEGER NOT NULL,          -- Entry cost
    small_blind INTEGER NOT NULL,
    big_blind INTEGER NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('OPEN', 'FULL', 'STARTED', 'CLOSED')),
    current_players INTEGER DEFAULT 0,
    game_id TEXT,                     -- Associated game if started
    started BOOLEAN DEFAULT 0,        -- Compatibility with SQLiteLobbyRepository
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_lobbies_status ON lobbies(status);
CREATE INDEX IF NOT EXISTS idx_lobbies_name ON lobbies(name);

-- ====================
-- LOBBY PLAYERS (Many-to-Many)
-- ====================
CREATE TABLE IF NOT EXISTS lobby_players (
    lobby_id TEXT NOT NULL,
    player_id TEXT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ready BOOLEAN DEFAULT 0,
    PRIMARY KEY (lobby_id, player_id),
    FOREIGN KEY (lobby_id) REFERENCES lobbies(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lobby_players_lobby ON lobby_players(lobby_id);
CREATE INDEX IF NOT EXISTS idx_lobby_players_player ON lobby_players(player_id);

-- ====================
-- PLAYER STATISTICS
-- ====================
CREATE TABLE IF NOT EXISTS player_stats (
    player_id TEXT PRIMARY KEY,
    games_played INTEGER DEFAULT 0,
    games_won INTEGER DEFAULT 0,
    games_lost INTEGER DEFAULT 0,
    total_winnings INTEGER DEFAULT 0,  -- Net winnings (can be negative)
    total_hands_played INTEGER DEFAULT 0,
    hands_won INTEGER DEFAULT 0,
    biggest_pot_won INTEGER DEFAULT 0,
    best_hand_rank TEXT,              -- Best hand ever achieved
    last_played_at TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
);

-- ====================
-- RANKINGS (Leaderboard)
-- ====================
CREATE TABLE IF NOT EXISTS rankings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id TEXT NOT NULL,
    rank INTEGER NOT NULL,
    score INTEGER NOT NULL,           -- Calculated score based on wins/winnings
    period TEXT NOT NULL CHECK(period IN ('ALL_TIME', 'MONTHLY', 'WEEKLY')),
    period_start DATE,
    period_end DATE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_rankings_period ON rankings(period, rank);
CREATE INDEX IF NOT EXISTS idx_rankings_player ON rankings(player_id);

-- ====================
-- TRIGGERS
-- ====================

-- Auto-update players.updated_at on UPDATE
DROP TRIGGER IF EXISTS update_players_timestamp;
CREATE TRIGGER update_players_timestamp 
AFTER UPDATE ON players
BEGIN
    UPDATE players SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Update lobby current_players count
DROP TRIGGER IF EXISTS increment_lobby_players;
CREATE TRIGGER increment_lobby_players
AFTER INSERT ON lobby_players
BEGIN
    UPDATE lobbies 
    SET current_players = (SELECT COUNT(*) FROM lobby_players WHERE lobby_id = NEW.lobby_id)
    WHERE id = NEW.lobby_id;
END;

DROP TRIGGER IF EXISTS decrement_lobby_players;
CREATE TRIGGER decrement_lobby_players
AFTER DELETE ON lobby_players
BEGIN
    UPDATE lobbies 
    SET current_players = (SELECT COUNT(*) FROM lobby_players WHERE lobby_id = OLD.lobby_id)
    WHERE id = OLD.lobby_id;
END;

-- ====================
-- INITIAL DATA (Optional)
-- ====================

-- Create default lobby
INSERT OR IGNORE INTO lobbies (id, name, max_players, buy_in, small_blind, big_blind, status)
VALUES ('00000000-0000-0000-0000-000000000001', 'Main Lobby', 6, 1000, 10, 20, 'OPEN');

-- ====================
-- VIEWS
-- ====================

-- Current leaderboard view
CREATE VIEW IF NOT EXISTS v_leaderboard AS
SELECT 
    p.id,
    p.name,
    p.chips,
    ps.games_played,
    ps.games_won,
    ps.total_winnings,
    CASE 
        WHEN ps.games_played > 0 THEN CAST(ps.games_won AS FLOAT) / ps.games_played * 100
        ELSE 0
    END AS win_rate
FROM players p
LEFT JOIN player_stats ps ON p.id = ps.player_id
ORDER BY ps.total_winnings DESC, ps.games_won DESC;

-- Active games view
CREATE VIEW IF NOT EXISTS v_active_games AS
SELECT 
    g.id,
    g.state,
    g.pot,
    COUNT(gp.player_id) AS player_count,
    g.created_at
FROM games g
LEFT JOIN game_players gp ON g.id = gp.game_id
WHERE g.state != 'FINISHED'
GROUP BY g.id;

-- ====================
-- COMMENTS
-- ====================

-- This schema supports:
-- 1. Player management with statistics tracking
-- 2. Game state persistence across rounds
-- 3. Lobby system for matchmaking
-- 4. Complete game history for audit/replay
-- 5. Rankings and leaderboards
-- 6. Referential integrity with cascading deletes
-- 7. Indexing for performance
-- 8. Triggers for data consistency
