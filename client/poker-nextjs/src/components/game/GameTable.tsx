'use client';

import { useGame } from '@/contexts/GameContext';
import PlayerSeat from './PlayerSeat';
import CommunityCards from './CommunityCards';
import { PhaseIndicator } from './PhaseIndicator';
import { WinnerModal } from './WinnerModal';

export default function GameTable() {
  const { gameState, isInGame, winner, clearWinner } = useGame();

  if (!gameState) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-yellow-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-white text-xl">
            {isInGame ? 'Loading game state...' : 'Waiting for game to start...'}
          </p>
        </div>
      </div>
    );
  }

  // Fill empty seats
  const seats = Array.from({ length: 9 }, (_, index) => {
    return gameState.players[index] || null;
  });

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-2 sm:p-4 overflow-x-auto overflow-y-auto">
      {/* Winner Modal */}
      {winner && (
        <WinnerModal
          winnerName={winner.winnerName}
          handRank={winner.handRank}
          amountWon={winner.amountWon}
          onClose={clearWinner}
        />
      )}

      {/* Minimalist game header */}
      <div className="mb-2 sm:mb-3 text-center w-full max-w-[95vw] sm:max-w-[600px] mx-auto">
        {/* Phase Indicator */}
        <PhaseIndicator currentPhase={gameState.round} />
        
        {/* Compact game stats */}
        <div className="flex gap-2 justify-center items-center text-white mt-2">
          <div className="flex items-center gap-1 bg-black/70 px-2 py-1 rounded-md border border-green-500/30">
            <span className="text-green-400 font-bold text-sm sm:text-base">${gameState.pot}</span>
            <span className="text-gray-400 text-[10px] sm:text-xs">POT</span>
          </div>
          
          <div className="flex items-center gap-1 bg-black/70 px-2 py-1 rounded-md border border-yellow-500/30">
            <span className="text-yellow-400 font-bold text-sm sm:text-base">${gameState.currentBet}</span>
            <span className="text-gray-400 text-[10px] sm:text-xs">BET</span>
          </div>
          
          <div className="flex items-center gap-1 bg-black/70 px-2 py-1 rounded-md border border-blue-500/30">
            <span className="text-blue-400 font-bold text-xs sm:text-sm uppercase">{gameState.round}</span>
          </div>
        </div>
      </div>

      {/* Poker table */}
      <div className="relative w-full min-w-[600px] max-w-[95vw] sm:max-w-[800px] md:max-w-[1100px] aspect-4/3 sm:aspect-3/2 mb-4 sm:mb-6 px-4 sm:px-8 py-8 sm:py-12">
        <div className="absolute inset-0 bg-linear-to-br from-green-900 to-green-950 border-8 border-amber-900 rounded-[50%] shadow-2xl">
          {/* Table felt texture */}
          <div className="absolute inset-4 border-4 border-amber-900/30 rounded-[50%]" />
          
          {/* Community cards in center */}
          <CommunityCards cards={gameState.communityCards} />
          
          {/* Player seats */}
          {seats.map((player, index) => (
            <PlayerSeat
              key={index}
              player={player}
              position={index}
              isCurrentPlayer={player?.id === gameState.currentPlayerId}
            />
          ))}
        </div>
      </div>
    </div>
  );
}
