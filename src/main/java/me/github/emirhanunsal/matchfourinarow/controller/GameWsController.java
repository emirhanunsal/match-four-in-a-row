package me.github.emirhanunsal.matchfourinarow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.github.emirhanunsal.matchfourinarow.dto.ws.ErrorMessage;
import me.github.emirhanunsal.matchfourinarow.dto.ws.GameStateMessage;
import me.github.emirhanunsal.matchfourinarow.dto.ws.JoinRoomMessage;
import me.github.emirhanunsal.matchfourinarow.dto.ws.MakeMoveMessage;
import me.github.emirhanunsal.matchfourinarow.service.GameService;
import me.github.emirhanunsal.matchfourinarow.service.RoomService;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GameWsController {
    private final GameService gameService;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room.join")
    public void joinRoom(JoinRoomMessage message) {
        try {
            log.info("WebSocket join request: player={}, room={}", 
                    message.getPlayerId(), message.getRoomCode());

            roomService.joinRoom(message.getRoomCode(), message.getPlayerId());

            GameStateMessage gameState = gameService.getGameState(message.getRoomCode());
            gameState.setMessage("Player joined");

            messagingTemplate.convertAndSend(
                    "/topic/room." + message.getRoomCode(),
                    gameState
            );

            log.info("Player {} joined room {} via WebSocket", 
                    message.getPlayerId(), message.getRoomCode());

        } catch (Exception e) {
            log.error("Error joining room via WebSocket", e);
            sendErrorToUser(message.getPlayerId(), "JOIN_ERROR", e.getMessage());
        }
    }

    @MessageMapping("/room.move")
    public void makeMove(MakeMoveMessage message) {
        try {
            log.info("WebSocket move request: player={}, room={}, column={}", 
                    message.getPlayerId(), message.getRoomCode(), message.getColumn());

            GameStateMessage gameState = gameService.makeMove(
                    message.getRoomCode(),
                    message.getPlayerId(),
                    message.getColumn()
            );

            messagingTemplate.convertAndSend(
                    "/topic/room." + message.getRoomCode(),
                    gameState
            );

            log.info("Move processed: player={}, room={}, column={}, status={}", 
                    message.getPlayerId(), message.getRoomCode(), 
                    message.getColumn(), gameState.getStatus());

        } catch (Exception e) {
            log.error("Error making move via WebSocket", e);
            sendErrorToUser(message.getPlayerId(), "MOVE_ERROR", e.getMessage());
        }
    }

    private void sendErrorToUser(String userId, String errorCode, String errorMessage) {
        ErrorMessage error = new ErrorMessage(errorCode, errorMessage);
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/errors",
                error
        );
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public ErrorMessage handleException(Exception e) {
        log.error("WebSocket error", e);
        return new ErrorMessage("ERROR", e.getMessage());
    }
}
