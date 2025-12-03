'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useWebSocket } from './WebSocketContext';
import { useAuth } from './AuthContext';
import { PlayerDTO } from '@/lib/types/player';
import {
  ServerEvent,
  EventGuards
} from '@/lib/types/events';
import { ALL_LOBBY_EVENT_TYPES } from '@/lib/constants/event-types';

interface LobbyContextType {
  lobbyId: string | null;
  lobbyPlayers: PlayerDTO[];
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
  const [lobbyPlayers, setLobbyPlayers] = useState<PlayerDTO[]>([]);
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
      
      if (EventGuards.isLobbyCreated(event)) {
        const { lobbyId, players, maxPlayers } = event.data;

        setLobbyId(lobbyId);
        setLobbyPlayers(players);
        setIsLobbyAdmin(true);
        setMaxPlayers(maxPlayers);

        console.log('Lobby created - you are the admin', { lobbyId, players: players.length });
      }

      // PLAYER_JOINED_LOBBY – broadcast when any player (including yourself) joins
      if (EventGuards.isPlayerJoinedLobby(event)) {
        const { lobbyId: eventLobbyId, players, maxPlayers: eventMaxPlayers, adminPlayerId } = event.data;
        
        // If we're not in a lobby yet, this must be us joining
        if (!lobbyId) {
          setLobbyId(eventLobbyId);
          setIsLobbyAdmin(playerId === adminPlayerId);
        }
        
        setLobbyPlayers(players);
        setMaxPlayers(eventMaxPlayers);
        
        console.log('Player joined lobby:', { 
          lobbyId: eventLobbyId, 
          players: players.length,
          isAdmin: playerId === adminPlayerId 
        });
      }

      // PLAYER_LEFT_LOBBY – someone left the lobby
      if (EventGuards.isPlayerLeftLobby(event)) {
        const { playerId, playerName } = event.data;

        console.log('Player left lobby:', { playerId, playerName });

        setLobbyPlayers(prev => prev.filter(p => p.playerId !== playerId));

        // If YOU are the one who left
        if (playerId === playerId) {
          console.log('You have left the lobby - resetting state');
          setLobbyId(null);
          setLobbyPlayers([]);
          setIsLobbyAdmin(false);
          setMaxPlayers(0);
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
    const command = commands.createLobby(playerId, lobbyName, maxPlayersCount);
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
    const playerIds = lobbyPlayers.map(p => p.playerId);
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
