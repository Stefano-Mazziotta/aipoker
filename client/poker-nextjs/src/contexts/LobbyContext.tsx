'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useWebSocket } from './WebSocketContext';
import { useAuth } from './AuthContext';
import { PlayerInfo } from '@/lib/types/player';
import {
  ServerEvent,
  isLobbyCreatedEvent,
  isLobbyJoinedEvent,
  isPlayerJoinedLobbyEvent,
  isPlayerLeftLobbyEvent,
} from '@/lib/types/server-events';
import { ALL_LOBBY_EVENT_TYPES } from '@/lib/constants/event-types';

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
    const unsubscribe = subscribe((event: ServerEvent) => {
      // Only handle lobby-related events
      if (!ALL_LOBBY_EVENT_TYPES.includes(event.eventType as any)) {
        return;
      }
      
      console.log('LobbyContext received event:', event.eventType, event);
      
      if (isLobbyCreatedEvent(event)) {
        setLobbyId(event.lobbyId);
        // Map PlayerDTO to PlayerInfo
        const mappedPlayers = event.players.map(p => ({
          id: p.playerId,
          name: p.playerName,
          chips: p.chips
        }));
        setLobbyPlayers(mappedPlayers);
        setIsLobbyAdmin(true);
        setMaxPlayers(event.maxPlayers);
        console.log('Lobby created, players:', mappedPlayers);
      } else if (isLobbyJoinedEvent(event)) {
        setLobbyId(event.lobbyId);
        // Map PlayerDTO to PlayerInfo
        const mappedPlayers = event.players.map(p => ({
          id: p.playerId,
          name: p.playerName,
          chips: p.chips
        }));
        setLobbyPlayers(mappedPlayers);
        setIsLobbyAdmin(false);
        console.log('Lobby joined, players:', mappedPlayers);
      } else if (isPlayerJoinedLobbyEvent(event)) {
        console.log('PLAYER_JOINED_LOBBY event received:', event);
        setLobbyPlayers(prev => {
          console.log('Current players:', prev);
          // Check if player already exists
          if (prev.some(p => p.id === event.playerId)) {
            console.log('Player already in list, skipping');
            return prev;
          }
          const newPlayer = {
            id: event.playerId,
            name: event.playerName,
            chips: event.playerChips,
          };
          console.log('Adding new player:', newPlayer);
          return [...prev, newPlayer];
        });
        setMaxPlayers(event.maxPlayers);
      } else if (isPlayerLeftLobbyEvent(event)) {
        setLobbyPlayers(prev => prev.filter(p => p.id !== event.playerId));
        
        // If we left, reset lobby state
        if (event.playerId === playerId) {
          setLobbyId(null);
          setLobbyPlayers([]);
          setIsLobbyAdmin(false);
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
    const command = commands.createLobby(playerId, maxPlayersCount);
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
    if (!lobbyId) {
      console.error('Cannot start game: no lobby ID');
      return;
    }
    const playerIds = lobbyPlayers.map(p => p.id);
    const command = commands.startGame(lobbyId, playerIds, smallBlind, bigBlind);
    sendCommand(command);
  }, [isLobbyAdmin, lobbyPlayers, lobbyId, commands, sendCommand]);

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
