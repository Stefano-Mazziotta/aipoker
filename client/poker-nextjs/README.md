# 🎰 AI Poker - Next.js Client

[![Next.js](https://img.shields.io/badge/Next.js-14-black.svg)](https://nextjs.org/)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue.svg)](https://www.typescriptlang.org/)
[![Tailwind](https://img.shields.io/badge/Tailwind-3-38bdf8.svg)](https://tailwindcss.com/)

A **mobile-first** Texas Hold'em poker client built with Next.js 14, featuring real-time WebSocket communication, responsive design, and smooth animations.

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [WebSocket Integration](#-websocket-integration)
- [Component Architecture](#-component-architecture)
- [State Management](#-state-management)
- [UI/UX Design](#-uiux-design)
- [Development Guide](#-development-guide)

---

## ✨ Features

### Real-Time Gameplay
- ✅ **WebSocket Connection** - Persistent bidirectional communication
- 🔄 **Live Game Updates** - Instant state synchronization
- 🎯 **Event-Driven UI** - React to server events in real-time
- ⚡ **Optimistic Updates** - Smooth user experience

### Game Features
- 🎲 **Full Texas Hold'em** - Complete poker gameplay
- 👥 **Multi-Player Support** - 2-9 players per table
- 💰 **Betting Actions** - CHECK, CALL, RAISE, FOLD, ALL_IN
- 🃏 **Visual Card Display** - SVG-based card rendering
- 🏆 **Winner Announcement** - Animated showdown results

### Responsive Design
- 📱 **Mobile-First** - Optimized for touchscreen devices
- 💻 **Desktop Support** - Adapts to larger screens
- 🎨 **Dark Theme** - Professional poker table aesthetic
- ✨ **Smooth Animations** - Tailwind transitions

### Developer Experience
- 🔒 **Type Safety** - Full TypeScript coverage
- 🧩 **Component Library** - Reusable UI components
- 🎯 **Custom Hooks** - Encapsulated logic
- 📝 **Strong Typing** - DTOs matching backend

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Next.js | 14 | React framework with App Router |
| React | 18 | UI library |
| TypeScript | 5 | Type safety |
| Tailwind CSS | 3 | Utility-first styling |
| Native WebSocket API | - | Real-time communication |
| Lucide React | Latest | Icon library |

---

## 📁 Project Structure

```
poker-nextjs/
├── src/
│   ├── app/                           # Next.js App Router
│   │   ├── layout.tsx                 # Root layout
│   │   ├── page.tsx                   # Home page
│   │   ├── lobby/
│   │   │   └── page.tsx               # Lobby page
│   │   └── game/
│   │       └── [id]/
│   │           └── page.tsx           # Game page
│   │
│   ├── components/                    # React Components
│   │   ├── game/                      # Game-specific components
│   │   │   ├── GameTable.tsx          # Main game table
│   │   │   ├── PlayerSeat.tsx         # Player position
│   │   │   ├── CommunityCards.tsx     # Shared cards
│   │   │   ├── ActionButtons.tsx      # Betting buttons
│   │   │   ├── PotDisplay.tsx         # Pot amount
│   │   │   └── WinnerDisplay.tsx      # Showdown results
│   │   │
│   │   ├── lobby/                     # Lobby components
│   │   │   ├── LobbyList.tsx          # Available lobbies
│   │   │   └── CreateLobbyForm.tsx    # New lobby form
│   │   │
│   │   └── ui/                        # Reusable UI components
│   │       ├── Button.tsx
│   │       ├── Card.tsx
│   │       ├── Input.tsx
│   │       └── Modal.tsx
│   │
│   ├── contexts/                      # React Contexts
│   │   ├── WebSocketContext.tsx       # WebSocket connection
│   │   ├── GameContext.tsx            # Game state management
│   │   └── PlayerContext.tsx          # Player session
│   │
│   ├── hooks/                         # Custom Hooks
│   │   ├── useWebSocket.ts            # WebSocket management
│   │   ├── useGame.ts                 # Game state access
│   │   └── usePlayer.ts               # Player data access
│   │
│   ├── lib/                           # Utilities & Types
│   │   ├── websocket/
│   │   │   ├── client.ts              # WebSocket client
│   │   │   └── events.ts              # Event type definitions
│   │   ├── game/
│   │   │   ├── card-parser.ts         # Card string to object
│   │   │   └── hand-evaluator.ts      # Client-side evaluation
│   │   └── utils/
│   │       ├── format.ts              # Number formatting
│   │       └── validation.ts          # Input validation
│   │
│   └── types/                         # TypeScript Types
│       ├── game.types.ts              # Game-related types
│       ├── player.types.ts            # Player types
│       └── websocket.types.ts         # WebSocket message types
│
├── public/                            # Static Assets
│   ├── cards/                         # Card SVG images
│   └── favicon.ico
│
├── tailwind.config.ts                 # Tailwind configuration
├── next.config.ts                     # Next.js configuration
├── tsconfig.json                      # TypeScript configuration
└── package.json                       # Dependencies
```

---

## 🚀 Getting Started

### Prerequisites

- **Node.js 18+** ([Download](https://nodejs.org/))
- **npm or yarn**
- **Backend server running** (see [root README](../../README.md))

### Installation

1. **Navigate to client directory**
   ```bash
   cd client/poker-nextjs
   ```

2. **Install dependencies**
   ```bash
   npm install
   # or
   yarn install
   ```

3. **Create environment variables**
   ```bash
   cp .env.example .env.local
   ```

   Edit `.env.local`:
   ```env
   NEXT_PUBLIC_WS_URL=ws://localhost:8025/poker
   ```

4. **Run development server**
   ```bash
   npm run dev
   # or
   yarn dev
   ```

5. **Open browser**
   ```
   http://localhost:3000
   ```

### Build for Production

```bash
npm run build
npm start
```

---

## 📡 WebSocket Integration

### Connection Flow

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   React      │────>│  WebSocket   │────>│   Backend    │
│  Component   │     │   Context    │     │    Server    │
└──────────────┘     └──────────────┘     └──────────────┘
      │                     │                     │
      │  1. useWebSocket()  │                     │
      │<────────────────────│                     │
      │                     │                     │
      │  2. connect()       │                     │
      │────────────────────>│  3. new WebSocket() │
      │                     │────────────────────>│
      │                     │                     │
      │                     │  4. onopen          │
      │  5. isConnected     │<────────────────────│
      │<────────────────────│                     │
      │                     │                     │
      │  6. sendCommand()   │                     │
      │────────────────────>│  7. send(JSON)      │
      │                     │────────────────────>│
      │                     │                     │
      │                     │  8. onmessage       │
      │  9. handleEvent()   │<────────────────────│
      │<────────────────────│                     │
```

### WebSocket Context

**Location:** `src/contexts/WebSocketContext.tsx`

```typescript
export const WebSocketProvider = ({ children }: { children: ReactNode }) => {
  const [socket, setSocket] = useState<WebSocket | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [messageQueue, setMessageQueue] = useState<string[]>([]);

  useEffect(() => {
    // Connect to WebSocket
    const ws = new WebSocket(process.env.NEXT_PUBLIC_WS_URL!);

    ws.onopen = () => {
      console.log('✅ WebSocket connected');
      setIsConnected(true);
      // Send queued messages
      messageQueue.forEach(msg => ws.send(msg));
      setMessageQueue([]);
    };

    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      handleIncomingMessage(message);
    };

    ws.onerror = (error) => {
      console.error('❌ WebSocket error:', error);
    };

    ws.onclose = () => {
      console.log('🔌 WebSocket disconnected');
      setIsConnected(false);
      // Attempt reconnection
      setTimeout(() => connectWebSocket(), 3000);
    };

    setSocket(ws);

    return () => ws.close();
  }, []);

  const sendCommand = useCallback((command: string, data: any) => {
    const message = JSON.stringify({ command, data });
    
    if (socket?.readyState === WebSocket.OPEN) {
      socket.send(message);
    } else {
      // Queue message for when connection opens
      setMessageQueue(prev => [...prev, message]);
    }
  }, [socket]);

  return (
    <WebSocketContext.Provider value={{ socket, isConnected, sendCommand }}>
      {children}
    </WebSocketContext.Provider>
  );
};
```

### Message Protocol

#### Outgoing Commands

```typescript
// Register player
sendCommand('REGISTER_PLAYER', {
  playerName: 'Alice',
  chips: 1000
});

// Start game
sendCommand('START_GAME', {
  playerIds: ['uuid1', 'uuid2'],
  smallBlind: 10,
  bigBlind: 20
});

// Player action
sendCommand('PLAYER_ACTION', {
  gameId: 'game-uuid',
  playerId: 'player-uuid',
  action: 'RAISE',
  amount: 50
});
```

#### Incoming Events

```typescript
// Event type definitions
export type GameEvent =
  | { type: 'GAME_STARTED'; data: GameStartedDTO }
  | { type: 'PLAYER_CARDS_DEALT'; data: PlayerCardsDealtDTO }
  | { type: 'PLAYER_ACTION'; data: PlayerActionDTO }
  | { type: 'GAME_STATE_CHANGED'; data: GameStateChangedDTO }
  | { type: 'WINNER_DETERMINED'; data: WinnerDeterminedDTO };

// Event handler
const handleIncomingMessage = (event: GameEvent) => {
  switch (event.type) {
    case 'GAME_STARTED':
      dispatch({ type: 'GAME_STARTED', payload: event.data });
      break;
    case 'PLAYER_CARDS_DEALT':
      dispatch({ type: 'SET_PLAYER_CARDS', payload: event.data.cards });
      break;
    case 'GAME_STATE_CHANGED':
      dispatch({ type: 'UPDATE_GAME_STATE', payload: event.data });
      break;
    // ... other cases
  }
};
```

---

## 🧩 Component Architecture

### Component Hierarchy

```
App
└── WebSocketProvider
    └── GameContext Provider
        └── PlayerContext Provider
            └── Page (Game)
                ├── GameTable
                │   ├── GameHeader
                │   │   ├── PotDisplay
                │   │   └── GameStateIndicator
                │   ├── CommunityCards
                │   └── PlayerSeats (9x)
                │       └── PlayerSeat
                │           ├── PlayerAvatar
                │           ├── PlayerName
                │           ├── ChipsDisplay
                │           └── Cards (2x)
                └── ActionButtons
                    ├── CheckButton
                    ├── CallButton
                    ├── RaiseButton
                    ├── FoldButton
                    └── AllInButton
```

### Key Components

#### GameTable Component

**Location:** `src/components/game/GameTable.tsx`

```typescript
export const GameTable = () => {
  const { gameState } = useGame();

  return (
    <div className="relative w-full h-screen bg-gradient-to-br from-green-900 to-green-950">
      {/* Game Header */}
      <div className="absolute top-0 left-0 right-0 p-4">
        <GameHeader />
      </div>

      {/* Poker Table */}
      <div className="absolute inset-0 flex items-center justify-center">
        <div className="relative w-[90vw] max-w-4xl aspect-[16/10]">
          {/* Table Surface */}
          <div className="absolute inset-0 bg-green-800 rounded-full border-8 border-amber-900 shadow-2xl">
            {/* Community Cards */}
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
              <CommunityCards cards={gameState.communityCards} />
            </div>

            {/* Pot Display */}
            <div className="absolute top-[35%] left-1/2 -translate-x-1/2">
              <PotDisplay amount={gameState.pot} />
            </div>
          </div>

          {/* Player Seats (positioned around table) */}
          {gameState.players.map((player, index) => (
            <PlayerSeat
              key={player.id}
              player={player}
              position={calculateSeatPosition(index, gameState.players.length)}
              isCurrentTurn={gameState.currentPlayerId === player.id}
            />
          ))}
        </div>
      </div>

      {/* Action Buttons (bottom) */}
      <div className="absolute bottom-0 left-0 right-0 p-4">
        <ActionButtons />
      </div>
    </div>
  );
};
```

#### ActionButtons Component

**Location:** `src/components/game/ActionButtons.tsx`

```typescript
export const ActionButtons = () => {
  const { gameState, isMyTurn } = useGame();
  const { sendCommand } = useWebSocket();

  const handleAction = (action: Action, amount?: number) => {
    sendCommand('PLAYER_ACTION', {
      gameId: gameState.id,
      playerId: currentPlayer.id,
      action,
      amount
    });
  };

  if (!isMyTurn) return null;

  return (
    <div className="grid grid-cols-2 gap-3 max-w-md mx-auto">
      {/* Row 1 */}
      <Button
        variant="secondary"
        onClick={() => handleAction('CHECK')}
        disabled={gameState.currentBet > 0}
      >
        CHECK
      </Button>
      <Button
        variant="primary"
        onClick={() => handleAction('CALL')}
        disabled={gameState.currentBet === 0}
      >
        CALL ${gameState.currentBet}
      </Button>

      {/* Row 2 */}
      <Button
        variant="accent"
        onClick={() => handleAction('RAISE', raiseAmount)}
      >
        RAISE
      </Button>
      <Button
        variant="danger"
        onClick={() => handleAction('FOLD')}
      >
        FOLD
      </Button>

      {/* Row 3 (full width) */}
      <Button
        variant="warning"
        className="col-span-2"
        onClick={() => handleAction('ALL_IN')}
      >
        ALL IN
      </Button>
    </div>
  );
};
```

#### PlayerSeat Component

**Location:** `src/components/game/PlayerSeat.tsx`

```typescript
interface PlayerSeatProps {
  player: Player;
  position: Position;
  isCurrentTurn: boolean;
}

export const PlayerSeat = ({ player, position, isCurrentTurn }: PlayerSeatProps) => {
  return (
    <div
      className={`absolute ${position.classes}`}
      style={{ 
        top: position.top, 
        left: position.left,
        transform: position.transform 
      }}
    >
      {/* Player Container */}
      <div className={`
        relative bg-gray-900 rounded-lg p-3 min-w-[120px]
        ${isCurrentTurn ? 'ring-4 ring-yellow-400' : ''}
        transition-all duration-300
      `}>
        {/* Player Name */}
        <div className="text-white font-semibold text-sm mb-2">
          {player.name}
        </div>

        {/* Chips */}
        <div className="text-yellow-400 text-xs mb-2">
          ${player.chips}
        </div>

        {/* Cards */}
        <div className="flex gap-1">
          {player.cards?.map((card, i) => (
            <Card key={i} card={card} />
          ))}
        </div>

        {/* Status Badge */}
        {player.status === 'FOLDED' && (
          <div className="absolute inset-0 bg-black/60 rounded-lg flex items-center justify-center">
            <span className="text-red-400 font-bold">FOLDED</span>
          </div>
        )}
      </div>
    </div>
  );
};
```

---

## 🗂️ State Management

### Game Context

**Location:** `src/contexts/GameContext.tsx`

```typescript
interface GameState {
  id: string;
  players: Player[];
  communityCards: Card[];
  pot: number;
  currentBet: number;
  currentPlayerId: string;
  state: 'PRE_FLOP' | 'FLOP' | 'TURN' | 'RIVER' | 'SHOWDOWN';
}

const gameReducer = (state: GameState, action: GameAction): GameState => {
  switch (action.type) {
    case 'GAME_STARTED':
      return {
        ...state,
        id: action.payload.gameId,
        players: action.payload.players,
        state: 'PRE_FLOP'
      };

    case 'PLAYER_ACTION':
      return {
        ...state,
        pot: action.payload.newPot,
        players: updatePlayerChips(state.players, action.payload)
      };

    case 'GAME_STATE_CHANGED':
      return {
        ...state,
        state: action.payload.newState,
        communityCards: action.payload.communityCards || state.communityCards,
        currentPlayerId: action.payload.currentPlayerId
      };

    default:
      return state;
  }
};

export const GameContextProvider = ({ children }: { children: ReactNode }) => {
  const [state, dispatch] = useReducer(gameReducer, initialState);
  
  return (
    <GameContext.Provider value={{ gameState: state, dispatch }}>
      {children}
    </GameContext.Provider>
  );
};
```

### Custom Hooks

#### useGame Hook

```typescript
export const useGame = () => {
  const context = useContext(GameContext);
  const { currentPlayer } = usePlayer();

  if (!context) throw new Error('useGame must be used within GameContextProvider');

  const isMyTurn = context.gameState.currentPlayerId === currentPlayer?.id;

  return {
    ...context,
    isMyTurn
  };
};
```

---

## 🎨 UI/UX Design

### Design Principles

1. **Mobile-First:** Touch-friendly buttons, optimized spacing
2. **Visual Hierarchy:** Important actions prominent
3. **Real-Time Feedback:** Instant visual updates
4. **Clear Affordances:** Button states clearly indicate availability
5. **Professional Aesthetic:** Dark poker table theme

### Responsive Layout

```typescript
// Seat positions adapt to screen size
const calculateSeatPosition = (index: number, totalPlayers: number) => {
  const angle = (360 / totalPlayers) * index;
  const radius = window.innerWidth < 768 ? '40vw' : '45%';
  
  return {
    top: `${50 + Math.sin(angle * Math.PI / 180) * parseFloat(radius)}%`,
    left: `${50 + Math.cos(angle * Math.PI / 180) * parseFloat(radius)}%`,
    transform: 'translate(-50%, -50%)'
  };
};
```

### Animations

```css
/* Card deal animation */
@keyframes dealCard {
  from {
    opacity: 0;
    transform: translateY(-100px) rotate(-45deg);
  }
  to {
    opacity: 1;
    transform: translateY(0) rotate(0);
  }
}

/* Turn indicator pulse */
@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(250, 204, 21, 0.7);
  }
  50% {
    box-shadow: 0 0 0 20px rgba(250, 204, 21, 0);
  }
}
```

---

## 👨‍💻 Development Guide

### Code Style

- **TypeScript Strict Mode:** Enabled
- **ESLint:** Enforced
- **Prettier:** Consistent formatting
- **Component Structure:** Function components with hooks
- **Naming Conventions:**
  - Components: PascalCase
  - Hooks: camelCase with `use` prefix
  - Types: PascalCase with `Type` or `DTO` suffix

### Testing

```bash
# Run type checking
npm run type-check

# Run linter
npm run lint

# Run tests (when implemented)
npm test
```

### Environment Variables

```env
# .env.local
NEXT_PUBLIC_WS_URL=ws://localhost:8025/poker
```

### Debugging WebSocket

```typescript
// Enable debug logging
const DEBUG = process.env.NODE_ENV === 'development';

ws.onmessage = (event) => {
  if (DEBUG) {
    console.log('📥 Received:', JSON.parse(event.data));
  }
  handleIncomingMessage(JSON.parse(event.data));
};
```

---

## 🤝 Contributing

See [root README](../../README.md) for contribution guidelines.

---

## 📚 Further Reading

- [Backend README](../../README.md) - Server architecture
- [Backend ARCHITECTURE.md](../../ARCHITECTURE.md) - Detailed backend design
- [Next.js Documentation](https://nextjs.org/docs)
- [WebSocket API](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)

---

<div align="center">
  <strong>Built with ❤️ for an immersive poker experience</strong>
</div>
