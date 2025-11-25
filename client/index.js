// Game State
let ws = null;
let playerId = null;
let playerName = null;
let gameId = null;
let lobbyId = null;
let isLobbyAdmin = false;
let playerChips = 0;
let currentPot = 0;
let currentBet = 0;
let currentRound = 'pre-flop';
let registeredPlayers = [];
let lobbyPlayers = [];

// Card Symbols
const suitSymbols = {
    'HEARTS': '♥️',
    'DIAMONDS': '♦️',
    'CLUBS': '♣️',
    'SPADES': '♠️'
};

// Connect to WebSocket
function connect() {
    const url = document.getElementById('wsUrl').value;
    addMessage('info', `Connecting to ${url}...`);
    
    try {
        ws = new WebSocket(url);
        
        ws.onopen = () => {
            updateConnectionStatus(true);
            addMessage('event', 'Connected to server! 🎉');
            document.getElementById('btnRegister').disabled = false;
            document.getElementById('btnConnect').textContent = 'DISCONNECT';
            document.getElementById('btnConnect').onclick = disconnect;
        };
        
        ws.onmessage = (event) => {
            handleMessage(event.data);
        };
        
        ws.onerror = (error) => {
            addMessage('error', 'WebSocket error occurred');
            console.error('WebSocket error:', error);
        };
        
        ws.onclose = () => {
            updateConnectionStatus(false);
            addMessage('info', 'Disconnected from server');
            document.getElementById('btnRegister').disabled = true;
            document.getElementById('btnStart').disabled = true;
            document.getElementById('btnConnect').textContent = 'CONNECT';
            document.getElementById('btnConnect').onclick = connect;
        };
        
    } catch (error) {
        addMessage('error', `Connection failed: ${error.message}`);
    }
}

// Disconnect
function disconnect() {
    if (ws) {
        ws.close();
        ws = null;
    }
}

// Send Command
function sendCommand(command) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        const message = JSON.stringify({ command: command });
        ws.send(message);
        addMessage('info', `Sent: ${command}`);
    } else {
        addMessage('error', 'Not connected to server');
    }
}

// Handle Incoming Messages
function handleMessage(data) {
    try {
        const message = JSON.parse(data);
        const type = message.type || message.eventType;

        switch(type) {
            case 'PLAYER_REGISTERED':
                handlePlayerRegistered(message);
                break;
            case 'LOBBY_CREATED':
                handleLobbyCreated(message);
                break;
            case 'LOBBY_JOINED':
                handleLobbyJoined(message);
                break;
            case 'PLAYER_JOINED_LOBBY':
                handlePlayerJoinedLobby(message);
                break;
            case 'GAME_STARTED':
                handleGameStarted(message);
                break;
            case 'GAME_STATE_CHANGED':
                handleGameStateChanged(message);
                break;
            case 'CARDS_DEALT':
                handleCardsDealt(message);
                break;
            case 'PLAYER_ACTION':
                handlePlayerAction(message);
                break;
            case 'WINNER_DETERMINED':
                handleWinnerDetermined(message);
                break;
            case 'PLAYER_CARDS':
                handlePlayerCards(message);
                break;
            case 'GAME_STATE':
                handleGameState(message);
                break;
            case 'FLOP_DEALT':
            case 'TURN_DEALT':
            case 'RIVER_DEALT':
                handleCommunityCards(message);
                break;
            case 'WINNER_DETERMINED':
                handleWinner(message);
                break;
            case 'ERROR':
                addMessage('error', message.message || message.content || 'An error occurred');
                break;
            case 'INFO':
                addMessage('info', message.message || message.content);
                break;
            case 'success':
                addMessage('info', message.content);
                break;
            case 'response':
                handleTextResponse(message.content);
                break;
            default:
                addMessage('event', JSON.stringify(message, null, 2));
        }
    } catch (e) {
        // Plain text message - parse it
        handleTextResponse(data);
    }
}

