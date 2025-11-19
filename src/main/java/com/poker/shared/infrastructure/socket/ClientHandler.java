package com.poker.shared.infrastructure.socket;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Handles individual client connections in separate threads.
 * Processes commands and sends responses.
 */
public class ClientHandler implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    
    private final Socket socket;
    private final ProtocolHandler protocolHandler;
    private final MessageFormatter messageFormatter;
    private BufferedReader input;
    private PrintWriter output;
    private volatile boolean running;

    public ClientHandler(Socket socket, ProtocolHandler protocolHandler, MessageFormatter messageFormatter) {
        this.socket = socket;
        this.protocolHandler = protocolHandler;
        this.messageFormatter = messageFormatter;
        this.running = false;
    }

    @Override
    public void run() {
        try {
            initialize();
            running = true;
            
            sendWelcome();
            processCommands();
            
        } catch (IOException e) {
            LOGGER.warning("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void initialize() throws IOException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }

    private void sendWelcome() {
        String welcome = messageFormatter.formatWelcome();
        output.println(welcome);
    }

    private void processCommands() throws IOException {
        String command;
        while (running && (command = input.readLine()) != null) {
            LOGGER.info("Received command: " + command);
            
            try {
                String response = protocolHandler.handle(command);
                output.println(response);
            } catch (Exception e) {
                String errorResponse = messageFormatter.formatError(e.getMessage());
                output.println(errorResponse);
                LOGGER.warning("Error processing command: " + e.getMessage());
            }
            
            if ("QUIT".equalsIgnoreCase(command.trim())) {
                running = false;
            }
        }
    }

    private void cleanup() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
            
            LOGGER.info("Client disconnected: " + socket.getInetAddress());
        } catch (IOException e) {
            LOGGER.warning("Error closing client connection: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
    }
}
