let stompClient = null;
let currentRoomCode = null;
let currentPlayerId = null;
let currentPlayerDisc = null;
let gameState = null;

const menuScreen = document.getElementById('menu-screen');
const gameScreen = document.getElementById('game-screen');
const newGameBtn = document.getElementById('new-game-btn');
const joinGameBtn = document.getElementById('join-game-btn');
const roomCodeInput = document.getElementById('room-code-input');
const backToMenuBtn = document.getElementById('back-to-menu-btn');
const roomCodeDisplay = document.getElementById('room-code-display');
const playerDiscDisplay = document.getElementById('player-disc');
const gameStatusDisplay = document.getElementById('game-status');
const messageDisplay = document.getElementById('message-display');
const boardElement = document.getElementById('board');

document.addEventListener('DOMContentLoaded', () => {
    newGameBtn.addEventListener('click', createNewGame);
    joinGameBtn.addEventListener('click', joinGame);
    backToMenuBtn.addEventListener('click', backToMenu);
    
    roomCodeInput.addEventListener('input', (e) => {
        e.target.value = e.target.value.toUpperCase();
    });
});

async function createNewGame() {
    try {
        const response = await fetch('/api/rooms', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to create room');
        }

        const data = await response.json();
        currentRoomCode = data.roomCode;
        currentPlayerId = data.playerId;
        currentPlayerDisc = data.disc;

        showMessage('Room created! Waiting for opponent...', 'info');
        connectWebSocket();
        switchToGameScreen();
    } catch (error) {
        console.error('Error creating game:', error);
        showMessage('Error creating game: ' + error.message, 'error');
    }
}

async function joinGame() {
    const roomCode = roomCodeInput.value.trim().toUpperCase();
    
    if (!roomCode || roomCode.length < 6) {
        showMessage('Please enter a valid room code', 'error');
        return;
    }

    try {
        currentPlayerId = generatePlayerId();
        
        const response = await fetch(`/api/rooms/${roomCode}/join`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                playerId: currentPlayerId
            })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to join room');
        }

        const data = await response.json();
        currentRoomCode = data.roomCode;
        currentPlayerDisc = data.disc;

        showMessage('Joined room successfully!', 'success');
        connectWebSocket();
        switchToGameScreen();
    } catch (error) {
        console.error('Error joining game:', error);
        showMessage('Error joining game: ' + error.message, 'error');
    }
}

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, (frame) => {
        console.log('Connected to WebSocket:', frame);

        stompClient.subscribe(`/topic/room.${currentRoomCode}`, (message) => {
            const gameStateMsg = JSON.parse(message.body);
            handleGameStateUpdate(gameStateMsg);
        });

        stompClient.subscribe(`/user/queue/errors`, (message) => {
            const error = JSON.parse(message.body);
            showMessage('Error: ' + error.message, 'error');
        });

        stompClient.send('/app/room.join', {}, JSON.stringify({
            roomCode: currentRoomCode,
            playerId: currentPlayerId
        }));
    }, (error) => {
        console.error('WebSocket error:', error);
        showMessage('Connection error. Please refresh.', 'error');
    });
}

function handleGameStateUpdate(gameStateMsg) {
    console.log('Game state update:', gameStateMsg);
    gameState = gameStateMsg;
    
    renderBoard(gameStateMsg.board);
    updateGameInfo(gameStateMsg);
    
    if (gameStateMsg.message) {
        let messageType = 'info';
        if (gameStateMsg.status === 'FINISHED') {
            messageType = 'success';
        }
        showMessage(gameStateMsg.message, messageType);
    }
}

function renderBoard(board) {
    boardElement.innerHTML = '';
    
    for (let row = 0; row < 6; row++) {
        for (let col = 0; col < 7; col++) {
            const cell = document.createElement('div');
            cell.className = 'cell';
            cell.dataset.row = row;
            cell.dataset.col = col;
            
            const disc = board[row][col];
            cell.classList.add(disc);
            
            if (row === 0) {
                cell.addEventListener('click', () => handleCellClick(col));
            }
            
            boardElement.appendChild(cell);
        }
    }
}

function handleCellClick(column) {
    if (!gameState || gameState.status !== 'IN_GAME') {
        showMessage('Game is not in progress', 'error');
        return;
    }

    if (gameState.currentTurn !== currentPlayerDisc) {
        showMessage('Not your turn!', 'error');
        return;
    }

    stompClient.send('/app/room.move', {}, JSON.stringify({
        roomCode: currentRoomCode,
        playerId: currentPlayerId,
        column: column
    }));
}

function updateGameInfo(gameStateMsg) {
    roomCodeDisplay.textContent = currentRoomCode;
    playerDiscDisplay.textContent = currentPlayerDisc || 'Waiting...';
    playerDiscDisplay.style.color = currentPlayerDisc === 'RED' ? '#dc3545' : '#ffc107';
    
    let status = '';
    switch (gameStateMsg.status) {
        case 'WAITING':
            status = 'Waiting for players...';
            break;
        case 'IN_GAME':
            if (gameStateMsg.currentTurn === currentPlayerDisc) {
                status = 'Your turn!';
            } else {
                status = "Opponent's turn";
            }
            break;
        case 'FINISHED':
            if (gameStateMsg.winnerPlayerId === currentPlayerId) {
                status = 'You won!';
            } else if (gameStateMsg.winnerPlayerId) {
                status = 'You lost!';
            } else {
                status = 'Draw!';
            }
            break;
    }
    gameStatusDisplay.textContent = status;
}

function showMessage(text, type = 'info') {
    messageDisplay.textContent = text;
    messageDisplay.className = `message ${type}`;
}

function switchToGameScreen() {
    menuScreen.classList.add('hidden');
    gameScreen.classList.remove('hidden');
    
    roomCodeDisplay.textContent = currentRoomCode;
    playerDiscDisplay.textContent = currentPlayerDisc || 'Waiting...';
    if (currentPlayerDisc) {
        playerDiscDisplay.style.color = currentPlayerDisc === 'RED' ? '#dc3545' : '#ffc107';
    }
    gameStatusDisplay.textContent = 'Connecting...';
}

function backToMenu() {
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
    }
    
    currentRoomCode = null;
    currentPlayerId = null;
    currentPlayerDisc = null;
    gameState = null;
    roomCodeInput.value = '';
    
    gameScreen.classList.add('hidden');
    menuScreen.classList.remove('hidden');
    messageDisplay.textContent = '';
}

function generatePlayerId() {
    return 'player_' + Math.random().toString(36).substr(2, 9);
}