// Handle Text Response (from server's MessageFormatter)
function handleTextResponse(content) {
    addMessage('info', content);
    
    // Parse player registration
    if (content.includes('SUCCESS: Player registered')) {
        const idMatch = content.match(/ID:\s*([a-f0-9-]+)/);
        const nameMatch = content.match(/Name:\s*(\w+)/);
        const chipsMatch = content.match(/Chips:\s*(\d+)/);
        
        if (idMatch && nameMatch && chipsMatch) {
            playerId = idMatch[1];
            playerName = nameMatch[1];
            playerChips = parseInt(chipsMatch[1]);
            
            // Show player ID field
            document.getElementById('playerIdDisplay').style.display = 'block';
            document.getElementById('playerIdText').value = playerId;
            
            // Enable lobby buttons
            document.getElementById('btnCreateLobby').disabled = false;
            document.getElementById('btnJoinLobby').disabled = false;
            document.getElementById('btnRegister').disabled = true;
            document.getElementById('btnRegister').textContent = 'REGISTERED ✓';
            
            registeredPlayers.push({ id: playerId, name: playerName, chips: playerChips });
            updatePlayersList();
        }
    }
    
    // Parse lobby creation
    if (content.includes('SUCCESS: Lobby created')) {
        const idMatch = content.match(/Lobby ID:\s*([a-f0-9-]+)/);
        if (idMatch) {
            lobbyId = idMatch[1];
            isLobbyAdmin = true;
            document.getElementById('lobbyIdDisplay').style.display = 'block';
            document.getElementById('lobbyIdText').value = lobbyId;
            document.getElementById('btnStart').disabled = false;
            document.getElementById('btnCreateLobby').disabled = true;
            document.getElementById('btnJoinLobby').disabled = true;
            addMessage('event', '🏠 You are the lobby admin - You can START the game');
            
            // Initialize lobby players with yourself
            lobbyPlayers = [{ id: playerId, name: playerName, chips: playerChips }];
            updateLobbyPlayersList();
            
            // Subscribe to lobby events for real-time updates
            sendCommand(`SUBSCRIBE_LOBBY ${lobbyId} ${playerId}`);
        }
    }
    
    // Parse lobby join
    if (content.includes('SUCCESS: Joined lobby')) {
        const idMatch = content.match(/Lobby ID:\s*([a-f0-9-]+)/);
        if (idMatch) {
            lobbyId = idMatch[1];
            document.getElementById('lobbyIdDisplay').style.display = 'block';
            document.getElementById('lobbyIdText').value = lobbyId;
            document.getElementById('btnCreateLobby').disabled = true;
            document.getElementById('btnJoinLobby').disabled = true;
            addMessage('event', '🏠 Joined lobby - Waiting for admin to start...');
            
            // Initialize lobby players with yourself
            lobbyPlayers = [{ id: playerId, name: playerName, chips: playerChips }];
            updateLobbyPlayersList();
            
            // Subscribe to lobby events for real-time updates
            sendCommand(`SUBSCRIBE_LOBBY ${lobbyId} ${playerId}`);
        }
    }
    
    // Parse game start
    if (content.includes('SUCCESS: Game started')) {
        const idMatch = content.match(/Game ID:\s*([a-f0-9-]+)/);
        if (idMatch) {
            gameId = idMatch[1];
            document.getElementById('gameId').textContent = gameId.substring(0, 8) + '...';
            addMessage('event', '🎮 Game started!');
            
            // Request initial state
            setTimeout(() => {
                sendCommand(`GET_GAME_STATE ${gameId}`);
                sendCommand(`GET_MY_CARDS ${gameId} ${playerId}`);
            }, 500);
        }
    }
}

// Handle Lobby Created
function handleLobbyCreated(message) {
    // Handled in handleTextResponse
}

// Handle Lobby Joined  
function handleLobbyJoined(message) {
    // Handled in handleTextResponse
}

// Handle Player Joined Lobby (Event from WebSocket)
function handlePlayerJoinedLobby(message) {
    const data = message.data || message;
    const joinedPlayerId = data.playerId;
    const joinedPlayerName = data.playerName || 'Unknown';
    const currentCount = data.currentPlayerCount;
    const maxPlayers = data.maxPlayers;
    
    addMessage('event', `👤 ${joinedPlayerName} joined lobby (${currentCount}/${maxPlayers} players)`);
    
    // Add player to lobby players list if not already present
    if (lobbyId === data.lobbyId) {
        const existingPlayer = lobbyPlayers.find(p => p.id === joinedPlayerId);
        if (!existingPlayer) {
            lobbyPlayers.push({
                id: joinedPlayerId,
                name: joinedPlayerName,
                chips: 1000 // Default value, actual value will be from game
            });
            updateLobbyPlayersList();
        }
    }
}

