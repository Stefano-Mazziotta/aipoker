'use client';

import { useWebSocket } from '@/contexts/WebSocketContext';

export default function ConnectionStatus() {
  const { status, isConnected } = useWebSocket();

  const statusConfig = {
    connecting: {
      color: 'bg-yellow-500',
      text: 'Connecting...',
      icon: '⚠️',
    },
    connected: {
      color: 'bg-green-500',
      text: 'Connected',
      icon: '✅',
    },
    disconnected: {
      color: 'bg-red-500',
      text: 'Disconnected',
      icon: '❌',
    },
    error: {
      color: 'bg-red-600',
      text: 'Connection Error',
      icon: '⚠️',
    },
  };

  const config = statusConfig[status];

  return (
    <div className="inline-flex items-center gap-2 px-4 py-2 bg-black/30 rounded-lg border border-white/20">
      <div className={`w-3 h-3 rounded-full ${config.color} ${isConnected ? '' : 'animate-pulse'}`} />
      <span className="text-sm font-medium text-white">
        {config.icon} {config.text}
      </span>
    </div>
  );
}
