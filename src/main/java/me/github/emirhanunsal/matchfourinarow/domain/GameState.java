package me.github.emirhanunsal.matchfourinarow.domain;

import lombok.Data;

@Data
public class GameState {
    private Board board;
    private Disc currentTurn;
    private GameStatus status;
    private String winnerPlayerId;

    public GameState() {
        this.board = new Board();
        this.status = GameStatus.WAITING;
        this.currentTurn = Disc.RED;
    }

    public void switchTurn() {
        this.currentTurn = (this.currentTurn == Disc.RED) ? Disc.YELLOW : Disc.RED;
    }

    public void setWinner(String playerId) {
        this.winnerPlayerId = playerId;
        this.status = GameStatus.FINISHED;
    }

    public void setDraw() {
        this.status = GameStatus.FINISHED;
        this.winnerPlayerId = null;
    }

    public void startGame() {
        this.status = GameStatus.IN_GAME;
    }
}
