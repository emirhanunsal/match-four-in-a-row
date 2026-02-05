package me.github.emirhanunsal.matchfourinarow.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomMessage {
    private String roomCode;
    private String playerId;
}
