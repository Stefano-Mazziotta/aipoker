'use client';

import { SUIT_SYMBOLS, SUIT_COLORS } from '@/lib/types/game';

interface CardProps {
  card: string | null;
}

export default function PlayingCard({ card }: CardProps) {
  if (!card) {
    // Empty card placeholder
    return (
      <div className="w-16 h-24 md:w-20 md:h-28 bg-gray-700/50 border-2 border-gray-600 rounded-lg flex items-center justify-center">
        <span className="text-4xl text-gray-500">🂠</span>
      </div>
    );
  }

  // Parse card string (e.g., "ACE_HEARTS")
  const [rank, suit] = card.split('_');
  const suitSymbol = SUIT_SYMBOLS[suit] || '?';
  const suitColor = SUIT_COLORS[suit] || 'text-gray-500';

  // Map rank names to display values
  const rankDisplay = rank === 'ACE' ? 'A' :
                      rank === 'KING' ? 'K' :
                      rank === 'QUEEN' ? 'Q' :
                      rank === 'JACK' ? 'J' :
                      rank === 'TEN' ? '10' :
                      rank.charAt(0);

  return (
    <div className="w-16 h-24 md:w-20 md:h-28 bg-white rounded-lg shadow-lg flex flex-col items-center justify-center border-2 border-gray-300 hover:shadow-xl transition-shadow">
      <div className={`text-2xl md:text-3xl font-bold ${suitColor}`}>
        {rankDisplay}
      </div>
      <div className="text-3xl md:text-4xl">
        {suitSymbol}
      </div>
    </div>
  );
}
