package me.github.emirhanunsal.matchfourinarow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.github.emirhanunsal.matchfourinarow.domain.GameRoom;
import me.github.emirhanunsal.matchfourinarow.domain.Player;
import me.github.emirhanunsal.matchfourinarow.repository.RoomRepository;
import me.github.emirhanunsal.matchfourinarow.util.CodeGenerator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {
    private final RoomRepository roomRepository;
    private final CodeGenerator codeGenerator;

    private static final int MAX_RETRIES = 10;

    public GameRoom createRoom() {
        String roomCode = generateUniqueRoomCode();
        GameRoom room = new GameRoom(roomCode);
        
        String playerId = UUID.randomUUID().toString();
        Player player = new Player(playerId, null);
        room.addPlayer(player);
        
        roomRepository.save(room);
        log.info("Created room with code: {}, player: {}", roomCode, playerId);
        
        return room;
    }

    public GameRoom joinRoom(String roomCode, String playerId) {
        GameRoom room = roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomCode));

        if (room.isFull() && !room.hasPlayer(playerId)) {
            throw new IllegalStateException("Room is full");
        }

        if (!room.hasPlayer(playerId)) {
            Player player = new Player(playerId, null);
            room.addPlayer(player);
            roomRepository.save(room);
            log.info("Player {} joined room {}", playerId, roomCode);
        } else {
            log.info("Player {} rejoined room {}", playerId, roomCode);
        }

        return room;
    }

    public GameRoom getRoom(String roomCode) {
        return roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomCode));
    }

    private String generateUniqueRoomCode() {
        for (int i = 0; i < MAX_RETRIES; i++) {
            String code = codeGenerator.generateRoomCode();
            if (!roomRepository.exists(code)) {
                return code;
            }
        }
        throw new RuntimeException("Failed to generate unique room code after " + MAX_RETRIES + " attempts");
    }

    public void deleteRoom(String roomCode) {
        roomRepository.delete(roomCode);
        log.info("Deleted room: {}", roomCode);
    }
}
