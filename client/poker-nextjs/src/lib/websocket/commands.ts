// WebSocket command builders matching Java backend protocol

export const WebSocketCommands = {
  // Player registration
  register: (name: string, chips: number): string => {
    return `REGISTER ${name} ${chips}`;
  },

  // Lobby operations
  createLobby: (lobbyName: string, maxPlayers: number, playerId: string): string => {
    return `CREATE_LOBBY ${lobbyName} ${maxPlayers} ${playerId}`;
  },

  joinLobby: (lobbyId: string, playerId: string): string => {
    return `JOIN_LOBBY ${lobbyId} ${playerId}`;
  },

  leaveLobby: (lobbyId: string, playerId: string): string => {
    return `LEAVE_LOBBY ${lobbyId} ${playerId}`;
  },

  // Game operations
  startGame: (playerIds: string[], smallBlind: number, bigBlind: number): string => {
    const playerIdsStr = playerIds.join(' ');
    return `START_GAME ${playerIdsStr} ${smallBlind} ${bigBlind}`;
  },

  // Player actions
  check: (gameId: string, playerId: string): string => {
    return `CHECK ${gameId} ${playerId}`;
  },

  call: (gameId: string, playerId: string): string => {
    return `CALL ${gameId} ${playerId}`;
  },

  raise: (gameId: string, playerId: string, amount: number): string => {
    return `RAISE ${gameId} ${playerId} ${amount}`;
  },

  fold: (gameId: string, playerId: string): string => {
    return `FOLD ${gameId} ${playerId}`;
  },

  allIn: (gameId: string, playerId: string): string => {
    return `ALL_IN ${gameId} ${playerId}`;
  },
};
