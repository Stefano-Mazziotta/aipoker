'use client';

interface PhaseIndicatorProps {
  currentPhase: string;
}

export function PhaseIndicator({ currentPhase }: PhaseIndicatorProps) {
  const phases = ['PRE_FLOP', 'FLOP', 'TURN', 'RIVER', 'SHOWDOWN'];
  const currentIndex = phases.indexOf(currentPhase);

  return (
    <div className="flex gap-2 justify-center mb-4 flex-wrap">
      {phases.map((phase, index) => {
        const isCompleted = index < currentIndex;
        const isCurrent = index === currentIndex;
        const isPending = index > currentIndex;

        return (
          <div
            key={phase}
            className={`
              px-3 py-2 rounded-lg transition-all duration-300 text-sm md:text-base font-semibold
              ${isCompleted ? 'bg-green-500 text-white' : ''}
              ${isCurrent ? 'bg-yellow-500 text-black ring-4 ring-yellow-400/50 scale-110 shadow-lg' : ''}
              ${isPending ? 'bg-gray-700 text-gray-400' : ''}
            `}
          >
            {/* Phase icon */}
            <span className="mr-2">
              {isCompleted && '✓'}
              {isCurrent && '▶'}
              {isPending && '○'}
            </span>
            {phase.replace('_', ' ')}
          </div>
        );
      })}
    </div>
  );
}
