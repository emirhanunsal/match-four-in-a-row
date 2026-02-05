package me.github.emirhanunsal.matchfourinarow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.github.emirhanunsal.matchfourinarow.domain.GameRoom;
import me.github.emirhanunsal.matchfourinarow.domain.Player;
import me.github.emirhanunsal.matchfourinarow.dto.CreateRoomResponse;
import me.github.emirhanunsal.matchfourinarow.dto.JoinRoomRequest;
import me.github.emirhanunsal.matchfourinarow.dto.JoinRoomResponse;
import me.github.emirhanunsal.matchfourinarow.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RoomController {
    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<CreateRoomResponse> createRoom() {
        GameRoom room = roomService.createRoom();
        Player player = room.getPlayers().get(0);
        
        CreateRoomResponse response = new CreateRoomResponse(
            room.getRoomCode(),
            player.getPlayerId(),
            player.getDisc()
        );
        
        log.info("Room created via REST: {}", room.getRoomCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{code}/join")
    public ResponseEntity<JoinRoomResponse> joinRoom(
            @PathVariable String code,
            @Valid @RequestBody JoinRoomRequest request) {
        
        GameRoom room = roomService.joinRoom(code, request.getPlayerId());
        Player player = room.getPlayer(request.getPlayerId());
        
        JoinRoomResponse response = new JoinRoomResponse(
            room.getRoomCode(),
            player.getPlayerId(),
            player.getDisc()
        );
        
        log.info("Player {} joined room {} via REST", request.getPlayerId(), code);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<GameRoom> getRoom(@PathVariable String code) {
        GameRoom room = roomService.getRoom(code);
        return ResponseEntity.ok(room);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    record ErrorResponse(String message) {}
}
