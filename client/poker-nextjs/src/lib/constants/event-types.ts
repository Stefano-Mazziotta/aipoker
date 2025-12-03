/**
 * Event type constants matching server domain events.
 * Single source of truth for event type strings used across client and server.
 */

// ============================================================================
// Auth Events
// ============================================================================

export const AUTH_EVENTS = {
  PLAYER_REGISTERED: 'PLAYER_REGISTERED',
} as const;

// ============================================================================
// Lobby Events
// ============================================================================

export const LOBBY_EVENTS = {
  LOBBY_CREATED: 'LOBBY_CREATED',
  PLAYER_JOINED_LOBBY: 'PLAYER_JOINED_LOBBY',
  PLAYER_LEFT_LOBBY: 'PLAYER_LEFT_LOBBY',
} as const;

// ============================================================================
// Game Events
// ============================================================================

export const GAME_EVENTS = {
  GAME_STARTED: 'GAME_STARTED',
  GAME_STATE_CHANGED: 'GAME_STATE_CHANGED',
  PLAYER_ACTION: 'PLAYER_ACTION',
  ROUND_COMPLETED: 'ROUND_COMPLETED',
  CARDS_DEALT: 'DEALT_CARDS',
  WINNER_DETERMINED: 'WINNER_DETERMINED',
} as const;

// ============================================================================
// System Events
// ============================================================================

export const SYSTEM_EVENTS = {
  ERROR: 'ERROR',
  SUCCESS: 'SUCCESS',
  WELCOME: 'WELCOME',
} as const;

// ============================================================================
// Aggregated Event Collections
// ============================================================================

export const ALL_AUTH_EVENT_TYPES = Object.values(AUTH_EVENTS);
export const ALL_LOBBY_EVENT_TYPES = Object.values(LOBBY_EVENTS);
export const ALL_GAME_EVENT_TYPES = Object.values(GAME_EVENTS);
export const ALL_SYSTEM_EVENT_TYPES = Object.values(SYSTEM_EVENTS);

export const ALL_EVENT_TYPES = [
  ...ALL_AUTH_EVENT_TYPES,
  ...ALL_LOBBY_EVENT_TYPES,
  ...ALL_GAME_EVENT_TYPES,
  ...ALL_SYSTEM_EVENT_TYPES,
] as const;

// ============================================================================
// Type Exports
// ============================================================================

export type AuthEventType = typeof AUTH_EVENTS[keyof typeof AUTH_EVENTS];
export type LobbyEventType = typeof LOBBY_EVENTS[keyof typeof LOBBY_EVENTS];
export type GameEventType = typeof GAME_EVENTS[keyof typeof GAME_EVENTS];
export type SystemEventType = typeof SYSTEM_EVENTS[keyof typeof SYSTEM_EVENTS];
export type EventType = AuthEventType | LobbyEventType | GameEventType | SystemEventType;
