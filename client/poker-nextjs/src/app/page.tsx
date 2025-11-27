'use client';

import { useGame } from '@/contexts/GameContext';
import { useLobby } from '@/contexts/LobbyContext';
import ConnectionStatus from '@/components/auth/ConnectionStatus';
import RegisterForm from '@/components/auth/RegisterForm';
import LobbyControls from '@/components/lobby/LobbyControls';
import PlayersList from '@/components/lobby/PlayersList';
import GameTable from '@/components/game/GameTable';

export default function Home() {
  const { isInGame } = useGame();
  const { isInLobby } = useLobby();

  // Show game table when in game
  if (isInGame) {
    return <GameTable />;
  }

  // Show lobby/registration screen
  return (
    <div className="min-h-screen flex flex-col items-center justify-start p-4 md:p-8 overflow-y-auto">
      {/* Header */}
      <div className="text-center mb-8">
        <h1 className="text-4xl md:text-6xl font-bold text-yellow-500 mb-3 drop-shadow-lg">
          ♠️ Texas Hold&apos;em Poker ♥️
        </h1>
        <p className="text-lg md:text-xl text-gray-300 mb-4">
          Join a game and test your poker skills!
        </p>
        <ConnectionStatus />
      </div>

      {/* Main content */}
      <div className="w-full max-w-4xl mx-auto">
        <div className="grid md:grid-cols-2 gap-6 mb-6">
          {/* Registration */}
          <RegisterForm />
          
          {/* Lobby controls */}
          <LobbyControls />
        </div>

        {/* Players list (shown when in lobby) */}
        {isInLobby && (
          <div className="mt-6">
            <PlayersList />
          </div>
        )}

        {/* Instructions */}
        {!isInLobby && (
          <div className="mt-8 bg-black/30 p-6 rounded-2xl border border-white/20">
            <h2 className="text-2xl font-bold text-yellow-500 mb-4">📖 How to Play</h2>
            <div className="space-y-2 text-gray-300">
              <p>1. <strong className="text-white">Register</strong> your player with a name and starting chips</p>
              <p>2. <strong className="text-white">Create a lobby</strong> or <strong className="text-white">join</strong> an existing one</p>
              <p>3. Wait for other players to join (min 2 players required)</p>
              <p>4. The lobby admin starts the game when ready</p>
              <p>5. Play Texas Hold&apos;em poker with standard rules!</p>
            </div>
            
            <div className="mt-6 p-4 bg-yellow-500/10 border border-yellow-500/30 rounded-lg">
              <h3 className="text-lg font-bold text-yellow-500 mb-2">🎮 Game Actions</h3>
              <div className="grid grid-cols-2 gap-2 text-sm text-gray-300">
                <div><strong className="text-white">CHECK:</strong> Pass if no bet</div>
                <div><strong className="text-white">CALL:</strong> Match current bet</div>
                <div><strong className="text-white">RAISE:</strong> Increase the bet</div>
                <div><strong className="text-white">FOLD:</strong> Give up your hand</div>
                <div><strong className="text-white">ALL IN:</strong> Bet all chips</div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="mt-auto pt-8 text-center text-gray-400 text-sm">
        <p>Built with Next.js 14+ • TypeScript • Tailwind CSS • WebSockets</p>
      </div>
    </div>
  );
}
