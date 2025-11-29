/**
 * TypeScript types mirroring Java server request DTOs.
 * These ensure type safety for commands sent from client to server.
 */

// ============================================================================
// Base Command Structure
// ============================================================================

export interface WebSocketCommand<T = unknown> {
  command: string;
  data: T;
}

// ============================================================================
// Player Commands
// ============================================================================

export interface RegisterPlayerData {
  playerName: string;
  chips: number;
}

export interface GetLeaderboardData {
  limit?: number;
}

// ============================================================================
// Lobby Commands
// ============================================================================

export interface CreateLobbyData {
  playerId: string;
  maxPlayers: number;
}

export interface JoinLobbyData {
  lobbyId: string;
  playerId: string;
}

export interface LeaveLobbyData {
  lobbyId: string;
  playerId: string;
}

export interface StartGameData {
  lobbyId: string;
  playerIds: string[];
  smallBlind: number;
  bigBlind: number;
}

// ============================================================================
// Game Commands
// ============================================================================

export interface GetGameStateData {
  gameId: string;
}

export interface GetPlayerCardsData {
  gameId: string;
  playerId: string;
}

export interface PlayerActionData {
  gameId: string;
  playerId: string;
  action: 'FOLD' | 'CHECK' | 'CALL' | 'RAISE' | 'ALL_IN';
  amount?: number;
}

// ============================================================================
// Command Builders (Type-Safe Factories)
// ============================================================================

export const CommandType = {
  // Player
  REGISTER_PLAYER: 'REGISTER_PLAYER',
  GET_LEADERBOARD: 'GET_LEADERBOARD',
  
  // Lobby
  CREATE_LOBBY: 'CREATE_LOBBY',
  JOIN_LOBBY: 'JOIN_LOBBY',
  LEAVE_LOBBY: 'LEAVE_LOBBY',
  START_GAME: 'START_GAME',
  
  // Game
  GET_GAME_STATE: 'GET_GAME_STATE',
  GET_PLAYER_CARDS: 'GET_PLAYER_CARDS',
  FOLD: 'FOLD',
  CHECK: 'CHECK',
  CALL: 'CALL',
  RAISE: 'RAISE',
  ALL_IN: 'ALL_IN',
} as const;

export type CommandTypeValue = typeof CommandType[keyof typeof CommandType];

// ============================================================================
// Typed Command Constructors
// ============================================================================

export function createRegisterPlayerCommand(playerName: string, chips: number): WebSocketCommand<RegisterPlayerData> {
  return {
    command: CommandType.REGISTER_PLAYER,
    data: { playerName, chips }
  };
}

export function createGetLeaderboardCommand(limit?: number): WebSocketCommand<GetLeaderboardData> {
  return {
    command: CommandType.GET_LEADERBOARD,
    data: { limit }
  };
}

export function createCreateLobbyCommand(playerId: string, maxPlayers: number): WebSocketCommand<CreateLobbyData> {
  return {
    command: CommandType.CREATE_LOBBY,
    data: { playerId, maxPlayers }
  };
}

export function createJoinLobbyCommand(lobbyId: string, playerId: string): WebSocketCommand<JoinLobbyData> {
  return {
    command: CommandType.JOIN_LOBBY,
    data: { lobbyId, playerId }
  };
}

export function createLeaveLobbyCommand(lobbyId: string, playerId: string): WebSocketCommand<LeaveLobbyData> {
  return {
    command: CommandType.LEAVE_LOBBY,
    data: { lobbyId, playerId }
  };
}

export function createStartGameCommand(
  lobbyId: string,
  playerIds: string[],
  smallBlind: number,
  bigBlind: number
): WebSocketCommand<StartGameData> {
  return {
    command: CommandType.START_GAME,
    data: { lobbyId, playerIds, smallBlind, bigBlind }
  };
}

export function createGetGameStateCommand(gameId: string): WebSocketCommand<GetGameStateData> {
  return {
    command: CommandType.GET_GAME_STATE,
    data: { gameId }
  };
}

export function createGetPlayerCardsCommand(gameId: string, playerId: string): WebSocketCommand<GetPlayerCardsData> {
  return {
    command: CommandType.GET_PLAYER_CARDS,
    data: { gameId, playerId }
  };
}

export function createPlayerActionCommand(
  gameId: string,
  playerId: string,
  action: 'FOLD' | 'CHECK' | 'CALL' | 'RAISE' | 'ALL_IN',
  amount?: number
): WebSocketCommand<PlayerActionData> {
  return {
    command: action,
    data: { gameId, playerId, action, amount }
  };
}

// Convenience functions for specific actions
export function createFoldCommand(gameId: string, playerId: string): WebSocketCommand<PlayerActionData> {
  return createPlayerActionCommand(gameId, playerId, 'FOLD');
}

export function createCheckCommand(gameId: string, playerId: string): WebSocketCommand<PlayerActionData> {
  return createPlayerActionCommand(gameId, playerId, 'CHECK');
}

export function createCallCommand(gameId: string, playerId: string): WebSocketCommand<PlayerActionData> {
  return createPlayerActionCommand(gameId, playerId, 'CALL');
}

export function createRaiseCommand(gameId: string, playerId: string, amount: number): WebSocketCommand<PlayerActionData> {
  return createPlayerActionCommand(gameId, playerId, 'RAISE', amount);
}

export function createAllInCommand(gameId: string, playerId: string): WebSocketCommand<PlayerActionData> {
  return createPlayerActionCommand(gameId, playerId, 'ALL_IN');
}
