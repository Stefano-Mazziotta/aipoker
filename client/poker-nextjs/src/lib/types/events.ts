import { PlayerDTO, PlayerInfo } from './player';
import { GameStateDTO, GameAction } from './game';

// WebSocket event types
export type EventType =
  | 'PLAYER_REGISTERED'
  | 'LOBBY_CREATED'
  | 'LOBBY_JOINED'
  | 'PLAYER_JOINED_LOBBY'
  | 'PLAYER_LEFT_LOBBY'
  | 'GAME_STARTED'
  | 'GAME_STATE'
  | 'PLAYER_ACTION'
  | 'GAME_ENDED'
  | 'ERROR';

// Base WebSocket event structure
export interface WebSocketEvent<T = any> {
  type: EventType;
  data: T;
}

// Specific event data types
export interface PlayerRegisteredData {
  id: string;
  name: string;
  chips: number;
}

export interface LobbyCreatedData {
  lobbyId: string;
  players: PlayerInfo[];
}

export interface LobbyJoinedData {
  lobbyId: string;
  players: PlayerInfo[];
}

export interface PlayerJoinedLobbyData {
  playerId: string;
  playerName: string;
  currentPlayerCount: number;
}

export interface PlayerLeftLobbyData {
  playerId: string;
  playerName: string;
  currentPlayerCount: number;
}

export interface GameStartedData {
  gameId: string;
  players: PlayerInfo[];
}

export interface PlayerActionData {
  playerId: string;
  action: GameAction;
  amount?: number;
}

export interface ErrorData {
  message: string;
  code?: string;
}
