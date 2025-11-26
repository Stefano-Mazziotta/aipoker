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
let maxLobbyPlayers = 9; // Maximum players in lobby
let isPlayerTurn = false;
let isGameActive = false;

// Game State Enum
const GameState = {
    DISCONNECTED: 'DISCONNECTED',
    CONNECTED: 'CONNECTED',
    REGISTERED: 'REGISTERED',
    IN_LOBBY: 'IN_LOBBY',
    IN_GAME: 'IN_GAME'
};

let currentGameState = GameState.DISCONNECTED;

// Card Symbols
const suitSymbols = {
    'HEARTS': '♥️',
    'DIAMONDS': '♦️',
    'CLUBS': '♣️',
    'SPADES': '♠️'
};

// Player seats (9 maximum around the table)
let playerSeats = new Array(9).fill(null);

// Screen Management Functions
function showOnboardingScreen() {
    document.getElementById('onboardingScreen').classList.add('active');
    document.getElementById('gameScreen').classList.remove('active');
}

function showGameScreen() {
    document.getElementById('onboardingScreen').classList.remove('active');
    document.getElementById('gameScreen').classList.add('active');
}

// Update Player Seats around the table
function updatePlayerSeats(players) {
    // Clear all seats first
    playerSeats.fill(null);
    
    // Assign players to seats
    players.forEach((player, index) => {
        if (index < 9) {
            playerSeats[index] = player;
        }
    });
    
    // Render all seats
    for (let i = 0; i < 9; i++) {
        const seatElement = document.getElementById(`seat-${i}`);
        if (!seatElement) continue;
        
        const player = playerSeats[i];
        
        if (player) {
            // Show player
            seatElement.style.display = 'block';
            
            const nameElement = seatElement.querySelector('.player-name-display');
            const chipsElement = seatElement.querySelector('.player-chips-display');
            
            if (nameElement) {
                nameElement.textContent = player.name || `Player ${player.id}`;
            }
            if (chipsElement) {
                chipsElement.textContent = `$${player.chips || 0}`;
            }
            
            // Highlight if it's your seat
            if (player.id === playerId) {
                seatElement.classList.add('you');
            } else {
                seatElement.classList.remove('you');
            }
            
            // Check if player is active
            if (player.isActive) {
                seatElement.classList.add('active');
                seatElement.classList.remove('folded');
            } else if (player.folded) {
                seatElement.classList.add('folded');
                seatElement.classList.remove('active');
            } else {
                seatElement.classList.remove('active', 'folded');
            }
        } else {
            // Hide empty seat
            seatElement.style.display = 'none';
        }
    }
}

// Update Player Action Indicator
function updatePlayerAction(playerId, action, amount = null) {
    // Find the seat for this player
    const seatIndex = playerSeats.findIndex(p => p && p.id === playerId);
    if (seatIndex === -1) return;
    
    const seatElement = document.getElementById(`seat-${seatIndex}`);
    if (!seatElement) return;
    
    const actionIndicator = seatElement.querySelector('.player-action-indicator');
    if (!actionIndicator) return;
    
    // Set action text
    let actionText = action.toUpperCase();
    if (amount) {
        actionText += ` $${amount}`;
    }
    actionIndicator.textContent = actionText;
    
    // Show with animation
    actionIndicator.classList.add('show');
    
    // Hide after 3 seconds
    setTimeout(() => {
        actionIndicator.classList.remove('show');
    }, 3000);
}

// Update Game Display (pot, community cards, etc.)
function updateGameDisplay(gameState) {
    // Update pot (try both IDs for compatibility)
    if (gameState.pot !== undefined) {
        const potElement = document.getElementById('potAmountGame') || document.getElementById('potAmount');
        if (potElement) {
            potElement.textContent = `$${gameState.pot}`;
        }
        currentPot = gameState.pot;
    }
    
    // Update current bet
    if (gameState.currentBet !== undefined) {
        const betElement = document.getElementById('currentBetGame') || document.getElementById('currentBetAmount');
        if (betElement) {
            betElement.textContent = `$${gameState.currentBet}`;
        }
        currentBet = gameState.currentBet;
    }
    
    // Update community cards
    if (gameState.communityCards) {
        const communityCardsContainer = document.getElementById('communityCardsGame') || 
                                       document.getElementById('communityCardsCenter') || 
                                       document.getElementById('communityCards');
        if (communityCardsContainer) {
            communityCardsContainer.innerHTML = '';
            
            gameState.communityCards.forEach(card => {
                communityCardsContainer.appendChild(createCardElement(card));
            });
            
            // Add empty placeholders if less than 5
            for (let i = gameState.communityCards.length; i < 5; i++) {
                communityCardsContainer.appendChild(createCardElement(null));
            }
        }
    }
    
    // Update round phase
    if (gameState.round) {
        currentRound = gameState.round;
    }
}

