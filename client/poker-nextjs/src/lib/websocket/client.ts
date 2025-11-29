'use client';

import { ServerEvent } from '../types/server-events';
import { WebSocketCommand } from '../types/commands';

export type WebSocketStatus = 'connecting' | 'connected' | 'disconnected' | 'error';

export type MessageHandler = (event: ServerEvent) => void;

export class WebSocketClient {
  private ws: WebSocket | null = null;
  private url: string;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000; // Start with 1 second
  private messageHandlers: Set<MessageHandler> = new Set();
  private statusHandlers: Set<(status: WebSocketStatus) => void> = new Set();
  private reconnectTimeout: NodeJS.Timeout | null = null;

  constructor(url: string) {
    this.url = url;
  }

  connect(): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      console.log('WebSocket already connected');
      return;
    }

    this.notifyStatus('connecting');
    
    try {
      this.ws = new WebSocket(this.url);
      
      this.ws.onopen = () => {
        console.log('WebSocket connected');
        this.reconnectAttempts = 0;
        this.reconnectDelay = 1000;
        this.notifyStatus('connected');
      };

      this.ws.onclose = (event) => {
        console.log('WebSocket closed:', event.code, event.reason);
        this.notifyStatus('disconnected');
        this.handleReconnect();
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        this.notifyStatus('error');
      };

      this.ws.onmessage = (event) => {
        try {
          const response = JSON.parse(event.data);
          
          // Backend sends two formats:
          // 1. Command responses: { type: "...", data: {...}, success: true/false, message: "..." }
          // 2. Domain events: { eventType: "...", data: {...}, timestamp: "...", eventId: "..." }
          const eventType = response.type || response.eventType;
          
          // Flatten response data into the event for easier access
          // Only spread data if it's an object, not a string or primitive
          const dataToSpread = response.data && typeof response.data === 'object' && !Array.isArray(response.data)
            ? response.data
            : {};
          
          const message: ServerEvent = {
            eventType: eventType?.toUpperCase() || 'UNKNOWN',
            timestamp: response.timestamp || Date.now(),
            ...response,
            ...dataToSpread
          } as ServerEvent;
          
          // Log errors
          if (response.success === false && response.message) {
            console.error('WebSocket error response:', response.message);
          }
          
          this.notifyMessageHandlers(message);
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error, event.data);
        }
      };
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
      this.notifyStatus('error');
      this.handleReconnect();
    }
  }

  private handleReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnect attempts reached');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1); // Exponential backoff

    console.log(`Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
    
    this.reconnectTimeout = setTimeout(() => {
      this.connect();
    }, delay);
  }

  send(command: WebSocketCommand<unknown>): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      const message = JSON.stringify(command);
      this.ws.send(message);
    } else {
      console.error('WebSocket is not connected. Cannot send command:', command);
    }
  }

  disconnect(): void {
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }
    
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    
    this.notifyStatus('disconnected');
  }

  onMessage(handler: MessageHandler): () => void {
    this.messageHandlers.add(handler);
    // Return unsubscribe function
    return () => {
      this.messageHandlers.delete(handler);
    };
  }

  onStatusChange(handler: (status: WebSocketStatus) => void): () => void {
    this.statusHandlers.add(handler);
    // Return unsubscribe function
    return () => {
      this.statusHandlers.delete(handler);
    };
  }

  private notifyMessageHandlers(message: ServerEvent): void {
    this.messageHandlers.forEach(handler => {
      try {
        handler(message);
      } catch (error) {
        console.error('Error in message handler:', error);
      }
    });
  }

  private notifyStatus(status: WebSocketStatus): void {
    this.statusHandlers.forEach(handler => {
      try {
        handler(status);
      } catch (error) {
        console.error('Error in status handler:', error);
      }
    });
  }

  getStatus(): WebSocketStatus {
    if (!this.ws) return 'disconnected';
    
    switch (this.ws.readyState) {
      case WebSocket.CONNECTING:
        return 'connecting';
      case WebSocket.OPEN:
        return 'connected';
      case WebSocket.CLOSING:
      case WebSocket.CLOSED:
        return 'disconnected';
      default:
        return 'error';
    }
  }

  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }
}