// Handle other domain events
function handleGameStateChanged(message) {
    const data = message.data || message;
    addMessage('event', `🔄 Game state: ${data.newState}`);
    if (gameId) {
        sendCommand(`GET_GAME_STATE ${gameId}`);
    }
}

function handleCardsDealt(message) {
    const data = message.data || message;
    addMessage('event', `🃏 Cards dealt`);
    if (gameId) {
        sendCommand(`GET_MY_CARDS ${gameId} ${playerId}`);
    }
}

function handlePlayerAction(message) {
    const data = message.data || message;
    addMessage('event', `🎲 Player action: ${data.action}`);
    if (gameId) {
        sendCommand(`GET_GAME_STATE ${gameId}`);
    }
}

function handleWinnerDetermined(message) {
    const data = message.data || message;
    addMessage('success', `🏆 Winner: ${data.winnerName} with ${data.handRank}`);
    if (gameId) {
        sendCommand(`GET_GAME_STATE ${gameId}`);
    }
}

// Update lobby player count display
function updateLobbyPlayerCount(current, max) {
    // Could add a UI element to show this
    console.log(`Lobby players: ${current}/${max}`);
}

// Handle Player Registered
function handlePlayerRegistered(message) {
    // Handled in handleTextResponse - kept for JSON compatibility
    playerId = message.playerId;
    playerName = message.name;
    playerChips = message.chips;
    
    document.getElementById('playerIdDisplay').style.display = 'block';
    document.getElementById('playerIdText').value = playerId;
    document.getElementById('btnCreateLobby').disabled = false;
    document.getElementById('btnJoinLobby').disabled = false;
    document.getElementById('btnRegister').disabled = true;
    document.getElementById('btnRegister').textContent = 'REGISTERED ✓';
    
    registeredPlayers.push({ id: playerId, name: playerName, chips: playerChips });
    updatePlayersList();
}

// Handle Game Started
function handleGameStarted(message) {
    gameId = message.gameId;
    document.getElementById('gameId').textContent = gameId.substring(0, 8) + '...';
    addMessage('event', `🎮 Game started! Game ID: ${gameId}`);
    
    // Request initial game state and cards
    setTimeout(() => {
        sendCommand(`GET_GAME_STATE ${gameId}`);
        sendCommand(`GET_MY_CARDS ${gameId} ${playerId}`);
    }, 500);
}

// Handle Player Cards
function handlePlayerCards(message) {
    const cards = message.cards || [];
    displayPlayerCards(cards);
    addMessage('event', `🃏 Received your cards`);
}

// Handle Game State
function handleGameState(message) {
    currentPot = message.pot || 0;
    currentBet = message.currentBet || 0;
    
    document.getElementById('potAmount').textContent = `$${currentPot}`;
    document.getElementById('currentBet').textContent = `$${currentBet}`;
    
    if (message.communityCards) {
        displayCommunityCards(message.communityCards);
    }
}

// Handle Community Cards
function handleCommunityCards(message) {
    const cards = message.communityCards || message.cards || [];
    displayCommunityCards(cards);
    
    const round = message.type.replace('_DEALT', '').toLowerCase();
    currentRound = round;
    document.getElementById('currentRound').textContent = round.toUpperCase();
    addMessage('event', `🎴 ${round.toUpperCase()} dealt`);
    
    // Update game state
    setTimeout(() => {
        sendCommand(`GET_GAME_STATE ${gameId}`);
    }, 300);
}

// Handle Winner
function handleWinner(message) {
    const winner = message.winner || message.winnerName;
    const hand = message.hand || message.handRank;
    const amount = message.amount || message.potWon;
    
    addMessage('event', `🏆 Winner: ${winner} with ${hand} - Won $${amount}`);
    
    // Reset table
    setTimeout(() => {
        resetTable();
    }, 3000);
}

// Register Player
function registerPlayer() {
    const name = document.getElementById('playerName').value.trim();
    const chips = document.getElementById('startingChips').value;
    
    if (!name) {
        addMessage('error', 'Please enter your name');
        return;
    }
    
    sendCommand(`REGISTER ${name} ${chips}`);
}

