'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useWebSocket } from './WebSocketContext';
import { useAuth } from './AuthContext';
import { useToast } from './ToastContext';
import { GameStateDTO } from '@/lib/types/game';
import {
  ServerEvent,
  EventGuards
} from '@/lib/types/events';
import { WebSocketCommand } from '@/lib/types/commands';
import { ALL_GAME_EVENT_TYPES } from '@/lib/constants/event-types';

interface WinnerData {
  winnerName: string;
  handRank: string;
  amountWon: number;
}

interface GameContextType {
  gameId: string | null;
  gameState: GameStateDTO | null;
  isInGame: boolean;
  isPlayerTurn: boolean;
  performAction: (action: string, amount?: number) => void;
  winner: WinnerData | null;
  clearWinner: () => void;
}

const GameContext = createContext<GameContextType | null>(null);

export function GameProvider({ children }: { children: React.ReactNode }) {
  const [gameId, setGameId] = useState<string | null>(null);
  const [gameState, setGameState] = useState<GameStateDTO | null>(null);
  const [winner, setWinner] = useState<WinnerData | null>(null);
  const { subscribe, sendCommand, commands } = useWebSocket();
  const { playerId } = useAuth();
  const { showToast } = useToast();

  const clearWinner = useCallback(() => {
    setWinner(null);
    setGameId(null);
    setGameState(null);
  }, []);

  useEffect(() => {
    const unsubscribe = subscribe((event: ServerEvent) => {
      // Early return if it's not a game-related event
      if (!ALL_GAME_EVENT_TYPES.includes(event.eventType as any)) {
        return;
      }

      // GAME_STARTED – game has officially begun
      if (EventGuards.isGameStartedEvent(event)) {
        const { gameId, players, pot, currentBet, currentPlayerId, currentPlayerName, gameState } = event.data;
        console.log('Game started event received:', event);

        setGameId(gameId);
        
        // Initialize game state from GAME_STARTED event with complete data
        const initialState: GameStateDTO = {
          gameId: gameId,
          currentPlayerId: currentPlayerId || '',
          pot: pot,
          currentBet: currentBet,
          round: (gameState || 'PRE_FLOP').toLowerCase().replace('_', '-') as any,
          communityCards: [],
          players: players.map(player => ({
            id: player.playerId,
            name: player.playerName,
            chips: player.chips,
            currentBet: player.currentBet,
            hasActed: false,
            isFolded: player.isFolded,
            isAllIn: player.isAllIn,
            cards: []
          }))
        };
        console.log('Initializing game state from GAME_STARTED:', initialState);
        setGameState(initialState);
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
        // Update game state optimistically
        if (gameState) {
          setGameState({
            ...gameState,
            pot: event.data.newPot,
          } as any);
        }
      }

      // ROUND_COMPLETED – betting round finished, next phase starting
      if (EventGuards.isRoundCompletedEvent(event)) {
        console.log('Round completed:', event.data.completedPhase, '→', event.data.nextPhase);
        
        // Show toast notification for round transition
        showToast(
          `${event.data.completedPhase.replace('_', ' ')} complete! Moving to ${event.data.nextPhase.replace('_', ' ')}...`,
          'success',
          2000
        );
      }

      // CARDS_DEALT – new community or hole cards
      if (EventGuards.isCardsDealtEvent(event)) {
        console.log('Cards dealt:', event.data.phase, event.data.newCards);
        // Update game state with new community cards
        if (gameState) {
          setGameState({
            ...gameState,
            communityCards: event.data.allCommunityCards,
            phase: event.data.phase,
          } as any);
        }
      }

      // WINNER_DETERMINED – hand is over, show results
      if (EventGuards.isWinnerDeterminedEvent(event)) {
        console.log('Winner determined:', event);

        // Set winner data for modal display
        setWinner({
          winnerName: event.data.winnerName,
          handRank: event.data.handRank || 'Best Hand',
          amountWon: event.data.amountWon,
        });

        // Auto-clear after 5 seconds (modal handles its own display)
        setTimeout(() => {
          console.log('Clearing game state after winner screen');
          clearWinner();
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
        winner,
        clearWinner,
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
