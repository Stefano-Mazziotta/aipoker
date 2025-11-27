'use client';

import { useState, FormEvent } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useWebSocket } from '@/contexts/WebSocketContext';

export default function RegisterForm() {
  const [name, setName] = useState('');
  const [chips, setChips] = useState(1000);
  const { register, isRegistered, playerName, playerId, playerChips } = useAuth();
  const { isConnected } = useWebSocket();

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      alert('Please enter a player name');
      return;
    }
    if (chips < 100) {
      alert('Minimum chips is 100');
      return;
    }
    register(name, chips);
  };

  const copyPlayerId = () => {
    if (playerId) {
      navigator.clipboard.writeText(playerId);
      alert('Player ID copied to clipboard!');
    }
  };

  if (isRegistered) {
    return (
      <div className="bg-linear-to-br from-green-900/50 to-green-950/50 p-6 rounded-2xl border-2 border-yellow-500/50">
        <h3 className="text-2xl font-bold text-yellow-500 mb-4">✅ Registered</h3>
        <div className="space-y-3">
          <div>
            <label className="text-sm text-gray-300">Player Name</label>
            <p className="text-lg font-bold text-white">{playerName}</p>
          </div>
          <div>
            <label className="text-sm text-gray-300">Chips</label>
            <p className="text-lg font-bold text-green-400">${playerChips}</p>
          </div>
          <div>
            <label className="text-sm text-gray-300">Player ID</label>
            <div 
              className="text-sm font-mono text-yellow-400 cursor-pointer hover:text-yellow-300 bg-black/30 p-2 rounded"
              onClick={copyPlayerId}
              title="Click to copy"
            >
              {playerId}
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-linear-to-br from-green-900/50 to-green-950/50 p-6 rounded-2xl border-2 border-white/20">
      <h3 className="text-2xl font-bold text-yellow-500 mb-4">1️⃣ Register Your Player</h3>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-white mb-2">
            Player Name
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Enter your name"
            disabled={!isConnected}
            className="w-full px-4 py-2 bg-black/50 border border-white/30 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-yellow-500 disabled:opacity-50 disabled:cursor-not-allowed"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-white mb-2">
            Starting Chips
          </label>
          <input
            type="number"
            value={chips}
            onChange={(e) => setChips(parseInt(e.target.value) || 0)}
            min={100}
            step={100}
            disabled={!isConnected}
            className="w-full px-4 py-2 bg-black/50 border border-white/30 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-yellow-500 disabled:opacity-50 disabled:cursor-not-allowed"
          />
        </div>
        <button
          type="submit"
          disabled={!isConnected}
          className="w-full bg-green-600 hover:bg-green-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white font-bold py-3 px-6 rounded-lg transition-colors"
        >
          {isConnected ? 'REGISTER' : 'Waiting for connection...'}
        </button>
      </form>
    </div>
  );
}
