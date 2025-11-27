'use client';

import { useGame } from '@/contexts/GameContext';
import PlayerSeat from './PlayerSeat';
import CommunityCards from './CommunityCards';
import ActionButtons from './ActionButtons';

export default function GameTable() {
  const { gameState, isInGame } = useGame();

  if (!isInGame || !gameState) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-yellow-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-white text-xl">Waiting for game to start...</p>
        </div>
      </div>
    );
  }

  // Fill empty seats
  const seats = Array.from({ length: 9 }, (_, index) => {
    return gameState.players[index] || null;
  });

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-4 overflow-hidden">
      {/* Game info header */}
      <div className="mb-6 text-center">
        <h1 className="text-3xl md:text-4xl font-bold text-yellow-500 mb-2">
          ♠️ Texas Hold&apos;em ♥️
        </h1>
        <div className="flex gap-6 justify-center text-white">
          <div className="bg-black/60 px-4 py-2 rounded-lg">
            <span className="text-gray-400">Pot:</span>
            <span className="ml-2 text-green-400 font-bold text-xl">
              ${gameState.pot}
            </span>
          </div>
          <div className="bg-black/60 px-4 py-2 rounded-lg">
            <span className="text-gray-400">Current Bet:</span>
            <span className="ml-2 text-yellow-400 font-bold text-xl">
              ${gameState.currentBet}
            </span>
          </div>
          <div className="bg-black/60 px-4 py-2 rounded-lg">
            <span className="text-gray-400">Round:</span>
            <span className="ml-2 text-blue-400 font-bold uppercase">
              {gameState.round}
            </span>
          </div>
        </div>
      </div>

      {/* Poker table */}
      <div className="relative w-full max-w-[900px] aspect-3/2 mb-6">
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

      {/* Action buttons */}
      <div className="w-full max-w-[600px]">
        <ActionButtons />
      </div>
    </div>
  );
}
