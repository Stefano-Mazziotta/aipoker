'use client';

import { useLobby } from '@/contexts/LobbyContext';
import { useAuth } from '@/contexts/AuthContext';

export default function PlayersList() {
  const { lobbyPlayers, isInLobby } = useLobby();
  const { playerId } = useAuth();

  if (!isInLobby || lobbyPlayers.length === 0) {
    return null;
  }

  return (
    <div className="bg-linear-to-br from-green-900/50 to-green-950/50 p-6 rounded-2xl border-2 border-white/20">
      <h3 className="text-xl font-bold text-yellow-500 mb-4">👥 Players in Lobby</h3>
      <div className="space-y-2">
        {lobbyPlayers.map((player, index) => (
          <div
            key={player.id}
            className={`flex items-center justify-between p-3 rounded-lg ${
              player.id === playerId
                ? 'bg-yellow-500/20 border-2 border-yellow-500'
                : 'bg-black/30 border border-white/20'
            }`}
          >
            <div className="flex items-center gap-3">
              <span className="text-2xl">
                {index === 0 ? '👑' : '🃏'}
              </span>
              <div>
                <p className="font-bold text-white">
                  {player.name}
                  {player.id === playerId && (
                    <span className="ml-2 text-yellow-500 text-sm">(You)</span>
                  )}
                </p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
