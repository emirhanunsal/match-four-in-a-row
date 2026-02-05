package me.github.emirhanunsal.matchfourinarow.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.github.emirhanunsal.matchfourinarow.domain.GameRoom;
import me.github.emirhanunsal.matchfourinarow.dto.ws.GameStateMessage;
import me.github.emirhanunsal.matchfourinarow.repository.RoomRepository;
import me.github.emirhanunsal.matchfourinarow.service.GameService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        log.info("WebSocket session disconnected: {}", sessionId);

        Collection<GameRoom> rooms = roomRepository.findAll();
        for (GameRoom room : rooms) {
            if (room.getState().getStatus().name().equals("IN_GAME")) {
                log.info("Active game in room {} may be affected by disconnect", room.getRoomCode());
            }
        }
    }

    public void notifyPlayerLeft(String roomCode, String playerId) {
        try {
            GameStateMessage gameState = gameService.getGameState(roomCode);
            gameState.setMessage("Player " + playerId + " has left the game");
            
            messagingTemplate.convertAndSend(
                    "/topic/room." + roomCode,
                    gameState
            );
            
            log.info("Notified room {} that player {} left", roomCode, playerId);
        } catch (Exception e) {
            log.error("Error notifying room about player leaving", e);
        }
    }
}
