package com.poker.shared.infrastructure.websocket;

import java.util.logging.Logger;

import org.glassfish.tyrus.server.Server;

import jakarta.websocket.DeploymentException;

/**
 * WebSocket server for real-time poker game communication.
 * Replaces the old TCP socket server with WebSocket for better browser compatibility.
 */
public class WebSocketServer {
    private static final Logger LOGGER = Logger.getLogger(WebSocketServer.class.getName());
    private static final int DEFAULT_PORT = 8081;
    private static final String DEFAULT_HOST = "localhost";
    
    private Server server;
    private final int port;
    private final String host;
    private volatile boolean running;

    public WebSocketServer() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public WebSocketServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.running = false;
    }

    /**
     * Start the WebSocket server.
     */
    public void start() {
        if (running) {
            LOGGER.warning("WebSocket server already running");
            return;
        }

        try {
            server = new Server(host, port, "/ws", null, PokerWebSocketEndpoint.class);
            server.start();
            running = true;
            
            LOGGER.info(() -> String.format("WebSocket server started on ws://%s:%d/ws/poker", host, port));
            LOGGER.info("Ready to accept WebSocket connections");
            
        } catch (DeploymentException exception) {
            LOGGER.severe(() -> String.format("Failed to start WebSocket server: %s", exception.getMessage()));
            throw new RuntimeException("Could not start WebSocket server", exception);
        }
    }

    /**
     * Stop the WebSocket server.
     */
    public void stop() {
        if (!running) {
            return;
        }

        try {
            if (server != null) {
                server.stop();
            }
            running = false;
            LOGGER.info("WebSocket server stopped");
        } catch (Exception e) {
            LOGGER.severe(() -> String.format("Error stopping WebSocket server: %s", e.getMessage()));
        }
    }

    /**
     * Check if server is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get server port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get server host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Wait for server to terminate.
     */
    public void awaitTermination() {
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warning("Server await interrupted");
                break;
            }
        }
    }
}
