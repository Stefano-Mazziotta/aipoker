'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useWebSocket } from './WebSocketContext';
import {
  WebSocketEvent,
  PlayerRegisteredData,
} from '@/lib/types/events';

interface AuthContextType {
  playerId: string | null;
  playerName: string | null;
  playerChips: number;
  isRegistered: boolean;
  register: (name: string, chips: number) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [playerId, setPlayerId] = useState<string | null>(null);
  const [playerName, setPlayerName] = useState<string | null>(null);
  const [playerChips, setPlayerChips] = useState<number>(0);
  const { subscribe, sendCommand, commands, isConnected } = useWebSocket();

  // Restore from localStorage on mount
  useEffect(() => {
    const storedPlayerId = localStorage.getItem('playerId');
    const storedPlayerName = localStorage.getItem('playerName');
    const storedPlayerChips = localStorage.getItem('playerChips');
    
    if (storedPlayerId && storedPlayerName && storedPlayerChips) {
      setPlayerId(storedPlayerId);
      setPlayerName(storedPlayerName);
      setPlayerChips(parseInt(storedPlayerChips, 10));
    }
  }, []);

  useEffect(() => {
    const unsubscribe = subscribe((event: WebSocketEvent) => {
      switch (event.type) {
        case 'PLAYER_REGISTERED': {
          const data = event.data as PlayerRegisteredData;
          setPlayerId(data.id);
          setPlayerName(data.name);
          setPlayerChips(data.chips);
          localStorage.setItem('playerId', data.id);
          localStorage.setItem('playerName', data.name);
          localStorage.setItem('playerChips', data.chips.toString());
          break;
        }
        case 'ERROR': {
          console.error('Auth error:', event.data);
          break;
        }
      }
    });

    return unsubscribe;
  }, [subscribe]);

  const register = useCallback((name: string, chips: number) => {
    if (!isConnected) {
      console.error('Cannot register: WebSocket not connected');
      return;
    }
    // Command only needs playerName, chips is defaulted on server to 1000
    const command = commands.register(name);
    console.log('Sending register command:', command);
    sendCommand(command);
  }, [isConnected, commands, sendCommand]);

  const logout = useCallback(() => {
    setPlayerId(null);
    setPlayerName(null);
    setPlayerChips(0);
    localStorage.removeItem('playerId');
    localStorage.removeItem('playerName');
    localStorage.removeItem('playerChips');
  }, []);

  const isRegistered = playerId !== null;

  return (
    <AuthContext.Provider
      value={{
        playerId,
        playerName,
        playerChips,
        isRegistered,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
