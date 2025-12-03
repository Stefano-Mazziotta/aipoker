'use client';

import { useState } from 'react';
import { useGame } from '@/contexts/GameContext';
import { useAuth } from '@/contexts/AuthContext';
import { PLAYER_ACTIONS } from '@/lib/constants/player-actions';

export default function ActionButtons() {
  const { gameState, isPlayerTurn, performAction } = useGame();
  const { playerChips } = useAuth();
  const [raiseAmount, setRaiseAmount] = useState(0);

  if (!gameState || !isPlayerTurn) {
    return (
      <div className="bg-black/60 backdrop-blur-sm p-4 rounded-xl border border-white/20">
        <p className="text-center text-gray-400">
          {gameState ? 'Waiting for your turn...' : 'Game not started'}
        </p>
      </div>
    );
  }

  const currentBet = gameState.currentBet;
  const canCheck = currentBet === 0;
  const callAmount = currentBet;
  const minRaise = currentBet + (currentBet || 20); // Min raise is current bet + 1 big blind

  const handleCheck = () => performAction(PLAYER_ACTIONS.CHECK);
  const handleCall = () => performAction(PLAYER_ACTIONS.CALL, callAmount);
  const handleRaise = () => {
    if (raiseAmount < minRaise) {
      alert(`Minimum raise is $${minRaise}`);
      return;
    }
    performAction(PLAYER_ACTIONS.RAISE, raiseAmount);
    setRaiseAmount(minRaise);
  };
  const handleFold = () => {
    if (confirm('Are you sure you want to fold?')) {
      performAction(PLAYER_ACTIONS.FOLD);
    }
  };
  const handleAllIn = () => {
    if (confirm('Are you sure you want to go ALL IN?')) {
      performAction(PLAYER_ACTIONS.ALL_IN);
    }
  };

  return (
    <div className="bg-black/60 backdrop-blur-sm p-4 rounded-xl border border-yellow-500/50">
      <h3 className="text-center text-yellow-500 font-bold mb-4 text-lg">
        🎯 YOUR TURN
      </h3>
      
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
        {canCheck && (
          <button
            onClick={handleCheck}
            className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded-lg transition-colors"
          >
            CHECK
          </button>
        )}
        
        {!canCheck && (
          <button
            onClick={handleCall}
            className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-4 rounded-lg transition-colors"
          >
            CALL ${callAmount}
          </button>
        )}
        
        <button
          onClick={handleFold}
          className="bg-red-600 hover:bg-red-700 text-white font-bold py-3 px-4 rounded-lg transition-colors"
        >
          FOLD
        </button>
        
        <button
          onClick={handleAllIn}
          className="bg-purple-600 hover:bg-purple-700 text-white font-bold py-3 px-4 rounded-lg transition-colors"
        >
          ALL IN
        </button>
      </div>

      {/* Raise controls */}
      <div className="space-y-2">
        <div className="flex items-center gap-2">
          <label className="text-white text-sm font-medium min-w-[60px]">
            Raise to:
          </label>
          <input
            type="range"
            min={minRaise}
            max={playerChips}
            step={10}
            value={raiseAmount}
            onChange={(e) => setRaiseAmount(parseInt(e.target.value))}
            className="flex-1"
          />
          <input
            type="number"
            value={raiseAmount}
            onChange={(e) => setRaiseAmount(parseInt(e.target.value) || minRaise)}
            min={minRaise}
            max={playerChips}
            step={10}
            className="w-24 px-2 py-1 bg-black/50 border border-white/30 rounded text-white text-center"
          />
        </div>
        <button
          onClick={handleRaise}
          disabled={raiseAmount < minRaise}
          className="w-full bg-yellow-600 hover:bg-yellow-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white font-bold py-3 px-4 rounded-lg transition-colors"
        >
          RAISE to ${raiseAmount}
        </button>
      </div>

      <div className="mt-3 text-center text-xs text-gray-400">
        Min raise: ${minRaise} | Your chips: ${playerChips}
      </div>
    </div>
  );
}