// Update UI Button States based on Game State
function updateButtonStates() {
    // Connection buttons (may not exist in auto-connect mode)
    const btnConnect = document.getElementById('btnConnect');
    const btnRegister = document.getElementById('btnRegister');
    const btnCreateLobby = document.getElementById('btnCreateLobby');
    const btnJoinLobby = document.getElementById('btnJoinLobby');
    const btnStart = document.getElementById('btnStart');
    
    // Game action buttons
    const btnCheck = document.getElementById('btnCheck');
    const btnCall = document.getElementById('btnCall');
    const btnRaise = document.getElementById('btnRaise');
    const btnFold = document.getElementById('btnFold');
    const btnAllIn = document.getElementById('btnAllIn');
    const btnDeal = document.getElementById('btnDeal');
    
    // Input fields
    const wsUrlInput = document.getElementById('wsUrl');
    const playerNameInput = document.getElementById('playerName');
    const startingChipsInput = document.getElementById('startingChips');
    const betAmountInput = document.getElementById('betAmount');

    switch(currentGameState) {
        case GameState.DISCONNECTED:
            // Only connect button enabled (if it exists)
            if (btnConnect) btnConnect.disabled = false;
            if (btnRegister) btnRegister.disabled = true;
            if (btnCreateLobby) btnCreateLobby.disabled = true;
            if (btnJoinLobby) btnJoinLobby.disabled = true;
            if (btnStart) btnStart.disabled = true;
            
            // All game actions disabled
            if (btnCheck) btnCheck.disabled = true;
            if (btnCall) btnCall.disabled = true;
            if (btnRaise) btnRaise.disabled = true;
            if (btnFold) btnFold.disabled = true;
            if (btnAllIn) btnAllIn.disabled = true;
            if (btnDeal) btnDeal.disabled = true;
            
            // Enable inputs
            if (wsUrlInput) wsUrlInput.disabled = false;
            if (playerNameInput) playerNameInput.disabled = true;
            if (startingChipsInput) startingChipsInput.disabled = true;
            if (betAmountInput) betAmountInput.disabled = true;
            break;

        case GameState.CONNECTED:
            // Can disconnect or register
            if (btnConnect) btnConnect.disabled = false;
            if (btnRegister) btnRegister.disabled = false;
            if (btnCreateLobby) btnCreateLobby.disabled = true;
            if (btnJoinLobby) btnJoinLobby.disabled = true;
            if (btnStart) btnStart.disabled = true;
            
            // All game actions disabled
            if (btnCheck) btnCheck.disabled = true;
            if (btnCall) btnCall.disabled = true;
            if (btnRaise) btnRaise.disabled = true;
            if (btnFold) btnFold.disabled = true;
            if (btnAllIn) btnAllIn.disabled = true;
            if (btnDeal) btnDeal.disabled = true;
            
            // Enable registration inputs
            if (wsUrlInput) wsUrlInput.disabled = true;
            if (playerNameInput) playerNameInput.disabled = false;
            if (startingChipsInput) startingChipsInput.disabled = false;
            if (betAmountInput) betAmountInput.disabled = true;
            break;

        case GameState.REGISTERED:
            // Can create or join lobby
            if (btnConnect) btnConnect.disabled = false;
            if (btnRegister) btnRegister.disabled = true;
            if (btnCreateLobby) btnCreateLobby.disabled = false;
            if (btnJoinLobby) btnJoinLobby.disabled = false;
            if (btnStart) btnStart.disabled = true;
            
            // All game actions disabled
            if (btnCheck) btnCheck.disabled = true;
            if (btnCall) btnCall.disabled = true;
            if (btnRaise) btnRaise.disabled = true;
            if (btnFold) btnFold.disabled = true;
            if (btnAllIn) btnAllIn.disabled = true;
            if (btnDeal) btnDeal.disabled = true;
            
            // Lock inputs
            if (wsUrlInput) wsUrlInput.disabled = true;
            if (playerNameInput) playerNameInput.disabled = true;
            if (startingChipsInput) startingChipsInput.disabled = true;
            if (betAmountInput) betAmountInput.disabled = true;
            break;

        case GameState.IN_LOBBY:
            // Only admin can start game, need at least 2 players
            if (btnConnect) btnConnect.disabled = false;
            if (btnRegister) btnRegister.disabled = true;
            if (btnCreateLobby) btnCreateLobby.disabled = true;
            if (btnJoinLobby) btnJoinLobby.disabled = true;
            if (btnStart) btnStart.disabled = !(isLobbyAdmin && lobbyPlayers.length >= 2);
            
            // All game actions disabled
            if (btnCheck) btnCheck.disabled = true;
            if (btnCall) btnCall.disabled = true;
            if (btnRaise) btnRaise.disabled = true;
            if (btnFold) btnFold.disabled = true;
            if (btnAllIn) btnAllIn.disabled = true;
            if (btnDeal) btnDeal.disabled = true;
            
            // Lock inputs
            if (wsUrlInput) wsUrlInput.disabled = true;
            if (playerNameInput) playerNameInput.disabled = true;
            if (startingChipsInput) startingChipsInput.disabled = true;
            if (betAmountInput) betAmountInput.disabled = true;
            break;

        case GameState.IN_GAME:
            // No lobby actions during game
            if (btnConnect) btnConnect.disabled = false;
            if (btnRegister) btnRegister.disabled = true;
            if (btnCreateLobby) btnCreateLobby.disabled = true;
            if (btnJoinLobby) btnJoinLobby.disabled = true;
            if (btnStart) btnStart.disabled = true;
            
            // Game actions enabled based on turn
            const canAct = isGameActive && isPlayerTurn;
            if (btnCheck) btnCheck.disabled = !canAct;
            if (btnCall) btnCall.disabled = !canAct || currentBet === 0;
            if (btnRaise) btnRaise.disabled = !canAct;
            if (btnFold) btnFold.disabled = !canAct;
            if (btnAllIn) btnAllIn.disabled = !canAct;
            
            // Deal button only for admin/dealer
            if (btnDeal) btnDeal.disabled = !isGameActive || isPlayerTurn;
            
            // Enable bet input during game
            if (wsUrlInput) wsUrlInput.disabled = true;
            if (playerNameInput) playerNameInput.disabled = true;
            if (startingChipsInput) startingChipsInput.disabled = true;
            if (betAmountInput) betAmountInput.disabled = !canAct;
            break;
    }
}

