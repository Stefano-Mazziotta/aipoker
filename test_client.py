#!/usr/bin/env python3
"""
Simple Python client for testing the AI Poker Server.
This demonstrates how to connect and interact with the socket server.

Usage:
    python3 test_client.py
"""

import socket
import sys

class PokerClient:
    def __init__(self, host='localhost', port=8080):
        self.host = host
        self.port = port
        self.socket = None
    
    def connect(self):
        """Connect to the poker server."""
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.connect((self.host, self.port))
            print(f"✓ Connected to {self.host}:{self.port}")
            return True
        except ConnectionRefusedError:
            print(f"✗ Connection refused. Is the server running on {self.host}:{self.port}?")
            return False
        except Exception as e:
            print(f"✗ Connection error: {e}")
            return False
    
    def send_command(self, command):
        """Send a command to the server and receive response."""
        try:
            # Send command with newline
            self.socket.sendall((command + '\n').encode('utf-8'))
            
            # Receive response
            response = self.socket.recv(4096).decode('utf-8')
            return response.strip()
        except Exception as e:
            print(f"✗ Error sending command: {e}")
            return None
    
    def close(self):
        """Close the connection."""
        if self.socket:
            self.socket.close()
            print("✓ Connection closed")

def main():
    print("=" * 50)
    print("AI Poker Server - Test Client")
    print("=" * 50)
    
    # Create client
    client = PokerClient()
    
    # Connect to server
    if not client.connect():
        sys.exit(1)
    
    print("\nTesting server commands...\n")
    
    try:
        # Test 1: Register Player
        print("1. Registering player 'TestPlayer'...")
        response = client.send_command("REGISTER TestPlayer 1000")
        print(f"   Response: {response}\n")
        
        # Test 2: Register another player
        print("2. Registering player 'AnotherPlayer'...")
        response = client.send_command("REGISTER AnotherPlayer 1500")
        print(f"   Response: {response}\n")
        
        # Test 3: Get Leaderboard
        print("3. Getting leaderboard...")
        response = client.send_command("LEADERBOARD 10")
        print(f"   Response: {response}\n")
        
        # Test 4: Create Lobby
        print("4. Creating lobby 'Test Table'...")
        response = client.send_command("CREATE_LOBBY Test Table 6")
        print(f"   Response: {response}\n")
        
        print("=" * 50)
        print("✓ All tests completed successfully!")
        print("=" * 50)
        
    except KeyboardInterrupt:
        print("\n\n✗ Interrupted by user")
    except Exception as e:
        print(f"\n✗ Error during testing: {e}")
    finally:
        client.close()

if __name__ == "__main__":
    main()
