package me.github.emirhanunsal.matchfourinarow.repository;

import me.github.emirhanunsal.matchfourinarow.domain.GameRoom;

import java.util.Collection;
import java.util.Optional;

public interface RoomRepository {
    void save(GameRoom room);
    Optional<GameRoom> findByCode(String roomCode);
    void delete(String roomCode);
    Collection<GameRoom> findAll();
    boolean exists(String roomCode);
}