// Connect to WebSocket
function connect() {
    const url = document.getElementById('wsUrl').value;
    addMessage('info', `🔌 Connecting to ${url}...`);
    
    // Update status to show connecting
    const statusText = document.getElementById('statusText');
    if (statusText) {
        statusText.textContent = 'Connecting...';
    }
    
    try {
        ws = new WebSocket(url);
        
        ws.onopen = () => {
            currentGameState = GameState.CONNECTED;
            updateConnectionStatus(true);
            addMessage('success', '✅ Connected to server! Ready to play!');
            
            // Remove any existing reconnect button
            const reconnectBtn = document.getElementById('btnReconnect');
            if (reconnectBtn) {
                reconnectBtn.remove();
            }
            
            updateButtonStates();
        };
        
        ws.onmessage = (event) => {
            handleMessage(event.data);
        };
        
        ws.onerror = (error) => {
            addMessage('error', '❌ Connection error - Is the server running?');
            console.error('WebSocket error:', error);
        };
        
        ws.onclose = (event) => {
            currentGameState = GameState.DISCONNECTED;
            updateConnectionStatus(false);
            
            // Determine the reason for disconnection
            if (event.wasClean) {
                addMessage('info', '🔌 Disconnected from server');
            } else {
                addMessage('error', '⚠️ Connection lost - Server might be offline');
            }
            
            // Show reconnect option
            const connectStep = document.querySelector('.onboarding-step');
            if (connectStep) {
                // Check if reconnect button already exists
                let reconnectBtn = document.getElementById('btnReconnect');
                if (!reconnectBtn) {
                    reconnectBtn = document.createElement('button');
                    reconnectBtn.id = 'btnReconnect';
                    reconnectBtn.className = 'btn-primary btn-large';
                    reconnectBtn.style.marginTop = '15px';
                    reconnectBtn.textContent = '🔄 RECONNECT';
                    reconnectBtn.onclick = () => {
                        reconnectBtn.disabled = true;
                        reconnectBtn.textContent = 'Connecting...';
                        setTimeout(() => {
                            connect();
                        }, 100);
                    };
                    connectStep.appendChild(reconnectBtn);
                }
            }
            
            // Hide registration and lobby steps
            const registerStep = document.getElementById('registerStep');
            if (registerStep) {
                registerStep.style.display = 'none';
            }
            const lobbyStep = document.getElementById('lobbyStep');
            if (lobbyStep) {
                lobbyStep.style.display = 'none';
            }
            
            updateButtonStates();
        };
        
    } catch (error) {
        addMessage('error', `❌ Connection failed: ${error.message}`);
        updateConnectionStatus(false);
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
    console.log('Received message:', data); // Debug logging
    
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
            case 'ERROR':
                addMessage('error', message.message || message.content || 'An error occurred');
                addMessage('error', message.message || message.content || 'An error occurred', 'game');
                break;
            case 'INFO':
                addMessage('info', message.message || message.content);
                addMessage('info', message.message || message.content, 'game');
                break;
            case 'success':
                addMessage('info', message.content);
                break;
            case 'response':
                handleTextResponse(message.content);
                break;
            default:
                // Try to handle as text response first
                handleTextResponse(data);
        }
    } catch (e) {
        // Plain text message - parse it
        console.log('Parsing as text response:', data);
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
            
            currentGameState = GameState.REGISTERED;
            
            // Show player ID field
            document.getElementById('playerIdDisplay').style.display = 'block';
            document.getElementById('playerIdText').value = playerId;
            
            // Show current chips
            document.getElementById('currentChipsDisplay').style.display = 'block';
            document.getElementById('currentChipsAmount').textContent = playerChips;
            
            const btnRegister = document.getElementById('btnRegister');
            if (btnRegister) {
                btnRegister.textContent = 'REGISTERED ✓';
            }
            
            registeredPlayers.push({ id: playerId, name: playerName, chips: playerChips });
            // Don't update players list here - only used in lobby context
            
            // Don't show lobby step - it's already visible
            addMessage('success', '✅ Registration complete! Now you can create or join a lobby');
            
            updateButtonStates();
        }
    }
    
    // Parse lobby creation
    if (content.includes('SUCCESS: Lobby created')) {
        const idMatch = content.match(/Lobby ID:\s*([a-f0-9-]+)/);
        const playersMatch = content.match(/Players:\s*(\d+)\/(\d+)/);
        
        if (idMatch) {
            lobbyId = idMatch[1];
            isLobbyAdmin = true;
            
            // Extract maxPlayers from the response
            if (playersMatch) {
                maxLobbyPlayers = parseInt(playersMatch[2]);
                addMessage('info', `📊 Lobby capacity: ${maxLobbyPlayers} players`);
            }
            
            currentGameState = GameState.IN_LOBBY;
            
            // Show lobby ID in onboarding
            document.getElementById('lobbyIdDisplay').style.display = 'block';
            document.getElementById('lobbyIdText').value = lobbyId;
            
            // Show lobby players container
            document.getElementById('lobbyPlayersContainer').style.display = 'block';
            
            // Initialize lobby players with yourself
            lobbyPlayers = [{ id: playerId, name: playerName, chips: playerChips }];
            
            // Update lobby display on onboarding screen
            updateLobbyPlayersList();
            
            // Transition to game screen to show seats
            showGameScreen();
            updateLobbySeatsDisplay();
            
            // Show lobby panel, hide game panel
            const lobbyPanel = document.getElementById('lobbyActionsPanel');
            const gamePanel = document.getElementById('gameActionsPanel');
            if (lobbyPanel) lobbyPanel.style.display = 'block';
            if (gamePanel) gamePanel.style.display = 'none';
            
            // Enable start button for admin with enough players
            const btnStartInGame = document.getElementById('btnStartInGame');
            if (btnStartInGame) {
                btnStartInGame.disabled = !(isLobbyAdmin && lobbyPlayers.length >= 2);
            }
            
            // Update footer lobby ID
            const lobbyIdFooter = document.getElementById('lobbyIdFooter');
            if (lobbyIdFooter) {
                lobbyIdFooter.value = lobbyId;
            }
            
            // Update header in game screen
            const lobbyHeader = document.getElementById('lobbyIdHeaderDisplay');
            if (lobbyHeader) {
                lobbyHeader.textContent = lobbyId.substring(0, 8) + '...';
            }
            
            // Update player chips in header
            const chipsHeader = document.getElementById('playerChipsHeader');
            if (chipsHeader) {
                chipsHeader.textContent = playerChips;
            }
            
            addMessage('event', '🏠 You are the lobby admin - You can START the game');
            addMessage('info', `📋 Share this Lobby ID with friends: ${lobbyId}`);
            updateButtonStates();
            
            // Subscribe to lobby events for real-time updates
            sendCommand(`SUBSCRIBE_LOBBY ${lobbyId} ${playerId}`);
        }
    }
    
    // Parse lobby join
    if (content.includes('SUCCESS: Joined lobby')) {
        const idMatch = content.match(/Lobby ID:\s*([a-f0-9-]+)/);
        const playersMatch = content.match(/Players:\s*(\d+)\/(\d+)/);
        
        if (idMatch) {
            lobbyId = idMatch[1];
            
            // Extract maxPlayers from the response
            if (playersMatch) {
                maxLobbyPlayers = parseInt(playersMatch[2]);
                addMessage('info', `📊 Lobby capacity: ${maxLobbyPlayers} players`);
            }
            
            currentGameState = GameState.IN_LOBBY;
            
            // Show lobby ID in onboarding
            document.getElementById('lobbyIdDisplay').style.display = 'block';
            document.getElementById('lobbyIdText').value = lobbyId;
            
            // Show lobby players container
            document.getElementById('lobbyPlayersContainer').style.display = 'block';
            
            // Initialize lobby players with yourself
            lobbyPlayers = [{ id: playerId, name: playerName, chips: playerChips }];
            
            // Update lobby display on onboarding screen
            updateLobbyPlayersList();
            
            // Transition to game screen to show seats
            showGameScreen();
            updateLobbySeatsDisplay();
            
            // Show lobby panel, hide game panel
            const lobbyPanel = document.getElementById('lobbyActionsPanel');
            const gamePanel = document.getElementById('gameActionsPanel');
            if (lobbyPanel) lobbyPanel.style.display = 'block';
            if (gamePanel) gamePanel.style.display = 'none';
            
            // Disable start button (not admin)
            const btnStartInGame = document.getElementById('btnStartInGame');
            if (btnStartInGame) {
                btnStartInGame.disabled = true;
            }
            
            // Update footer lobby ID
            const lobbyIdFooter = document.getElementById('lobbyIdFooter');
            if (lobbyIdFooter) {
                lobbyIdFooter.value = lobbyId;
            }
            
            // Update header in game screen
            const lobbyHeader = document.getElementById('lobbyIdHeaderDisplay');
            if (lobbyHeader) {
                lobbyHeader.textContent = lobbyId.substring(0, 8) + '...';
            }
            
            // Update player chips in header
            const chipsHeader = document.getElementById('playerChipsHeader');
            if (chipsHeader) {
                chipsHeader.textContent = playerChips;
            }
            
            addMessage('event', '🏠 Joined lobby - Waiting for admin to start...', 'game');
            addMessage('info', `📋 Lobby ID: ${lobbyId}`, 'game');
            updateButtonStates();
            
            // Subscribe to lobby events for real-time updates
            sendCommand(`SUBSCRIBE_LOBBY ${lobbyId} ${playerId}`);
        }
    }
    
    // Parse game start
    if (content.includes('SUCCESS: Game started')) {
        const idMatch = content.match(/Game ID:\s*([a-f0-9-]+)/);
        if (idMatch) {
            gameId = idMatch[1];
            isGameActive = true;
            
            currentGameState = GameState.IN_GAME;
            
            // Update footer to show game info instead of lobby
            const lobbyPanel = document.getElementById('lobbyActionsPanel');
            const gamePanel = document.getElementById('gameActionsPanel');
            if (lobbyPanel) lobbyPanel.style.display = 'none';
            if (gamePanel) gamePanel.style.display = 'block';
            
            // Keep header simple - don't need to show game ID there
            
            // Reset seat display for game mode (clear "Open Seat" styling)
            playerSeats.fill(null);
            for (let i = 0; i < 9; i++) {
                const seatElement = document.getElementById(`seat-${i}`);
                if (seatElement) {
                    seatElement.style.display = 'none';
                    seatElement.style.opacity = '1';
                    seatElement.style.border = '3px solid #ffd700';
                }
            }
            
            addMessage('event', '🎮 Game started!', 'game');
            addMessage('info', `🎲 Game ID: ${gameId}`, 'game');
            updateButtonStates();
            
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
    
    // Log to both onboarding and game events
    addMessage('event', `👤 ${joinedPlayerName} joined lobby (${currentCount}/${maxPlayers} players)`);
    addMessage('event', `👤 ${joinedPlayerName} joined lobby (${currentCount}/${maxPlayers} players)`, 'game');
    
    // Add player to lobby players list if not already present
    if (lobbyId === data.lobbyId) {
        const existingPlayer = lobbyPlayers.find(p => p.id === joinedPlayerId);
        if (!existingPlayer && joinedPlayerId !== playerId) {
            // Add new player
            lobbyPlayers.push({
                id: joinedPlayerId,
                name: joinedPlayerName,
                chips: 1000 // Default value, actual value will be from game
            });
        }
        
        // Always update displays for all clients
        updateLobbyPlayersList();
        updateLobbySeatsDisplay(); // Update seats to show new player
        updateButtonStates(); // Update start button when lobby size changes
    }
}

// Handle other domain events
function handleGameStateChanged(message) {
    console.log('Game state changed event:', message);
    const data = message.data || message;
    const newState = data.newState || data.state;
    
    addMessage('event', `🔄 Game state: ${newState}`, 'game');
    
    // Update current round if provided
    if (data.round || data.currentRound) {
        currentRound = data.round || data.currentRound;
    }
    
    // Request updated game state
    if (gameId) {
        sendCommand(`GET_GAME_STATE ${gameId}`);
    }
}

function handleCardsDealt(message) {
    console.log('Cards dealt event:', message);
    const data = message.data || message;
    
    addMessage('event', `🃏 Cards dealt`, 'game');
    
    // Request my cards
    if (gameId && playerId) {
        sendCommand(`GET_MY_CARDS ${gameId} ${playerId}`);
    }
    
    // Request game state to see community cards if any
    if (gameId) {
        sendCommand(`GET_GAME_STATE ${gameId}`);
    }
}

function handlePlayerAction(message) {
    console.log('Player action event:', message);
    const data = message.data || message;
    const actionPlayerId = data.playerId || message.playerId;
    const action = data.action || message.action;
    const amount = data.amount || message.amount;
    
    // Update visual indicator
    if (actionPlayerId && action) {
        updatePlayerAction(actionPlayerId, action, amount);
    }
    
    const playerName = data.playerName || actionPlayerId;
    addMessage('event', `🎲 ${playerName}: ${action}${amount ? ` $${amount}` : ''}`, 'game');
    
    // Check if it's now our turn
    if (data.nextPlayerId === playerId || data.currentPlayerId === playerId) {
        isPlayerTurn = true;
        addMessage('info', '⏰ It\'s your turn!', 'game');
        updateButtonStates();
    }
    
    // Request updated game state
    if (gameId) {
        sendCommand(`GET_GAME_STATE ${gameId}`);
    }
}

function handleWinnerDetermined(message) {
    console.log('Winner determined event:', message);
    const data = message.data || message;
    const winnerName = data.winnerName || data.winner;
    const handRank = data.handRank || data.hand;
    const potWon = data.potWon || data.amount;
    
    addMessage('success', `🏆 Winner: ${winnerName} with ${handRank} - Won $${potWon}`, 'game');
    
    // Reset turn
    isPlayerTurn = false;
    updateButtonStates();
    
    // Request final game state
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
    
    currentGameState = GameState.REGISTERED;
    
    document.getElementById('playerIdDisplay').style.display = 'block';
    document.getElementById('playerIdText').value = playerId;
    const btnRegister = document.getElementById('btnRegister');
    if (btnRegister) {
        btnRegister.textContent = 'REGISTERED ✓';
    }
    
    registeredPlayers.push({ id: playerId, name: playerName, chips: playerChips });
    // Don't update players list here - only used in lobby context
    
    // Don't show lobby step - it's already visible
    addMessage('success', '✅ Registration complete! Now you can create or join a lobby');
    
    updateButtonStates();
}

// Handle Game Started
function handleGameStarted(message) {
    console.log('Game started event:', message);
    
    // Extract from event data structure
    const data = message.data || message;
    gameId = data.gameId || message.gameId;
    isGameActive = true;
    
    currentGameState = GameState.IN_GAME;
    
    // Update footer to show game controls
    const lobbyPanel = document.getElementById('lobbyActionsPanel');
    const gamePanel = document.getElementById('gameActionsPanel');
    if (lobbyPanel) lobbyPanel.style.display = 'none';
    if (gamePanel) gamePanel.style.display = 'block';
    
    // Get player information from the data
    const players = data.players || data.playerIds || [];
    
    // If we have player IDs as strings, convert to player objects
    const playerObjects = players.map((p, index) => {
        if (typeof p === 'string') {
            // It's just an ID, try to find in lobby players
            const lobbyPlayer = lobbyPlayers.find(lp => lp.id === p);
            return {
                id: p,
                name: lobbyPlayer ? lobbyPlayer.name : `Player ${index + 1}`,
                chips: lobbyPlayer ? lobbyPlayer.chips : 1000,
                isActive: true,
                folded: false
            };
        }
        // It's already an object
        return {
            id: p.id || p.playerId,
            name: p.name || p.playerName || `Player ${index + 1}`,
            chips: p.chips || 1000,
            isActive: true,
            folded: false
        };
    });
    
    // Update player seats with game players
    updatePlayerSeats(playerObjects);
    
    addMessage('event', `🎮 Game started! Game ID: ${gameId}`, 'game');
    addMessage('info', `� ${playerObjects.length} players in game`, 'game');
    updateButtonStates();
    
    // Request initial game state and cards
    setTimeout(() => {
        sendCommand(`GET_GAME_STATE ${gameId}`);
        sendCommand(`GET_MY_CARDS ${gameId} ${playerId}`);
    }, 500);
}

// Handle Player Cards
function handlePlayerCards(message) {
    console.log('Player cards event:', message);
    const data = message.data || message;
    const cards = data.cards || message.cards || [];
    
    if (cards.length > 0) {
        displayPlayerCards(cards);
        addMessage('event', `🃏 Received your cards (${cards.length})`, 'game');
    }
}

// Handle Game State
function handleGameState(message) {
    console.log('Game state event:', message);
    const data = message.data || message;
    
    currentPot = data.pot || 0;
    currentBet = data.currentBet || 0;
    
    // Update player chips if provided
    if (data.playerChips !== undefined) {
        playerChips = data.playerChips;
        const chipsHeader = document.getElementById('playerChipsHeader');
        if (chipsHeader) {
            chipsHeader.textContent = playerChips;
        }
    }
    
    // Update display using the new function
    updateGameDisplay({
        pot: currentPot,
        currentBet: currentBet,
        communityCards: data.communityCards || [],
        round: data.round || data.currentRound
    });
    
    // Update player states if available
    if (data.players && Array.isArray(data.players)) {
        const playerObjects = data.players.map(p => ({
            id: p.id || p.playerId,
            name: p.name || p.playerName,
            chips: p.chips || p.chipCount || 0,
            isActive: p.isActive !== false, // Default to true
            folded: p.folded || p.hasFolded || false,
            currentBet: p.currentBet || 0
        }));
        updatePlayerSeats(playerObjects);
    }
    
    // Update round display
    if (data.round || data.currentRound) {
        currentRound = data.round || data.currentRound;
    }
    
    // Check if it's our turn
    if (data.currentPlayerId === playerId || data.activePlayerId === playerId) {
        isPlayerTurn = true;
        if (!document.hidden) { // Only show if window is visible
            addMessage('info', '⏰ It\'s your turn!', 'game');
        }
        updateButtonStates();
    } else {
        isPlayerTurn = false;
        updateButtonStates();
    }
}

// Handle Community Cards
function handleCommunityCards(message) {
    console.log('Community cards event:', message);
    const data = message.data || message;
    const cards = data.communityCards || data.cards || [];
    const eventType = message.eventType || message.type || '';
    
    if (cards.length > 0) {
        displayCommunityCards(cards);
    }
    
    // Update round based on event type
    let round = 'pre-flop';
    if (eventType.includes('FLOP')) {
        round = 'flop';
    } else if (eventType.includes('TURN')) {
        round = 'turn';
    } else if (eventType.includes('RIVER')) {
        round = 'river';
    }
    
    currentRound = round;
    addMessage('event', `🎴 ${round.toUpperCase()} dealt`, 'game');
    
    // Update game state
    setTimeout(() => {
        if (gameId) {
            sendCommand(`GET_GAME_STATE ${gameId}`);
        }
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

// Leave Lobby
function leaveLobby() {
    if (!playerId) {
        addMessage('error', 'No player registered');
        return;
    }
    
    if (!lobbyId) {
        addMessage('error', 'Not in a lobby');
        return;
    }
    
    if (confirm('Are you sure you want to leave the lobby?')) {
        sendCommand(`LEAVE_LOBBY ${lobbyId} ${playerId}`);
        
        // Reset lobby state
        lobbyId = null;
        isLobbyAdmin = false;
        lobbyPlayers = [];
        currentGameState = GameState.REGISTERED;
        
        // Hide lobby display
        document.getElementById('lobbyIdDisplay').style.display = 'none';
        document.getElementById('lobbyPlayersContainer').style.display = 'none';
        
        // Go back to onboarding screen
        showOnboardingScreen();
        
        addMessage('info', '👋 Left the lobby');
        updateButtonStates();
    }
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

// Copy Lobby ID from Footer
function copyLobbyIdFooter() {
    const input = document.getElementById('lobbyIdFooter');
    if (input) {
        input.select();
        navigator.clipboard.writeText(input.value).then(() => {
            addMessage('info', '📋 Lobby ID copied to clipboard!', 'game');
        }).catch(() => {
            // Fallback
            document.execCommand('copy');
            addMessage('info', '📋 Lobby ID copied to clipboard!', 'game');
        });
    }
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
    
    // After action, assume it's not our turn anymore until server confirms
    isPlayerTurn = false;
    updateButtonStates();
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
    const container = document.getElementById('yourHandGame') || document.getElementById('playerHand');
    if (!container) {
        console.warn('Player hand container not found');
        return;
    }
    
    container.innerHTML = '';
    
    if (!cards || cards.length === 0) {
        // Show empty cards
        for (let i = 0; i < 2; i++) {
            const emptyCard = document.createElement('div');
            emptyCard.className = 'card empty';
            container.appendChild(emptyCard);
        }
        return;
    }
    
    cards.forEach(cardStr => {
        const card = parseCard(cardStr);
        const cardEl = createCardElement(card);
        container.appendChild(cardEl);
    });
}

// Display Community Cards
function displayCommunityCards(cards) {
    const container = document.getElementById('communityCardsGame') || 
                     document.getElementById('communityCardsCenter') || 
                     document.getElementById('communityCards');
    
    if (!container) {
        console.warn('Community cards container not found');
        return;
    }
    
    container.innerHTML = '';
    
    // Always show 5 card slots
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
    // Handle different formats
    // Format 1: "ACE of HEARTS" or "10 of DIAMONDS"
    // Format 2: "ACE_HEARTS" or "10_DIAMONDS"  
    // Format 3: Object { rank: "ACE", suit: "HEARTS" }
    
    if (typeof cardStr === 'object') {
        return cardStr;
    }
    
    if (cardStr.includes(' of ')) {
        const parts = cardStr.split(' of ');
        return {
            rank: parts[0],
            suit: parts[1]
        };
    }
    
    if (cardStr.includes('_')) {
        const parts = cardStr.split('_');
        return {
            rank: parts[0],
            suit: parts[1]
        };
    }
    
    // Fallback - assume it's just the rank
    return {
        rank: cardStr,
        suit: 'UNKNOWN'
    };
}

// Create Card Element
function createCardElement(card) {
    if (!card) {
        const emptyCard = document.createElement('div');
        emptyCard.className = 'card empty';
        return emptyCard;
    }
    
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
    
    // Check if container exists
    if (!container) {
        console.warn('playersList container not found');
        return;
    }
    
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
    const container = document.getElementById('lobbyPlayersList');
    
    // Check if container exists
    if (!container) {
        console.warn('lobbyPlayersList container not found');
        return;
    }
    
    // Only show lobby players when in a lobby
    if (!lobbyId) {
        return; // Don't update if not in lobby yet
    }
    
    container.innerHTML = '';
    
    if (lobbyPlayers.length === 0) {
        container.innerHTML = '<div class="lobby-player-item"><span class="player-name">No players yet</span></div>';
        return;
    }
    
    lobbyPlayers.forEach(player => {
        const playerEl = document.createElement('div');
        playerEl.className = 'lobby-player-item';
        if (player.id === playerId) {
            playerEl.classList.add('you');
        }
        
        const isAdmin = isLobbyAdmin && player.id === playerId;
        playerEl.innerHTML = `
            <span style="font-weight: bold;">${player.name} ${isAdmin ? '👑' : ''} ${player.id === playerId ? '(You)' : ''}</span>
            <span style="color: #ffd700;">$${player.chips || 1000}</span>
        `;
        
        container.appendChild(playerEl);
    });
}

// Update Lobby Seats Display (show players in seats before game starts)
function updateLobbySeatsDisplay() {
    if (!lobbyId || isGameActive) {
        return; // Only update in lobby state, not during game
    }
    
    // Clear all seats first
    playerSeats.fill(null);
    
    // Assign lobby players to seats
    lobbyPlayers.forEach((player, index) => {
        if (index < maxLobbyPlayers) {
            playerSeats[index] = {
                id: player.id,
                name: player.name,
                chips: player.chips || 1000,
                isActive: false,
                folded: false
            };
        }
    });
    
    // Render all seats based on maxLobbyPlayers
    for (let i = 0; i < 9; i++) {
        const seatElement = document.getElementById(`seat-${i}`);
        if (!seatElement) continue;
        
        // Only show seats up to maxLobbyPlayers
        if (i >= maxLobbyPlayers) {
            seatElement.style.display = 'none';
            continue;
        }
        
        const player = playerSeats[i];
        
        if (player) {
            // Show player
            seatElement.style.display = 'block';
            seatElement.style.opacity = '0.7'; // Dimmed in lobby
            seatElement.style.border = '3px solid #888';
            
            const nameElement = seatElement.querySelector('.player-name-display');
            const chipsElement = seatElement.querySelector('.player-chips-display');
            
            if (nameElement) {
                nameElement.textContent = player.name || `Player ${player.id}`;
            }
            if (chipsElement) {
                chipsElement.textContent = `$${player.chips || 0}`;
            }
            
            // Highlight if it's your seat
            if (player.id === playerId) {
                seatElement.style.border = '3px solid #ffd700';
                seatElement.style.opacity = '1';
            }
        } else {
            // Show as "Open Seat" (only for available seats within maxLobbyPlayers)
            seatElement.style.display = 'block';
            seatElement.style.opacity = '0.3';
            seatElement.style.border = '3px dashed #555';
            
            const nameElement = seatElement.querySelector('.player-name-display');
            const chipsElement = seatElement.querySelector('.player-chips-display');
            
            if (nameElement) {
                nameElement.textContent = '🪑 Open Seat';
            }
            if (chipsElement) {
                chipsElement.textContent = 'Waiting...';
            }
        }
    }
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
    const indicatorGame = document.getElementById('statusIndicatorGame');
    const text = document.getElementById('statusText');
    const textGame = document.getElementById('statusTextGame');
    
    if (connected) {
        if (indicator) {
            indicator.classList.add('connected');
        }
        if (indicatorGame) {
            indicatorGame.classList.add('connected');
        }
        if (text) {
            text.textContent = 'Connected';
        }
        if (textGame) {
            textGame.textContent = 'Connected';
        }
    } else {
        if (indicator) {
            indicator.classList.remove('connected');
        }
        if (indicatorGame) {
            indicatorGame.classList.remove('connected');
        }
        if (text) {
            text.textContent = 'Disconnected';
        }
        if (textGame) {
            textGame.textContent = 'Disconnected';
        }
    }
}

// Add Message to Log
function addMessage(type, message, target = 'onboarding') {
    // Determine which message container to use
    const containerId = target === 'game' ? 'gameMessages' : 'messages';
    const container = document.getElementById(containerId);
    
    if (!container) {
        console.warn(`Message container '${containerId}' not found`);
        return;
    }
    
    const messageEl = document.createElement('div');
    messageEl.className = `message ${type}`;
    
    const time = new Date().toLocaleTimeString();
    messageEl.innerHTML = `<span class="message-time">${time}</span>${message}`;
    
    container.appendChild(messageEl);
    container.scrollTop = container.scrollHeight;
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    // Start on onboarding screen
    showOnboardingScreen();
    
    addMessage('info', '🎰 Welcome to Texas Hold\'em Poker!');
    addMessage('info', '🔌 Attempting to connect to server...');
    addMessage('info', '💡 Make sure the poker server is running on port 8081');
    updateButtonStates(); // Initialize button states on page load
    
    // Auto-connect to WebSocket server
    setTimeout(() => {
        connect();
    }, 500); // Small delay to ensure UI is ready
});
