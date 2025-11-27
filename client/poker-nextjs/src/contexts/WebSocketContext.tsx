'use client';

import React, { createContext, useContext, useEffect, useState, useRef, useCallback } from 'react';
import { WebSocketClient, WebSocketStatus } from '@/lib/websocket/client';
import { WebSocketCommands } from '@/lib/websocket/commands';
import { WebSocketEvent } from '@/lib/types/events';

interface WebSocketContextType {
  status: WebSocketStatus;
  isConnected: boolean;
  sendCommand: (command: string) => void;
  subscribe: (handler: (event: WebSocketEvent) => void) => () => void;
  commands: typeof WebSocketCommands;
}

const WebSocketContext = createContext<WebSocketContextType | null>(null);

export function WebSocketProvider({ children }: { children: React.ReactNode }) {
  const [status, setStatus] = useState<WebSocketStatus>('disconnected');
  const wsClientRef = useRef<WebSocketClient | null>(null);

  useEffect(() => {
    // Initialize WebSocket client
    const wsUrl = process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8081/ws/poker';
    const client = new WebSocketClient(wsUrl);
    wsClientRef.current = client;

    // Subscribe to status changes
    const unsubscribeStatus = client.onStatusChange(setStatus);

    // Connect
    client.connect();

    // Cleanup on unmount
    return () => {
      unsubscribeStatus();
      client.disconnect();
    };
  }, []);

  const sendCommand = useCallback((command: string) => {
    wsClientRef.current?.send(command);
  }, []);

  // Don't memoize subscribe - it needs to access current wsClientRef
  const subscribe = (handler: (event: WebSocketEvent) => void) => {
    if (!wsClientRef.current) {
      console.warn('WebSocket client not initialized yet');
      return () => {};
    }
    return wsClientRef.current.onMessage(handler);
  };

  const isConnected = status === 'connected';

  return (
    <WebSocketContext.Provider
      value={{
        status,
        isConnected,
        sendCommand,
        subscribe,
        commands: WebSocketCommands,
      }}
    >
      {children}
    </WebSocketContext.Provider>
  );
}

export function useWebSocket() {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within WebSocketProvider');
  }
  return context;
}
