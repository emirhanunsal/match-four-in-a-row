package me.github.emirhanunsal.matchfourinarow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.github.emirhanunsal.matchfourinarow.domain.Disc;

@Data
@AllArgsConstructor
public class JoinRoomResponse {
    private String roomCode;
    private String playerId;
    private Disc disc;
}
