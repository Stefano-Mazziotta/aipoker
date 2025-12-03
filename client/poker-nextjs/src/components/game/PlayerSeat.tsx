'use client';

import { PlayerStateDTO } from '@/lib/types/player';
import { useAuth } from '@/contexts/AuthContext';
import { useGame } from '@/contexts/GameContext';
import PlayingCard from './PlayingCard';
import ActionButtons from './ActionButtons';

interface PlayerSeatProps {
  player: PlayerStateDTO | null;
  position: number;
  isCurrentPlayer: boolean;
}

// 9 seat positions - perfectly symmetric around the table
const SEAT_POSITIONS = [
  'bottom-[-15%] left-1/2 -translate-x-1/2',              // 0: Bottom center (hero)
  'bottom-[10%] left-[5%]',                               // 1: Bottom left  
  'top-1/2 left-[-3%] -translate-y-1/2',                  // 2: Middle left
  'top-[10%] left-[10%]',                                 // 3: Top left
  'top-[-6%] left-1/2 -translate-x-1/2',                  // 4: Top center
  'top-[10%] right-[10%]',                                // 5: Top right
  'top-1/2 right-[-3%] -translate-y-1/2',                 // 6: Middle right
  'bottom-[10%] right-[5%]',                              // 7: Bottom right
  'bottom-[10%] right-[25%]',                             // 8: Bottom right-center
];

export default function PlayerSeat({ player, position, isCurrentPlayer }: PlayerSeatProps) {
  const { playerId } = useAuth();
  const { isPlayerTurn } = useGame();

  if (!player) {
    return (
      <div className={`absolute ${SEAT_POSITIONS[position]} hidden md:block`}>
        <div className="bg-gray-700/30 border-2 border-gray-600 rounded-2xl p-3 min-w-[150px] opacity-50">
          <p className="text-gray-500 text-center text-sm">Empty Seat</p>
        </div>
      </div>
    );
  }

  const isYou = player.id === playerId;
  const isFolded = player.folded;
  
  // Debug logging
  if (isYou) {
    console.log('PlayerSeat rendering for YOU:', {
      playerId: player.id,
      name: player.name,
      cards: player.cards,
      cardsLength: player.cards?.length,
      position,
      isPlayerTurn,
      isFolded
    });
  }

  return (
    <div className={`absolute ${SEAT_POSITIONS[position]}`}>
      <div
        className={`
          bg-black/80 backdrop-blur-sm rounded-xl p-3
          ${isYou ? 'w-[300px] sm:w-[340px]' : 'min-w-[140px] w-[140px]'}
          border-2 transition-all shadow-lg
          ${isCurrentPlayer ? 'border-yellow-400 shadow-yellow-400/50' : 'border-white/30'}
          ${isYou ? 'ring-2 ring-green-500' : ''}
          ${isFolded ? 'opacity-50 grayscale' : ''}
        `}
      >
        {/* Player info */}
        <div className="text-center mb-2">
          <div className="flex items-center justify-center gap-1 mb-1">
            {player.isDealer && <span className="text-lg">🎯</span>}
            {player.isSmallBlind && <span className="text-xs bg-blue-500 px-1 rounded">SB</span>}
            {player.isBigBlind && <span className="text-xs bg-red-500 px-1 rounded">BB</span>}
          </div>
          <p className={`font-bold truncate ${isYou ? 'text-green-400' : 'text-yellow-500'}`}>
            {player.name}
            {isYou && <span className="text-xs ml-1">(You)</span>}
          </p>
          <p className="text-white text-sm">${player.chips}</p>
          {player.bet > 0 && (
            <p className="text-yellow-400 text-sm font-bold">Bet: ${player.bet}</p>
          )}
        </div>

        {/* Player cards (only show for current player) */}
        {isYou && player.cards && player.cards.length > 0 && (
          <div className="flex gap-1 justify-center mb-2">
            {player.cards.map((card, index) => (
              <div key={index} className="scale-75">
                <PlayingCard card={card} />
              </div>
            ))}
          </div>
        )}

        {/* Action buttons integrated for your seat */}
        {isYou && !isFolded && (
          <div className="mt-2">
            <ActionButtons compact={true} />
          </div>
        )}

        {/* Folded indicator */}
        {isFolded && (
          <div className="text-center text-red-500 text-sm font-bold mt-2">
            FOLDED
          </div>
        )}
      </div>
    </div>
  );
}