// Create Lobby
function createLobby() {
    if (!playerId) {
        addMessage('error', 'Please register first');
        return;
    }
    
    const lobbyName = prompt('Enter lobby name:', `${playerName}'s Game`);
    if (!lobbyName) return;
    
    const maxPlayers = prompt('Maximum number of players (2-10):', '6');
    if (!maxPlayers) return;
    
    sendCommand(`CREATE_LOBBY ${lobbyName} ${maxPlayers} ${playerId}`);
}

// Join Lobby
function joinLobby() {
    if (!playerId) {
        addMessage('error', 'Please register first');
        return;
    }
    
    const lobbyIdToJoin = prompt('Enter Lobby ID to join:');
    if (!lobbyIdToJoin) return;
    
    sendCommand(`JOIN_LOBBY ${lobbyIdToJoin} ${playerId}`);
}

// Start Game (Admin only)
function startGame() {
    if (!playerId) {
        addMessage('error', 'Please register first');
        return;
    }
    
    if (!lobbyId) {
        addMessage('error', 'Please create or join a lobby first');
        return;
    }
    
    if (!isLobbyAdmin) {
        addMessage('error', 'Only the lobby admin can start the game');
        return;
    }
    
    // Get player IDs from lobby context
    if (lobbyPlayers.length < 2) {
        addMessage('error', 'Need at least 2 players to start a game. Current players: ' + lobbyPlayers.length);
        return;
    }
    
    const playerIds = lobbyPlayers.map(p => p.id);
    addMessage('info', `Starting game with ${playerIds.length} players: ${lobbyPlayers.map(p => p.name).join(', ')}`);
    
    const smallBlind = prompt('Small Blind:', '10');
    const bigBlind = prompt('Big Blind:', '20');
    
    if (!smallBlind || !bigBlind) return;
    
    const command = `START_GAME ${playerIds.join(' ')} ${smallBlind} ${bigBlind}`;
    sendCommand(command);
}

// Copy Player ID
function copyPlayerId() {
    const input = document.getElementById('playerIdText');
    input.select();
    document.execCommand('copy');
    addMessage('info', '📋 Player ID copied to clipboard!');
}

// Copy Lobby ID
function copyLobbyId() {
    const input = document.getElementById('lobbyIdText');
    input.select();
    document.execCommand('copy');
    addMessage('info', '📋 Lobby ID copied to clipboard!');
}

// Perform Action
function performAction(action) {
    if (!gameId || !playerId) {
        addMessage('error', 'No active game');
        return;
    }
    
    let command = '';
    const betAmount = document.getElementById('betAmount').value;
    
    switch(action) {
        case 'CHECK':
            command = `CHECK ${gameId} ${playerId}`;
            break;
        case 'CALL':
            command = `CALL ${gameId} ${playerId} ${currentBet}`;
            break;
        case 'RAISE':
            command = `RAISE ${gameId} ${playerId} ${betAmount}`;
            break;
        case 'FOLD':
            command = `FOLD ${gameId} ${playerId}`;
            break;
        case 'ALL_IN':
            command = `ALL_IN ${gameId} ${playerId}`;
            break;
    }
    
    sendCommand(command);
}

// Deal Next Round
function dealNext() {
    if (!gameId) {
        addMessage('error', 'No active game');
        return;
    }
    
    let command = '';
    switch(currentRound) {
        case 'pre-flop':
            command = `DEAL_FLOP ${gameId}`;
            break;
        case 'flop':
            command = `DEAL_TURN ${gameId}`;
            break;
        case 'turn':
            command = `DEAL_RIVER ${gameId}`;
            break;
        case 'river':
            command = `DETERMINE_WINNER ${gameId}`;
            break;
    }
    
    if (command) {
        sendCommand(command);
    }
}

// Quick Bet
function quickBet(type) {
    let amount = 0;
    switch(type) {
        case 'pot':
            amount = currentPot;
            break;
        case 'half':
            amount = Math.floor(currentPot / 2);
            break;
    }
    document.getElementById('betAmount').value = amount;
}

// Display Player Cards
function displayPlayerCards(cards) {
    const container = document.getElementById('playerHand');
    container.innerHTML = '';
    
    cards.forEach(cardStr => {
        const card = parseCard(cardStr);
        const cardEl = createCardElement(card);
        container.appendChild(cardEl);
    });
}

