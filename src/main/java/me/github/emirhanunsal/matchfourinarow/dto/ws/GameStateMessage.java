package me.github.emirhanunsal.matchfourinarow.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.github.emirhanunsal.matchfourinarow.domain.Disc;
import me.github.emirhanunsal.matchfourinarow.domain.GameStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStateMessage {
    private String roomCode;
    private Disc[][] board;
    private Disc currentTurn;
    private GameStatus status;
    private String winnerPlayerId;
    private String message;
}
