package com.poker.shared.infrastructure.socket;

import java.net.Socket;

/**
 * Factory interface for creating ClientHandler instances.
 */
public interface ClientHandlerFactory {
    ClientHandler create(Socket socket);
}
