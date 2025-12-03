/**
 * Player action constants matching backend PlayerAction enum.
 * Single source of truth for player action types used across client and server.
 */

// ============================================================================
// Player Actions
// ============================================================================

export const PLAYER_ACTIONS = {
  FOLD: 'FOLD',
  CHECK: 'CHECK',
  CALL: 'CALL',
  RAISE: 'RAISE',
  ALL_IN: 'ALL_IN',
} as const;

// ============================================================================
// Type Exports
// ============================================================================

export type PlayerAction = typeof PLAYER_ACTIONS[keyof typeof PLAYER_ACTIONS];

// ============================================================================
// Utility Arrays
// ============================================================================

export const ALL_PLAYER_ACTIONS = Object.values(PLAYER_ACTIONS);
