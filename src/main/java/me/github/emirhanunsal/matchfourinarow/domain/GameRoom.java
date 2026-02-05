package me.github.emirhanunsal.matchfourinarow.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameRoom {
    private String roomCode;
    private List<Player> players;
    private GameState state;

    public GameRoom(String roomCode) {
        this.roomCode = roomCode;
        this.players = new ArrayList<>();
        this.state = new GameState();
    }

    public void addPlayer(Player player) {
        if (players.size() >= 2) {
            throw new IllegalStateException("Room is full");
        }

        if (players.isEmpty()) {
            player.setDisc(Disc.RED);
        } else {
            player.setDisc(Disc.YELLOW);
        }

        players.add(player);

        if (players.size() == 2) {
            state.startGame();
        }
    }

    public void removePlayer(String playerId) {
        players.removeIf(p -> p.getPlayerId().equals(playerId));
        
        if (state.getStatus() == GameStatus.IN_GAME) {
            state.setStatus(GameStatus.FINISHED);
        }
    }

    public Player getPlayer(String playerId) {
        return players.stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public boolean isFull() {
        return players.size() >= 2;
    }

    public boolean hasPlayer(String playerId) {
        return players.stream().anyMatch(p -> p.getPlayerId().equals(playerId));
    }
}