// Display Community Cards
function displayCommunityCards(cards) {
    const container = document.getElementById('communityCards');
    container.innerHTML = '';
    
    for (let i = 0; i < 5; i++) {
        if (i < cards.length) {
            const card = parseCard(cards[i]);
            const cardEl = createCardElement(card);
            container.appendChild(cardEl);
        } else {
            const emptyCard = document.createElement('div');
            emptyCard.className = 'card empty';
            container.appendChild(emptyCard);
        }
    }
}

// Parse Card String
function parseCard(cardStr) {
    // Format: "ACE of HEARTS" or "10 of DIAMONDS"
    const parts = cardStr.split(' of ');
    return {
        rank: parts[0],
        suit: parts[1]
    };
}

// Create Card Element
function createCardElement(card) {
    const cardEl = document.createElement('div');
    const isRed = card.suit === 'HEARTS' || card.suit === 'DIAMONDS';
    cardEl.className = `card ${isRed ? 'red' : 'black'}`;
    
    const suitSymbol = suitSymbols[card.suit] || card.suit;
    
    cardEl.innerHTML = `
        <div class="card-rank">${card.rank}</div>
        <div class="card-suit">${suitSymbol}</div>
    `;
    
    return cardEl;
}

// Update Players List
function updatePlayersList() {
    const container = document.getElementById('playersList');
    container.innerHTML = '';
    
    if (registeredPlayers.length === 0) {
        container.innerHTML = '<div class="player-item"><span class="player-name">No players yet</span></div>';
        return;
    }
    
    registeredPlayers.forEach(player => {
        const playerEl = document.createElement('div');
        playerEl.className = 'player-item';
        if (player.id === playerId) {
            playerEl.classList.add('active');
        }
        
        playerEl.innerHTML = `
            <span class="player-name">${player.name}</span>
            <span class="player-chips">$${player.chips}</span>
        `;
        
        container.appendChild(playerEl);
    });
}

// Update Lobby Players List (real-time lobby members)
function updateLobbyPlayersList() {
    const container = document.getElementById('playersList');
    
    // Only show lobby players when in a lobby (not in game)
    if (!lobbyId || gameId) {
        return; // Don't update if not in lobby or if game has started
    }
    
    container.innerHTML = '<div style="color: #ffd700; font-weight: bold; margin-bottom: 10px;">🏠 Lobby Players</div>';
    
    if (lobbyPlayers.length === 0) {
        container.innerHTML += '<div class="player-item"><span class="player-name">No players yet</span></div>';
        return;
    }
    
    lobbyPlayers.forEach(player => {
        const playerEl = document.createElement('div');
        playerEl.className = 'player-item';
        if (player.id === playerId) {
            playerEl.classList.add('active');
        }
        
        const isAdmin = isLobbyAdmin && player.id === playerId;
        playerEl.innerHTML = `
            <span class="player-name">${player.name} ${isAdmin ? '👑' : ''}</span>
            <span class="player-chips">Ready</span>
        `;
        
        container.appendChild(playerEl);
    });
}

// Reset Table
function resetTable() {
    currentRound = 'pre-flop';
    document.getElementById('currentRound').textContent = '-';
    document.getElementById('communityCards').innerHTML = `
        <div class="card empty"></div>
        <div class="card empty"></div>
        <div class="card empty"></div>
        <div class="card empty"></div>
        <div class="card empty"></div>
    `;
}

// Update Connection Status
function updateConnectionStatus(connected) {
    const indicator = document.getElementById('statusIndicator');
    const text = document.getElementById('statusText');
    
    if (connected) {
        indicator.classList.add('connected');
        text.textContent = 'Connected';
    } else {
        indicator.classList.remove('connected');
        text.textContent = 'Disconnected';
    }
}

// Add Message to Log
function addMessage(type, message) {
    const container = document.getElementById('messages');
    const messageEl = document.createElement('div');
    messageEl.className = `message ${type}`;
    
    const time = new Date().toLocaleTimeString();
    messageEl.innerHTML = `<span class="message-time">${time}</span>${message}`;
    
    container.appendChild(messageEl);
    container.scrollTop = container.scrollHeight;
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    addMessage('info', 'Welcome to Texas Hold\'em Poker! 🎰');
    addMessage('info', 'Click CONNECT to start playing');
});
