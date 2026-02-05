package me.github.emirhanunsal.matchfourinarow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.github.emirhanunsal.matchfourinarow.domain.*;
import me.github.emirhanunsal.matchfourinarow.dto.ws.GameStateMessage;
import me.github.emirhanunsal.matchfourinarow.repository.RoomRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final RoomRepository roomRepository;

    public GameStateMessage makeMove(String roomCode, String playerId, int column) {
        GameRoom room = roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomCode));

        GameState state = room.getState();

        Player player = room.getPlayer(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Player not in room: " + playerId);
        }

        if (state.getStatus() != GameStatus.IN_GAME) {
            throw new IllegalStateException("Game is not in progress");
        }

        if (player.getDisc() != state.getCurrentTurn()) {
            throw new IllegalStateException("Not your turn");
        }

        if (column < 0 || column > 6) {
            throw new IllegalArgumentException("Invalid column: " + column);
        }

        if (state.getBoard().isColumnFull(column)) {
            throw new IllegalStateException("Column is full");
        }

        int row = state.getBoard().dropDisc(column, player.getDisc());
        log.info("Player {} dropped {} disc in column {} (row {}) in room {}", 
                playerId, player.getDisc(), column, row, roomCode);

        Disc winner = state.getBoard().checkWinner(row, column);
        if (winner != null) {
            state.setWinner(playerId);
            log.info("Player {} won in room {}", playerId, roomCode);
            return createGameStateMessage(room, "Player won!");
        }

        if (state.getBoard().isFull()) {
            state.setDraw();
            log.info("Game ended in draw in room {}", roomCode);
            return createGameStateMessage(room, "Game ended in a draw!");
        }

        state.switchTurn();
        roomRepository.save(room);

        return createGameStateMessage(room, "Move successful");
    }

    public GameStateMessage getGameState(String roomCode) {
        GameRoom room = roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomCode));

        return createGameStateMessage(room, "Current game state");
    }

    private GameStateMessage createGameStateMessage(GameRoom room, String message) {
        GameState state = room.getState();
        
        GameStateMessage msg = new GameStateMessage();
        msg.setRoomCode(room.getRoomCode());
        msg.setBoard(state.getBoard().getGrid());
        msg.setCurrentTurn(state.getCurrentTurn());
        msg.setStatus(state.getStatus());
        msg.setWinnerPlayerId(state.getWinnerPlayerId());
        msg.setMessage(message);
        
        return msg;
    }
}
