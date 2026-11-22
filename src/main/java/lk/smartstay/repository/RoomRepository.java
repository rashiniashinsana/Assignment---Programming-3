package lk.smartstay.repository;

import lk.smartstay.model.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomRepository {

    private final Map<String, Room> rooms;

    public RoomRepository() {
        this.rooms = new HashMap<>();
    }

    public boolean addRoom(Room room) {
        if (room == null || room.getRoomId() == null || room.getRoomId().trim().isEmpty()) {
            return false;
        }
        String id = room.getRoomId().trim();
        if (rooms.containsKey(id)) {
            return false;
        }
        rooms.put(id, room);
        return true;
    }

    public boolean updateRoom(Room room) {
        if (room == null || room.getRoomId() == null || room.getRoomId().trim().isEmpty()) {
            return false;
        }
        String id = room.getRoomId().trim();
        if (!rooms.containsKey(id)) {
            return false;
        }
        rooms.put(id, room);
        return true;
    }

    public boolean removeRoom(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return false;
        }
        return rooms.remove(roomId.trim()) != null;
    }

    public Room findById(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return null;
        }
        return rooms.get(roomId.trim());
    }

    public boolean existsById(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return false;
        }
        return rooms.containsKey(roomId.trim());
    }

    public List<Room> findAll() {
        return new ArrayList<>(rooms.values());
    }

    public int count() {
        return rooms.size();
    }
}
