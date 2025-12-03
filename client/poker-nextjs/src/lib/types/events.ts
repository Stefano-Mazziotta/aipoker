// src/types/events.ts
import { PlayerDTO } from './player';

import {
  AUTH_EVENTS,
  LOBBY_EVENTS,
  GAME_EVENTS,
  SYSTEM_EVENTS,
} from '../constants/event-types';

/* ==========================================================================
   Base structures
   ========================================================================== */
/** Events that come from the Java DomainEvent hierarchy */
export interface DomainEvent<T> {
  eventId: string;
  eventType: string;
  code?: string;
  message: string;
  data: T;
  timestamp: string;
}

/* ==========================================================================
   Specific event payloads
   ========================================================================== */

/** AUTH & SYSTEM */
export interface PlayerRegisteredEvent extends DomainEvent<PlayerDTO> {
  eventType: typeof AUTH_EVENTS.PLAYER_REGISTERED;
}

export interface WelcomeEvent extends DomainEvent<null> {
  eventType: typeof SYSTEM_EVENTS.WELCOME;
  sessionId: string;
}

export interface SuccessEvent extends DomainEvent<null>{
  eventType: typeof SYSTEM_EVENTS.SUCCESS;
}

export interface ErrorEvent extends DomainEvent<null> {
  eventType: typeof SYSTEM_EVENTS.ERROR;
}

/** LOBBY */
interface LobbyCreatedDTO {
  lobbyId: string;
  adminId: string;
  players: PlayerDTO[];
  maxPlayers: number;
}

export interface LobbyCreatedEvent extends DomainEvent<LobbyCreatedDTO> {
  eventType: typeof LOBBY_EVENTS.LOBBY_CREATED;
}

interface PlayerJoinedLobbyDTO {
  lobbyId: string;
  lobbyName: string;
  currentPlayerCount: number;
  maxPlayers: number;
  isOpen: boolean;
  adminPlayerId: string;
  players: PlayerDTO[];
}
export interface PlayerJoinedLobbyEvent extends DomainEvent<PlayerJoinedLobbyDTO> {
  eventType: typeof LOBBY_EVENTS.PLAYER_JOINED_LOBBY;
}

interface PlayerLeftLobbyDTO {
  lobbyId: string;
  playerId: string;
  currentPlayerCount: number;
  maxPlayers: number;
  adminPlayerId: string;
  players: PlayerDTO[];
}

export interface PlayerLeftLobbyEvent extends DomainEvent<PlayerLeftLobbyDTO> {
  eventType: typeof LOBBY_EVENTS.PLAYER_LEFT_LOBBY;
}

/** GAME */
interface GameStartedDTO {
  gameId: string;
  lobbyId: string;
  players: PlayerDTO[];
  smallBlind: number;
  bigBlind: number;
}
export interface GameStartedEvent extends DomainEvent<GameStartedDTO> {
  eventType: typeof GAME_EVENTS.GAME_STARTED;
}

interface GameStateChangedDTO {
  gameId: string;
  newState: string;
  currentPlayerId: string;
  currentPlayerName: string;
  pot: number;
}

export interface GameStateChangedEvent extends DomainEvent<GameStateChangedDTO> {
  eventType: typeof GAME_EVENTS.GAME_STATE_CHANGED;
}
interface PlayerActionDTO {
  gameId: string;
  player: PlayerDTO
  action: 'FOLD' | 'CHECK' | 'CALL' | 'RAISE' | 'ALL_IN';
  amount: number;
  newPot: number;
  currentBet: number;
}
export interface PlayerActionEvent extends DomainEvent<PlayerActionDTO> {
  eventType: typeof GAME_EVENTS.PLAYER_ACTION;
  
}
interface CardsDealtDTO {
  gameId: string;
  phase: 'PREFLOP' | 'FLOP' | 'TURN' | 'RIVER';
  newCards: string[];
  allCommunityCards: string[];
}
export interface CardsDealtEvent extends DomainEvent<CardsDealtDTO> {
  eventType: typeof GAME_EVENTS.CARDS_DEALT;
}

interface WinnerDeterminedDTO {
  gameId: string;
  winnerId: string;
  winnerName: string;
  handRank: string;
  amountWon: number;
}
export interface WinnerDeterminedEvent extends DomainEvent<WinnerDeterminedDTO> {
  eventType: typeof GAME_EVENTS.WINNER_DETERMINED;
}

/* ==========================================================================
   Unified discriminated union
   ========================================================================== */

export type ServerEvent =
  | PlayerRegisteredEvent
  | WelcomeEvent
  | SuccessEvent
  | ErrorEvent
  | LobbyCreatedEvent
  | PlayerJoinedLobbyEvent
  | PlayerLeftLobbyEvent
  | GameStartedEvent
  | GameStateChangedEvent
  | PlayerActionEvent
  | CardsDealtEvent
  | WinnerDeterminedEvent;

/* ==========================================================================
   Convenient type guards (optional but very useful)
   ========================================================================== */

export const isEvent = <T extends ServerEvent['eventType']>(
  event: ServerEvent,
  type: T,
): event is Extract<ServerEvent, { eventType: T }> => event.eventType === type;

// Example usage:
// if (isEvent(event, GAME_EVENTS.PLAYER_ACTION)) { ... }

export const EventGuards = {
  isPlayerRegistered: (e: ServerEvent): e is PlayerRegisteredEvent =>
    e.eventType === AUTH_EVENTS.PLAYER_REGISTERED,
  isWelcome: (e: ServerEvent): e is WelcomeEvent =>
    e.eventType === SYSTEM_EVENTS.WELCOME,
  isError: (e: ServerEvent): e is ErrorEvent =>
    e.eventType === SYSTEM_EVENTS.ERROR,
  isLobbyCreated: (e: ServerEvent): e is LobbyCreatedEvent =>
    e.eventType === LOBBY_EVENTS.LOBBY_CREATED,
  isPlayerJoinedLobby: (e: ServerEvent): e is PlayerJoinedLobbyEvent =>
    e.eventType === LOBBY_EVENTS.PLAYER_JOINED_LOBBY,
  isPlayerLeftLobby: (e: ServerEvent): e is PlayerLeftLobbyEvent =>
    e.eventType === LOBBY_EVENTS.PLAYER_LEFT_LOBBY,
  isGameStartedEvent: (e: ServerEvent): e is GameStartedEvent =>
    e.eventType === GAME_EVENTS.GAME_STARTED,
  isGameStateChangedEvent: (e: ServerEvent): e is GameStateChangedEvent =>
    e.eventType === GAME_EVENTS.GAME_STATE_CHANGED,
  isPlayerActionEvent: (e: ServerEvent): e is PlayerActionEvent =>
    e.eventType === GAME_EVENTS.PLAYER_ACTION,
  isCardsDealtEvent: (e: ServerEvent): e is CardsDealtEvent =>
    e.eventType === GAME_EVENTS.CARDS_DEALT,
  isWinnerDeterminedEvent: (e: ServerEvent): e is WinnerDeterminedEvent =>
    e.eventType === GAME_EVENTS.WINNER_DETERMINED,
};