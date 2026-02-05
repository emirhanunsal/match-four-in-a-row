package me.github.emirhanunsal.matchfourinarow.repository;

import me.github.emirhanunsal.matchfourinarow.domain.GameRoom;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRoomRepository implements RoomRepository {
    private final ConcurrentHashMap<String, GameRoom> rooms = new ConcurrentHashMap<>();

    @Override
    public void save(GameRoom room) {
        rooms.put(room.getRoomCode(), room);
    }

    @Override
    public Optional<GameRoom> findByCode(String roomCode) {
        return Optional.ofNullable(rooms.get(roomCode));
    }

    @Override
    public void delete(String roomCode) {
        rooms.remove(roomCode);
    }

    @Override
    public Collection<GameRoom> findAll() {
        return rooms.values();
    }

    @Override
    public boolean exists(String roomCode) {
        return rooms.containsKey(roomCode);
    }
}
