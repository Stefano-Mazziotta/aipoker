'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useWebSocket } from './WebSocketContext';
import { useAuth } from './AuthContext';
import { PlayerInfo } from '@/lib/types/player';
import {
  WebSocketEvent,
  LobbyCreatedData,
  LobbyJoinedData,
  PlayerJoinedLobbyData,
  PlayerLeftLobbyData,
} from '@/lib/types/events';

interface LobbyContextType {
  lobbyId: string | null;
  lobbyPlayers: PlayerInfo[];
  isInLobby: boolean;
  isLobbyAdmin: boolean;
  maxPlayers: number;
  createLobby: (lobbyName: string, maxPlayers: number) => void;
  joinLobby: (lobbyId: string) => void;
  leaveLobby: () => void;
  startGame: (smallBlind: number, bigBlind: number) => void;
}

const LobbyContext = createContext<LobbyContextType | null>(null);

export function LobbyProvider({ children }: { children: React.ReactNode }) {
  const [lobbyId, setLobbyId] = useState<string | null>(null);
  const [lobbyPlayers, setLobbyPlayers] = useState<PlayerInfo[]>([]);
  const [isLobbyAdmin, setIsLobbyAdmin] = useState(false);
  const [maxPlayers, setMaxPlayers] = useState(9);
  const { subscribe, sendCommand, commands } = useWebSocket();
  const { playerId } = useAuth();

  useEffect(() => {
    const unsubscribe = subscribe((event: WebSocketEvent) => {
      console.log('LobbyContext received event:', event.type, event.data);
      switch (event.type) {
        case 'LOBBY_CREATED': {
          const data = event.data as LobbyCreatedData;
          setLobbyId(data.lobbyId);
          // Map backend format to frontend format
          const players = data.players.map((p: any) => ({
            id: p.playerId || p.id,
            name: p.playerName || p.name,
            chips: p.chips || 0
          }));
          setLobbyPlayers(players);
          setIsLobbyAdmin(true);
          break;
        }
        case 'LOBBY_JOINED': {
          const data = event.data as LobbyJoinedData;
          setLobbyId(data.lobbyId);
          // Map backend format to frontend format
          const players = data.players.map((p: any) => ({
            id: p.playerId || p.id,
            name: p.playerName || p.name,
            chips: p.chips || 0
          }));
          setLobbyPlayers(players);
          setIsLobbyAdmin(false);
          break;
        }
        case 'PLAYER_JOINED_LOBBY': {
          const data = event.data as PlayerJoinedLobbyData;
          console.log('PLAYER_JOINED_LOBBY event received:', data);
          setLobbyPlayers(prev => {
            console.log('Current players:', prev);
            // Check if player already exists
            if (prev.some(p => p.id === data.playerId)) {
              console.log('Player already in list, skipping');
              return prev;
            }
            const newPlayer = {
              id: data.playerId,
              name: data.playerName,
              chips: 0, // Will be updated when game starts
            };
            console.log('Adding new player:', newPlayer);
            return [...prev, newPlayer];
          });
          break;
        }
        case 'PLAYER_LEFT_LOBBY': {
          const data = event.data as PlayerLeftLobbyData;
          setLobbyPlayers(prev => prev.filter(p => p.id !== data.playerId));
          
          // If we left, reset lobby state
          if (data.playerId === playerId) {
            setLobbyId(null);
            setLobbyPlayers([]);
            setIsLobbyAdmin(false);
          }
          break;
        }
        case 'GAME_STARTED': {
          // Game started, we're no longer just in lobby
          // Keep lobby data but transition to game
          break;
        }
      }
    });

    return unsubscribe;
  }, [subscribe, playerId]);

  const createLobby = useCallback((lobbyName: string, maxPlayersCount: number) => {
    if (!playerId) {
      console.error('Cannot create lobby: not registered');
      return;
    }
    const command = commands.createLobby(lobbyName, maxPlayersCount, playerId);
    sendCommand(command);
    setMaxPlayers(maxPlayersCount);
  }, [playerId, commands, sendCommand]);

  const joinLobby = useCallback((targetLobbyId: string) => {
    if (!playerId) {
      console.error('Cannot join lobby: not registered');
      return;
    }
    const command = commands.joinLobby(targetLobbyId, playerId);
    sendCommand(command);
  }, [playerId, commands, sendCommand]);

  const leaveLobby = useCallback(() => {
    if (!playerId || !lobbyId) {
      console.error('Cannot leave lobby: not in lobby');
      return;
    }
    const command = commands.leaveLobby(lobbyId, playerId);
    sendCommand(command);
  }, [playerId, lobbyId, commands, sendCommand]);

  const startGame = useCallback((smallBlind: number, bigBlind: number) => {
    if (!isLobbyAdmin) {
      console.error('Cannot start game: not lobby admin');
      return;
    }
    if (lobbyPlayers.length < 2) {
      console.error('Cannot start game: need at least 2 players');
      return;
    }
    const playerIds = lobbyPlayers.map(p => p.id);
    const command = commands.startGame(playerIds, smallBlind, bigBlind);
    sendCommand(command);
  }, [isLobbyAdmin, lobbyPlayers, commands, sendCommand]);

  const isInLobby = lobbyId !== null;

  return (
    <LobbyContext.Provider
      value={{
        lobbyId,
        lobbyPlayers,
        isInLobby,
        isLobbyAdmin,
        maxPlayers,
        createLobby,
        joinLobby,
        leaveLobby,
        startGame,
      }}
    >
      {children}
    </LobbyContext.Provider>
  );
}

export function useLobby() {
  const context = useContext(LobbyContext);
  if (!context) {
    throw new Error('useLobby must be used within LobbyProvider');
  }
  return context;
}
