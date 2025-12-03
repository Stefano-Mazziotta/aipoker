'use client';

interface WinnerModalProps {
  winnerName: string;
  handRank: string;
  amountWon: number;
  onClose: () => void;
}

export function WinnerModal({ winnerName, handRank, amountWon, onClose }: WinnerModalProps) {
  return (
    <div 
      className="fixed inset-0 bg-black/80 flex items-center justify-center z-50 backdrop-blur-sm"
      onClick={onClose}
    >
      <div 
        className="relative animate-bounce-in"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Celebration background glow */}
        <div className="absolute inset-0 bg-yellow-500/30 blur-3xl animate-pulse" />
        
        {/* Modal content */}
        <div className="relative bg-gradient-to-br from-yellow-600 via-yellow-700 to-yellow-900 p-8 md:p-12 rounded-3xl shadow-2xl text-center border-4 border-yellow-400">
          {/* Trophy icon */}
          <div className="text-8xl mb-4 animate-wiggle">
            🏆
          </div>
          
          {/* Winner name */}
          <h1 className="text-4xl md:text-5xl font-bold text-white mb-4 drop-shadow-lg">
            {winnerName} Wins!
          </h1>
          
          {/* Hand rank */}
          <div className="bg-black/30 px-6 py-3 rounded-xl mb-4 inline-block">
            <p className="text-2xl md:text-3xl text-yellow-100 font-semibold">
              {handRank}
            </p>
          </div>
          
          {/* Amount won */}
          <div className="bg-green-600 px-8 py-4 rounded-xl shadow-xl border-2 border-green-400">
            <p className="text-sm text-green-200 uppercase tracking-wide mb-1">
              Prize
            </p>
            <p className="text-4xl md:text-5xl text-white font-bold">
              ${amountWon.toLocaleString()}
            </p>
          </div>
          
          {/* Sparkles decoration */}
          <div className="absolute -top-4 -left-4 text-4xl animate-spin-slow">✨</div>
          <div className="absolute -top-4 -right-4 text-4xl animate-spin-slow-reverse">✨</div>
          <div className="absolute -bottom-4 -left-4 text-4xl animate-spin-slow-reverse">✨</div>
          <div className="absolute -bottom-4 -right-4 text-4xl animate-spin-slow">✨</div>
          
          {/* Close hint */}
          <p className="text-yellow-200 text-sm mt-6 opacity-75">
            Click anywhere to continue
          </p>
        </div>
      </div>
    </div>
  );
}
