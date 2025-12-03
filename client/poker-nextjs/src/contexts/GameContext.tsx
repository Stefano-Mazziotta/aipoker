'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useWebSocket } from './WebSocketContext';
import { useAuth } from './AuthContext';
import { useToast } from './ToastContext';
import { GameStateDTO, parseCard } from '@/lib/types/game';
import {
  ServerEvent,
  EventGuards
} from '@/lib/types/events';
import { WebSocketCommand } from '@/lib/types/commands';
import { ALL_GAME_EVENT_TYPES } from '@/lib/constants/event-types';
import { PLAYER_ACTIONS } from '@/lib/constants/player-actions';

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

        // If game state is already initialized for this gameId, don't reinitialize
        // This prevents overwriting player cards that were already dealt
        setGameState(prevState => {
          if (prevState && prevState.gameId === gameId) {
            console.log('Game state already initialized for this gameId, skipping GAME_STARTED');
            return prevState;
          }

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
              cards: [],
              bet: player.currentBet, // Assuming `bet` corresponds to `currentBet`
              isActive: !player.isFolded, // Assuming `isActive` is true if not folded
              folded: player.isFolded // Assuming `folded` corresponds to `isFolded`
            }))
          };
          console.log('Initializing game state from GAME_STARTED:', initialState);
          return initialState;
        });
      }

      // GAME_STATE_CHANGED – full or partial state update from server
      if (EventGuards.isGameStateChangedEvent(event)) {
        console.log('Game state changed:', event);
        
        // Merge partial state update with existing game state
        setGameState(prevState => {
          if (!prevState) return prevState;
          
          const updatedState = {
            ...prevState,
            currentPlayerId: event.data.currentPlayerId,
            pot: event.data.pot,
            round: event.data.newState.toLowerCase().replace('_', '-') as any,
          };

          // Update community cards if provided
          if (event.data.communityCards) {
            updatedState.communityCards = event.data.communityCards.map(card => parseCard(card));
          }

          // Update current bet if provided
          if (event.data.currentBet !== undefined) {
            updatedState.currentBet = event.data.currentBet;
          }

          return updatedState;
        });
      }

      // PLAYER_ACTION – someone folded, raised, etc.
      if (EventGuards.isPlayerActionEvent(event)) {
        console.log('Player action received:', event);
        
        // Skip if this is our own action - we already got the sync response
        if (event.data.playerId === playerId) {
          console.log('Ignoring PLAYER_ACTION for own action (already handled by sync response)');
          return;
        }
        
        // Update game state for other players' actions
        setGameState(prevState => {
          if (!prevState) return prevState;
          
          return {
            ...prevState,
            pot: event.data.newPot,
            currentBet: event.data.currentBet,
          } as any;
        });
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
        setGameState(prevState => {
          if (!prevState) return prevState;
          
          return {
            ...prevState,
            communityCards: event.data.allCommunityCards,
            phase: event.data.phase,
          } as any;
        });
      }

      // PLAYER_CARDS_DEALT – player's private hole cards
      if (EventGuards.isPlayerCardsDealtEvent(event)) {
        console.log('==== PLAYER_CARDS_DEALT EVENT ====');
        console.log('Event data:', event.data);
        console.log('Current playerId:', playerId);
        console.log('Event playerId:', event.data.playerId);
        console.log('Cards:', event.data.cards);
        
        // Update current player's cards in the game state
        setGameState(prevState => {
          if (!prevState) {
            console.log('ERROR: No prevState, skipping card update');
            return prevState;
          }
          
          console.log('Previous state players:', prevState.players.map(p => ({ id: p.id, name: p.name, cards: p.cards })));
          
          const updatedPlayers = prevState.players.map(player => {
            if (player.id === event.data.playerId) {
              console.log(`✓ Updating cards for player ${player.name} (${player.id}) with cards:`, event.data.cards);
              return { ...player, cards: event.data.cards };
            }
            return player;
          });
          
          const newState = {
            ...prevState,
            players: updatedPlayers,
          };
          
          console.log('New state players:', newState.players.map(p => ({ id: p.id, name: p.name, cards: p.cards })));
          console.log('==== END PLAYER_CARDS_DEALT ====');
          
          return newState;
        });
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
      case PLAYER_ACTIONS.CHECK:
        command = commands.check(gameId, playerId);
        break;
      case PLAYER_ACTIONS.CALL:
        if (amount === undefined) {
          console.error('CALL action requires an amount');
          return;
        }
        command = commands.call(gameId, playerId, amount);
        break;
      case PLAYER_ACTIONS.RAISE:
        if (amount === undefined) {
          console.error('RAISE action requires an amount');
          return;
        }
        command = commands.raise(gameId, playerId, amount);
        break;
      case PLAYER_ACTIONS.FOLD:
        command = commands.fold(gameId, playerId);
        break;
      case PLAYER_ACTIONS.ALL_IN:
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
