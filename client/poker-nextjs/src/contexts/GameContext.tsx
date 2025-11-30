'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useWebSocket } from './WebSocketContext';
import { useAuth } from './AuthContext';
import { GameStateDTO } from '@/lib/types/game';
import {
  ServerEvent,
  EventGuards
} from '@/lib/types/events';
import { WebSocketCommand } from '@/lib/types/commands';
import { ALL_GAME_EVENT_TYPES } from '@/lib/constants/event-types';

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
      // Early return if it's not a game-related event
      if (!ALL_GAME_EVENT_TYPES.includes(event.eventType as any)) {
        return;
      }

      // GAME_STARTED – game has officially begun
      if (EventGuards.isGameStartedEvent(event)) {
        const {gameId} = event.data;
        console.log('Game started event received:', event);

        setGameId(gameId);

        if (gameId) {
          console.log('Requesting initial game state for gameId:', gameId);
          sendCommand(commands.getGameState(gameId));
        }
      }

      // GAME_STATE_CHANGED – full or partial state update from server
      if (EventGuards.isGameStateChangedEvent(event)) {
        console.log('Game state changed:', event);
        // const dto:GameStateDTO ={
        //   gameId: event.data.gameId,
        //   currentPlayerId: event.data.currentPlayerId,
        //   currentPlayerName: event.data.currentPlayerName,
        //   pot: event.data.pot,
        // } 
        setGameState(event.data as any); // todo: fix it
      }

      // PLAYER_ACTION – someone folded, raised, etc.
      if (EventGuards.isPlayerActionEvent(event)) {
        console.log('Player action received:', event);
        // You can optimistically update UI here if desired
      }

      // CARDS_DEALT – new community or hole cards
      if (EventGuards.isCardsDealtEvent(event)) {
        console.log('Cards dealt:', event);
        // Optionally merge into current game state
      }

      // WINNER_DETERMINED – hand is over, show results
      if (EventGuards.isWinnerDeterminedEvent(event)) {
        console.log('Winner determined:', event);

        setTimeout(() => {
          console.log('Clearing game state after winner screen');
          setGameId(null);
          setGameState(null);
        }, 5000);
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
