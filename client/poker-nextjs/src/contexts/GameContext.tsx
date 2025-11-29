'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useWebSocket } from './WebSocketContext';
import { useAuth } from './AuthContext';
import { GameStateDTO } from '@/lib/types/game';
import {
  ServerEvent,
  isGameStartedEvent,
  isPlayerActionEvent,
  isGameStateChangedEvent,
} from '@/lib/types/server-events';
import { WebSocketCommand } from '@/lib/types/commands';

interface GameContextType {
  gameId: string | null;
  gameState: GameStateDTO | null;
  isInGame: boolean;
  isPlayerTurn: boolean;
  performAction: (action: string, amount?: number) => void;
}

const GameContext = createContext<GameContextType | null>(null);

export function GameProvider({ children }: { children: React.ReactNode }) {
  const [gameId, setGameId] = useState<string | null>(null);
  const [gameState, setGameState] = useState<GameStateDTO | null>(null);
  const { subscribe, sendCommand, commands } = useWebSocket();
  const { playerId } = useAuth();

  useEffect(() => {
    const unsubscribe = subscribe((event: ServerEvent) => {
      if (isGameStartedEvent(event)) {
        console.log('Game started event received:', event);
        setGameId(event.gameId);
        // Request initial game state
        if (event.gameId) {
          console.log('Requesting game state for:', event.gameId);
          sendCommand(commands.getGameState(event.gameId));
        }
      } else if (event.eventType === 'GAME_STATE') {
        // GAME_STATE is not a domain event, handle specially
        const data = event as any;
        console.log('Game state received:', data);
        setGameState(data);
      } else if (isPlayerActionEvent(event)) {
        console.log('Player action:', event);
        // Optionally update local game state based on action
      } else if (event.eventType === 'GAME_ENDED' || event.eventType === 'WINNER_DETERMINED') {
        // Handle game end
        setTimeout(() => {
          setGameId(null);
          setGameState(null);
        }, 5000); // Show results for 5 seconds
      }
    });

    return unsubscribe;
  }, [subscribe, sendCommand, commands]);

  const performAction = useCallback((action: string, amount?: number) => {
    if (!gameId || !playerId) {
      console.error('Cannot perform action: missing gameId or playerId');
      return;
    }

    let command: WebSocketCommand<unknown>;
    switch (action) {
      case 'CHECK':
        command = commands.check(gameId, playerId);
        break;
      case 'CALL':
        command = commands.call(gameId, playerId);
        break;
      case 'RAISE':
        if (amount === undefined) {
          console.error('RAISE action requires an amount');
          return;
        }
        command = commands.raise(gameId, playerId, amount);
        break;
      case 'FOLD':
        command = commands.fold(gameId, playerId);
        break;
      case 'ALL_IN':
        command = commands.allIn(gameId, playerId);
        break;
      default:
        console.error('Unknown action:', action);
        return;
    }

    sendCommand(command);
  }, [gameId, playerId, commands, sendCommand]);

  const isInGame = gameId !== null;
  const isPlayerTurn = gameState?.currentPlayerId === playerId;

  return (
    <GameContext.Provider
      value={{
        gameId,
        gameState,
        isInGame,
        isPlayerTurn,
        performAction,
      }}
    >
      {children}
    </GameContext.Provider>
  );
}

export function useGame() {
  const context = useContext(GameContext);
  if (!context) {
    throw new Error('useGame must be used within GameProvider');
  }
  return context;
}
