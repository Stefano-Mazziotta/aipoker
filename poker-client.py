#!/usr/bin/env python3
"""
WebSocket Poker Client - Terminal-based client for testing poker game flow
Usage: python3 poker-client.py [player_name]
"""

import asyncio
import websockets
import json
import sys
import readline  # For command history in terminal

class PokerClient:
    def __init__(self, player_name):
        self.player_name = player_name
        self.ws = None
        self.player_id = None
        self.game_id = None
        self.lobby_id = None
        
    async def connect(self, uri="ws://localhost:8081/ws/poker"):
        """Connect to the WebSocket server"""
        print(f"🎰 Connecting to {uri}...")
        try:
            self.ws = await websockets.connect(uri)
            print(f"✅ Connected successfully!\n")
            return True
        except Exception as e:
            print(f"❌ Connection failed: {e}")
            return False
    
    async def send_command(self, command):
        """Send a command to the server"""
        if not self.ws:
            print("❌ Not connected to server")
            return
        
        try:
            # Send command as JSON with 'command' field
            message = json.dumps({"command": command})
            await self.ws.send(message)
            print(f"📤 Sent: {command}")
        except Exception as e:
            print(f"❌ Error sending command: {e}")
    
    async def receive_messages(self):
        """Listen for messages from the server"""
        try:
            async for message in self.ws:
                try:
                    # Try to parse as JSON
                    data = json.loads(message)
                    self.handle_message(data)
                except json.JSONDecodeError:
                    # Plain text response
                    print(f"📥 {message}\n")
        except websockets.exceptions.ConnectionClosed:
            print("\n❌ Connection closed by server")
        except Exception as e:
            print(f"\n❌ Error receiving message: {e}")
    
    def handle_message(self, data):
        """Handle parsed JSON messages"""
        msg_type = data.get('type', 'UNKNOWN')
        
        if msg_type == 'PLAYER_REGISTERED':
            self.player_id = data.get('playerId')
            print(f"📥 ✅ Player registered!")
            print(f"   Player ID: {self.player_id}")
            print(f"   Name: {data.get('name')}")
            print(f"   Chips: {data.get('chips')}\n")
        
        elif msg_type == 'GAME_STARTED':
            self.game_id = data.get('gameId')
            print(f"📥 🎮 Game started!")
            print(f"   Game ID: {self.game_id}")
            print(f"   Players: {', '.join(data.get('playerIds', []))}\n")
        
        elif msg_type == 'LOBBY_CREATED':
            self.lobby_id = data.get('lobbyId')
            print(f"📥 🏠 Lobby created!")
            print(f"   Lobby ID: {self.lobby_id}")
            print(f"   Name: {data.get('name')}")
            print(f"   Max Players: {data.get('maxPlayers')}\n")
        
        elif msg_type == 'LOBBY_JOINED':
            self.lobby_id = data.get('lobbyId')
            print(f"📥 🏠 Joined lobby!")
            print(f"   Lobby ID: {self.lobby_id}\n")
        
        elif msg_type == 'PLAYER_CARDS':
            print(f"📥 🃏 Your cards:")
            for card in data.get('cards', []):
                print(f"   {card}")
            print()
        
        elif msg_type == 'GAME_STATE':
            print(f"📥 🎮 Game State:")
            print(f"   Pot: {data.get('pot', 0)}")
            print(f"   Current bet: {data.get('currentBet', 0)}")
            print(f"   Active players: {len(data.get('activePlayers', []))}")
            if 'communityCards' in data:
                print(f"   Community cards: {', '.join(data.get('communityCards', []))}")
            print()
        
        elif msg_type == 'ERROR':
            print(f"📥 ❌ Error: {data.get('message')}\n")
        
        elif msg_type == 'INFO':
            print(f"📥 ℹ️  {data.get('message')}\n")
        
        else:
            # Print the entire message for unknown types
            print(f"📥 {json.dumps(data, indent=2)}\n")
    
    async def interactive_mode(self):
        """Interactive command mode"""
        print(f"═══════════════════════════════════════════════════")
        print(f"  🎴 POKER CLIENT - {self.player_name}")
        print(f"═══════════════════════════════════════════════════")
        print(f"Type 'help' for available commands")
        print(f"Type 'quick' for quick command shortcuts")
        print(f"Type 'exit' to quit\n")
        
        # Start message receiver in background
        receive_task = asyncio.create_task(self.receive_messages())
        
        try:
            while True:
                try:
                    # Read command from user
                    command = await asyncio.get_event_loop().run_in_executor(
                        None, input, f"{self.player_name}> "
                    )
                    
                    command = command.strip()
                    
                    if not command:
                        continue
                    
                    # Handle special commands
                    if command.lower() == 'exit':
                        break
                    
                    elif command.lower() == 'help':
                        self.show_help()
                        continue
                    
                    elif command.lower() == 'quick':
                        self.show_quick_commands()
                        continue
                    
                    elif command.lower() == 'info':
                        self.show_info()
                        continue
                    
                    # Send command to server
                    await self.send_command(command)
                    
                except EOFError:
                    break
                except KeyboardInterrupt:
                    print("\n^C")
                    break
        
        finally:
            receive_task.cancel()
            if self.ws:
                await self.ws.close()
    
    def show_help(self):
        """Show available commands"""
        print("""
╔══════════════════════════════════════════════════════════════════════╗
║                        AVAILABLE COMMANDS                            ║
╚══════════════════════════════════════════════════════════════════════╝

Player Management:
  REGISTER <name> <chips>              Register a new player
  LEADERBOARD [limit]                  Show top players

Lobby Management:
  CREATE_LOBBY <name> <maxPlayers> <adminId>    Create a new lobby
  JOIN_LOBBY <lobbyId> <playerId>               Join existing lobby

Game Management:
  START_GAME <playerIds...> <sb> <bb>  Start a new game
  GET_GAME_STATE <gameId>               View current game state
  GET_MY_CARDS <gameId> <playerId>      View your hole cards
  DEAL_FLOP <gameId>                    Deal flop cards
  DEAL_TURN <gameId>                    Deal turn card
  DEAL_RIVER <gameId>                   Deal river card
  DETERMINE_WINNER <gameId>             Determine winner

Player Actions:
  FOLD <gameId> <playerId>              Fold current hand
  CHECK <gameId> <playerId>             Check
  CALL <gameId> <playerId> <amount>     Call bet
  RAISE <gameId> <playerId> <amount>    Raise bet
  ALL_IN <gameId> <playerId>            Go all-in

Other:
  help                                  Show this help
  quick                                 Show quick command shortcuts
  info                                  Show current player info
  exit                                  Disconnect and quit

════════════════════════════════════════════════════════════════════════
""")
    
    def show_quick_commands(self):
        """Show quick command examples with current IDs"""
        print(f"""
╔══════════════════════════════════════════════════════════════════════╗
║                     QUICK COMMAND TEMPLATES                          ║
╚══════════════════════════════════════════════════════════════════════╝

Current Session:
  Player ID: {self.player_id or '(not registered)'}
  Game ID:   {self.game_id or '(no game)'}
  Lobby ID:  {self.lobby_id or '(no lobby)'}

Quick Commands (copy-paste ready):
""")
        
        if not self.player_id:
            print(f"  REGISTER {self.player_name} 1000")
        else:
            print(f"  GET_MY_CARDS {self.game_id or '<gameId>'} {self.player_id}")
            print(f"  GET_GAME_STATE {self.game_id or '<gameId>'}")
            print(f"  CHECK {self.game_id or '<gameId>'} {self.player_id}")
            print(f"  CALL {self.game_id or '<gameId>'} {self.player_id} <amount>")
            print(f"  RAISE {self.game_id or '<gameId>'} {self.player_id} <amount>")
            print(f"  FOLD {self.game_id or '<gameId>'} {self.player_id}")
        
        print("""
════════════════════════════════════════════════════════════════════════
""")
    
    def show_info(self):
        """Show current player information"""
        print(f"""
╔══════════════════════════════════════════════════════════════════════╗
║                      CURRENT SESSION INFO                            ║
╚══════════════════════════════════════════════════════════════════════╝

  Player Name: {self.player_name}
  Player ID:   {self.player_id or '(not registered)'}
  Game ID:     {self.game_id or '(no game)'}
  Lobby ID:    {self.lobby_id or '(no lobby)'}

════════════════════════════════════════════════════════════════════════
""")


async def main():
    # Get player name from command line or prompt
    if len(sys.argv) > 1:
        player_name = sys.argv[1]
    else:
        player_name = input("Enter your player name: ").strip() or "Player"
    
    # Create client
    client = PokerClient(player_name)
    
    # Connect to server
    if await client.connect():
        # Run interactive mode
        await client.interactive_mode()
    
    print("👋 Goodbye!")


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n👋 Goodbye!")
    except Exception as e:
        print(f"❌ Fatal error: {e}")
        sys.exit(1)
