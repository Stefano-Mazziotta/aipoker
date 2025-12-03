import { PlayerStateDTO } from './player';

// Game round phases
export type GameRound = 'pre-flop' | 'flop' | 'turn' | 'river' | 'showdown';

// Player actions
export type GameAction = 'CHECK' | 'CALL' | 'RAISE' | 'FOLD' | 'ALL_IN';

// Game state DTO matching Java backend
export interface GameStateDTO {
  gameId: string;
  pot: number;
  currentBet: number;
  communityCards: string[];
  round: GameRound;
  players: PlayerStateDTO[];
  currentPlayerId?: string;
  winners?: WinnerDTO[];
}

export interface WinnerDTO {
  playerId: string;
  playerName: string;
  amount: number;
  handRank: string;
}

// Card representation
export interface Card {
  rank: string;  // e.g., "ACE", "KING", "TWO"
  suit: string;  // e.g., "HEARTS", "DIAMONDS", "CLUBS", "SPADES"
}

// Card suit symbols
export const SUIT_SYMBOLS: Record<string, string> = {
  HEARTS: '♥️',
  DIAMONDS: '♦️',
  CLUBS: '♣️',
  SPADES: '♠️',
};

// Card suit colors
export const SUIT_COLORS: Record<string, string> = {
  HEARTS: 'text-red-600',
  DIAMONDS: 'text-red-600',
  CLUBS: 'text-black',
  SPADES: 'text-black',
};

// Map backend suit symbols to suit names
export const SYMBOL_TO_SUIT: Record<string, string> = {
  '♥': 'HEARTS',
  '♦': 'DIAMONDS',
  '♣': 'CLUBS',
  '♠': 'SPADES',
};

/**
 * Parse card string from backend format (e.g., "ACE♥" or "ACE_HEARTS") to standard format
 */
export function parseCard(card: string): string {
  if (!card) return '';
  
  // Already in format "RANK_SUIT"
  if (card.includes('_')) return card;
  
  // Parse "RANK♥" format
  const lastChar = card.slice(-1);
  const suit = SYMBOL_TO_SUIT[lastChar];
  const rank = card.slice(0, -1);
  
  return suit ? `${rank}_${suit}` : card;
}
