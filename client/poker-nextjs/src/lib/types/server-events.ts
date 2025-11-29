/**
 * TypeScript types mirroring Java server domain events.
 * These ensure type safety across the WebSocket communication layer.
 */

import { Card, GameRound } from './game';
import { PlayerInfo } from './player';

// ============================================================================
// Base Event Structure
// ============================================================================

export interface DomainEvent {
  eventType: string;
  timestamp: number;
}

// ============================================================================
// Lobby Events
// ============================================================================

export interface PlayerJoinedLobbyEvent extends DomainEvent {
  eventType: 'PLAYER_JOINED_LOBBY';
  lobbyId: string;
  playerId: string;
  playerName: string;
  playerChips: number;
  currentPlayerCount: number;
  maxPlayers: number;
}

export interface PlayerLeftLobbyEvent extends DomainEvent {
  eventType: 'PLAYER_LEFT_LOBBY';
  lobbyId: string;
  playerId: string;
  playerName: string;
  currentPlayerCount: number;
  maxPlayers: number;
}

// ============================================================================
// Game Events
// ============================================================================

export interface GameStateChangedEvent extends DomainEvent {
  eventType: 'GAME_STATE_CHANGED';
  gameId: string;
  newState: string;
  currentPlayerId: string;
  currentPlayerName: string;
  pot: number;
}

export interface PlayerActionEvent extends DomainEvent {
  eventType: 'PLAYER_ACTION';
  gameId: string;
  playerId: string;
  playerName: string;
  action: 'FOLD' | 'CHECK' | 'CALL' | 'RAISE' | 'ALL_IN';
  amount: number;
  newPot: number;
  currentBet: number;
}

export interface CardsDealtEvent extends DomainEvent {
  eventType: 'CARDS_DEALT';
  gameId: string;
  phase: string;
  newCards: string[];
  allCommunityCards: string[];
}

export interface WinnerDeterminedEvent extends DomainEvent {
  eventType: 'WINNER_DETERMINED';
  gameId: string;
  winnerId: string;
  winnerName: string;
  handRank: string;
  amountWon: number;
}

// ============================================================================
// WebSocket Response Events (Not Domain Events)
// ============================================================================

export interface PlayerRegisteredEvent {
  eventType: 'PLAYER_REGISTERED';
  playerId: string;
  playerName: string;
  chips: number;
}

export interface LobbyCreatedEvent {
  eventType: 'LOBBY_CREATED';
  lobbyId: string;
  adminId: string;
  players: PlayerInfo[];
  maxPlayers: number;
}

export interface LobbyJoinedEvent {
  eventType: 'LOBBY_JOINED';
  lobbyId: string;
  playerId: string;
  players: PlayerInfo[];
}

export interface GameStartedEvent {
  eventType: 'GAME_STARTED';
  gameId: string;
  lobbyId: string;
  playerIds: string[];
  smallBlind: number;
  bigBlind: number;
}

export interface ErrorEvent {
  eventType: 'ERROR';
  message: string;
  code?: string;
}

export interface SuccessEvent {
  eventType: 'SUCCESS';
  message: string;
}

export interface WelcomeEvent {
  eventType: 'WELCOME';
  message: string;
  sessionId: string;
}

// ============================================================================
// Union Type for All Events
// ============================================================================

export type ServerEvent =
  // Lobby Events
  | PlayerJoinedLobbyEvent
  | PlayerLeftLobbyEvent
  // Game Events
  | GameStateChangedEvent
  | PlayerActionEvent
  | CardsDealtEvent
  | WinnerDeterminedEvent
  // Response Events
  | PlayerRegisteredEvent
  | LobbyCreatedEvent
  | LobbyJoinedEvent
  | GameStartedEvent
  | ErrorEvent
  | SuccessEvent
  | WelcomeEvent;

// ============================================================================
// Type Guards for Event Discrimination
// ============================================================================

export function isPlayerJoinedLobbyEvent(event: ServerEvent): event is PlayerJoinedLobbyEvent {
  return event.eventType === 'PLAYER_JOINED_LOBBY';
}

export function isPlayerLeftLobbyEvent(event: ServerEvent): event is PlayerLeftLobbyEvent {
  return event.eventType === 'PLAYER_LEFT_LOBBY';
}

export function isGameStateChangedEvent(event: ServerEvent): event is GameStateChangedEvent {
  return event.eventType === 'GAME_STATE_CHANGED';
}

export function isPlayerActionEvent(event: ServerEvent): event is PlayerActionEvent {
  return event.eventType === 'PLAYER_ACTION';
}

export function isCardsDealtEvent(event: ServerEvent): event is CardsDealtEvent {
  return event.eventType === 'CARDS_DEALT';
}

export function isWinnerDeterminedEvent(event: ServerEvent): event is WinnerDeterminedEvent {
  return event.eventType === 'WINNER_DETERMINED';
}

export function isPlayerRegisteredEvent(event: ServerEvent): event is PlayerRegisteredEvent {
  return event.eventType === 'PLAYER_REGISTERED';
}

export function isLobbyCreatedEvent(event: ServerEvent): event is LobbyCreatedEvent {
  return event.eventType === 'LOBBY_CREATED';
}

export function isLobbyJoinedEvent(event: ServerEvent): event is LobbyJoinedEvent {
  return event.eventType === 'LOBBY_JOINED';
}

export function isGameStartedEvent(event: ServerEvent): event is GameStartedEvent {
  return event.eventType === 'GAME_STARTED';
}

export function isErrorEvent(event: ServerEvent): event is ErrorEvent {
  return event.eventType === 'ERROR';
}

export function isSuccessEvent(event: ServerEvent): event is SuccessEvent {
  return event.eventType === 'SUCCESS';
}

export function isWelcomeEvent(event: ServerEvent): event is WelcomeEvent {
  return event.eventType === 'WELCOME';
}
