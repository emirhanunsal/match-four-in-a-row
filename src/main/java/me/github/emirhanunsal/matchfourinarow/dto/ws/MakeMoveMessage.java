package me.github.emirhanunsal.matchfourinarow.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeMoveMessage {
    private String roomCode;
    private String playerId;
    private int column;
}
