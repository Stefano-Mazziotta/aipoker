'use client';

import { useState, FormEvent } from 'react';
import { useLobby } from '@/contexts/LobbyContext';
import { useAuth } from '@/contexts/AuthContext';

export default function LobbyControls() {
  const [lobbyName, setLobbyName] = useState('');
  const [maxPlayers, setMaxPlayers] = useState(9);
  const [joinLobbyId, setJoinLobbyId] = useState('');
  const [showCreate, setShowCreate] = useState(true);
  
  const { createLobby, joinLobby, leaveLobby, isInLobby, startGame, isLobbyAdmin, lobbyPlayers, lobbyId } = useLobby();
  const { isRegistered } = useAuth();

  const copyLobbyId = () => {
    if (lobbyId) {
      navigator.clipboard.writeText(lobbyId);
      alert('Lobby ID copied to clipboard!');
    }
  };

  const handleCreateLobby = (e: FormEvent) => {
    e.preventDefault();
    if (!lobbyName.trim()) {
      alert('Please enter a lobby name');
      return;
    }
    createLobby(lobbyName, maxPlayers);
  };

  const handleJoinLobby = (e: FormEvent) => {
    e.preventDefault();
    if (!joinLobbyId.trim()) {
      alert('Please enter a lobby ID');
      return;
    }
    joinLobby(joinLobbyId);
  };

  const handleStartGame = () => {
    const smallBlind = 10;
    const bigBlind = 20;
    startGame(smallBlind, bigBlind);
  };

  if (!isRegistered) {
    return (
      <div className="bg-linear-to-br from-green-900/50 to-green-950/50 p-6 rounded-2xl border-2 border-white/20 opacity-50">
        <h3 className="text-2xl font-bold text-gray-400 mb-4">2️⃣ Join or Create a Lobby</h3>
        <p className="text-gray-400">Please register first</p>
      </div>
    );
  }

  if (isInLobby) {
    return (
      <div className="bg-linear-to-br from-green-900/50 to-green-950/50 p-6 rounded-2xl border-2 border-yellow-500/50">
        <h3 className="text-2xl font-bold text-yellow-500 mb-4">🎮 In Lobby</h3>
        <div className="space-y-4">
          {lobbyId && (
            <div className="bg-black/30 p-3 rounded-lg">
              <label className="text-sm text-gray-300 block mb-1">Lobby ID (share with others)</label>
              <div 
                className="flex items-center justify-between gap-2 cursor-pointer hover:bg-black/50 p-2 rounded"
                onClick={copyLobbyId}
                title="Click to copy"
              >
                <code className="text-yellow-400 font-mono text-sm flex-1 break-all">
                  {lobbyId}
                </code>
                <span className="text-2xl">📋</span>
              </div>
            </div>
          )}
          
          <div className="flex items-center justify-between">
            <span className="text-white">Players: {lobbyPlayers.length}</span>
            {isLobbyAdmin && (
              <span className="bg-yellow-500 text-black px-3 py-1 rounded-full text-sm font-bold">
                ADMIN
              </span>
            )}
          </div>
          
          {isLobbyAdmin && (
            <button
              onClick={handleStartGame}
              disabled={lobbyPlayers.length < 2}
              className="w-full bg-green-600 hover:bg-green-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white font-bold py-3 px-6 rounded-lg transition-colors"
            >
              {lobbyPlayers.length < 2 ? 'Need at least 2 players' : 'START GAME'}
            </button>
          )}
          
          <button
            onClick={leaveLobby}
            className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-3 px-6 rounded-lg transition-colors"
          >
            LEAVE LOBBY
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-linear-to-br from-green-900/50 to-green-950/50 p-6 rounded-2xl border-2 border-white/20">
      <h3 className="text-2xl font-bold text-yellow-500 mb-4">2️⃣ Join or Create a Lobby</h3>
      
      <div className="flex gap-2 mb-4">
        <button
          onClick={() => setShowCreate(true)}
          className={`flex-1 py-2 px-4 rounded-lg font-medium transition-colors ${
            showCreate 
              ? 'bg-yellow-500 text-black' 
              : 'bg-black/30 text-white hover:bg-black/50'
          }`}
        >
          Create
        </button>
        <button
          onClick={() => setShowCreate(false)}
          className={`flex-1 py-2 px-4 rounded-lg font-medium transition-colors ${
            !showCreate 
              ? 'bg-yellow-500 text-black' 
              : 'bg-black/30 text-white hover:bg-black/50'
          }`}
        >
          Join
        </button>
      </div>

      {showCreate ? (
        <form onSubmit={handleCreateLobby} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-white mb-2">
              Lobby Name
            </label>
            <input
              type="text"
              value={lobbyName}
              onChange={(e) => setLobbyName(e.target.value)}
              placeholder="Enter lobby name"
              className="w-full px-4 py-2 bg-black/50 border border-white/30 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-yellow-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-white mb-2">
              Max Players (2-9)
            </label>
            <input
              type="number"
              value={maxPlayers}
              onChange={(e) => setMaxPlayers(Math.min(9, Math.max(2, parseInt(e.target.value) || 2)))}
              min={2}
              max={9}
              className="w-full px-4 py-2 bg-black/50 border border-white/30 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-yellow-500"
            />
          </div>
          <button
            type="submit"
            className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-lg transition-colors"
          >
            CREATE LOBBY
          </button>
        </form>
      ) : (
        <form onSubmit={handleJoinLobby} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-white mb-2">
              Lobby ID
            </label>
            <input
              type="text"
              value={joinLobbyId}
              onChange={(e) => setJoinLobbyId(e.target.value)}
              placeholder="Enter lobby ID"
              className="w-full px-4 py-2 bg-black/50 border border-white/30 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-yellow-500"
            />
          </div>
          <button
            type="submit"
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg transition-colors"
          >
            JOIN LOBBY
          </button>
        </form>
      )}
    </div>
  );
}
