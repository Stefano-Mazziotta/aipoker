'use client';

import PlayingCard from './PlayingCard';

interface CommunityCardsProps {
  cards: string[];
}

export default function CommunityCards({ cards }: CommunityCardsProps) {
  // Always show 5 card slots
  const displayCards = [...cards];
  while (displayCards.length < 5) {
    displayCards.push('');
  }

  return (
    <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-10">
      <div className="flex gap-2 md:gap-3">
        {displayCards.map((card, index) => (
          <PlayingCard key={index} card={card || null} />
        ))}
      </div>
    </div>
  );
}
