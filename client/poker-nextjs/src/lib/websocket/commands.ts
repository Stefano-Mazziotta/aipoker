// WebSocket command builders matching Java backend protocol
// Uses JSON format with typed data for type safety

export const WebSocketCommands = {
  // Player registration
  register: (name: string, chips: number) => ({
    command: 'REGISTER',
    data: { name, chips }
  }),

  // Lobby operations
  createLobby: (lobbyName: string, maxPlayers: number, playerId: string) => ({
    command: 'CREATE_LOBBY',
    data: {
      name: lobbyName,
      maxPlayers,
      adminPlayerId: playerId
    }
  }),

  joinLobby: (lobbyId: string, playerId: string) => ({
    command: 'JOIN_LOBBY',
    data: { lobbyId, playerId }
  }),

  leaveLobby: (lobbyId: string, playerId: string) => ({
    command: 'LEAVE_LOBBY',
    data: { lobbyId, playerId }
  }),

  subscribeLobby: (lobbyId: string, playerId: string): string => {
    // Keep as string for backward compatibility with subscription mechanism
    return `SUBSCRIBE_LOBBY ${lobbyId} ${playerId}`;
  },

  subscribeGame: (gameId: string, playerId: string): string => {
    // Keep as string for backward compatibility with subscription mechanism
    return `SUBSCRIBE_GAME ${gameId} ${playerId}`;
  },

  // Game operations
  startGame: (lobbyId: string, playerIds: string[], smallBlind: number, bigBlind: number) => ({
    command: 'START_GAME',
    data: {
      lobbyId,
      playerIds,
      smallBlind,
      bigBlind
    }
  }),

  getGameState: (gameId: string) => ({
    command: 'GET_GAME_STATE',
    data: { gameId }
  }),

  // Player actions
  check: (gameId: string, playerId: string) => ({
    command: 'CHECK',
    data: { gameId, playerId }
  }),

  call: (gameId: string, playerId: string) => ({
    command: 'CALL',
    data: { gameId, playerId, amount: 0 }
  }),

  raise: (gameId: string, playerId: string, amount: number) => ({
    command: 'RAISE',
    data: { gameId, playerId, amount }
  }),

  fold: (gameId: string, playerId: string) => ({
    command: 'FOLD',
    data: { gameId, playerId }
  }),

  allIn: (gameId: string, playerId: string) => ({
    command: 'ALL_IN',
    data: { gameId, playerId }
  }),
};
