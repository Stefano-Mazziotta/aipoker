/**
 * WebSocket command builders for client-to-server communication.
 * Now using strongly-typed commands from the commands module.
 */

import {
  createRegisterPlayerCommand,
  createGetLeaderboardCommand,
  createCreateLobbyCommand,
  createJoinLobbyCommand,
  createLeaveLobbyCommand,
  createStartGameCommand,
  createGetGameStateCommand,
  createGetPlayerCardsCommand,
  createFoldCommand,
  createCheckCommand,
  createCallCommand,
  createRaiseCommand,
  createAllInCommand,
} from '../types/commands';

export const commands = {
  // Player commands
  register: (playerName: string, chips: number) => createRegisterPlayerCommand(playerName, chips),
  
  getLeaderboard: (limit?: number) => createGetLeaderboardCommand(limit),
  
  // Lobby commands
  createLobby: (playerId: string, lobbyName:string, maxPlayers: number) => 
    createCreateLobbyCommand(playerId, lobbyName, maxPlayers),
  
  joinLobby: (lobbyId: string, playerId: string) => 
    createJoinLobbyCommand(lobbyId, playerId),
  
  leaveLobby: (lobbyId: string, playerId: string,) => 
    createLeaveLobbyCommand(lobbyId, playerId),
  
  startGame: (lobbyId: string, playerIds: string[], smallBlind: number, bigBlind: number) =>
    createStartGameCommand(lobbyId, playerIds, smallBlind, bigBlind),
  
  // Game commands
  getGameState: (gameId: string) => createGetGameStateCommand(gameId),
  
  getPlayerCards: (gameId: string, playerId: string) => 
    createGetPlayerCardsCommand(gameId, playerId),
  
  // Player actions
  fold: (gameId: string, playerId: string) => createFoldCommand(gameId, playerId),
  
  check: (gameId: string, playerId: string) => createCheckCommand(gameId, playerId),
  
  call: (gameId: string, playerId: string) => createCallCommand(gameId, playerId),
  
  raise: (gameId: string, playerId: string, amount: number) => 
    createRaiseCommand(gameId, playerId, amount),
  
  allIn: (gameId: string, playerId: string) => createAllInCommand(gameId, playerId),
};

export type Commands = typeof commands;

// Backward compatibility export
export const WebSocketCommands = commands;
