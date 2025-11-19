package com.poker.shared.infrastructure.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * TCP Socket Server for multiplayer poker.
 * Listens on port 8080 and handles multiple client connections.
 */
public class SocketServer {
    private static final Logger LOGGER = Logger.getLogger(SocketServer.class.getName());
    private static final int DEFAULT_PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;
    
    private final int port;
    private final ExecutorService threadPool;
    private final ClientHandlerFactory handlerFactory;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public SocketServer(int port, ClientHandlerFactory handlerFactory) {
        this.port = port;
        this.handlerFactory = handlerFactory;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.running = false;
    }

    public static SocketServer create(ClientHandlerFactory handlerFactory) {
        return new SocketServer(DEFAULT_PORT, handlerFactory);
    }

    public void start() {
        if (running) {
            LOGGER.warning("Server is already running");
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            running = true;
            LOGGER.info("Poker server started on port " + port);
            
            acceptConnections();
        } catch (IOException e) {
            LOGGER.severe("Failed to start server: " + e.getMessage());
            throw new RuntimeException("Failed to start server", e);
        }
    }

    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                LOGGER.info("New client connected: " + clientSocket.getInetAddress());
                
                ClientHandler handler = handlerFactory.create(clientSocket);
                threadPool.execute(handler);
            } catch (IOException e) {
                if (running) {
                    LOGGER.warning("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        if (!running) {
            return;
        }

        running = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LOGGER.warning("Error closing server socket: " + e.getMessage());
        }

        threadPool.shutdown();
        LOGGER.info("Poker server stopped");
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }
}
