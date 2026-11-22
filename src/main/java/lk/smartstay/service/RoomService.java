package lk.smartstay.service;

import lk.smartstay.enums.RoomStatus;
import lk.smartstay.model.Reservation;
import lk.smartstay.model.Room;
import lk.smartstay.repository.DatabaseManager;
import lk.smartstay.repository.ReservationRepository;
import lk.smartstay.repository.RoomRepository;

import java.util.ArrayList;
import java.util.List;

public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        if (roomRepository == null) {
            throw new IllegalArgumentException("RoomRepository cannot be null.");
        }
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    public RoomService(RoomRepository roomRepository) {
        this(roomRepository, null);
    }

    public boolean addRoom(Room room) {
        if (room == null || room.getRoomId() == null || room.getRoomId().trim().isEmpty()) {
            return false;
        }
        if (roomRepository.existsById(room.getRoomId().trim())) {
            return false;
        }
        boolean ok = roomRepository.addRoom(room);
        if (ok) {
            DatabaseManager.getInstance().autoSave();
        }
        return ok;
    }

    public boolean updateRoom(Room room) {
        if (room == null || room.getRoomId() == null || room.getRoomId().trim().isEmpty()) {
            return false;
        }
        if (!roomRepository.existsById(room.getRoomId().trim())) {
            return false;
        }
        boolean ok = roomRepository.updateRoom(room);
        if (ok) {
            DatabaseManager.getInstance().autoSave();
        }
        return ok;
    }

    public boolean removeRoom(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return false;
        }
        String id = roomId.trim();
        if (reservationRepository != null) {
            List<Reservation> roomReservations = reservationRepository.findByRoomId(id);
            if (!roomReservations.isEmpty()) {
                return false;
            }
        }
        boolean ok = roomRepository.removeRoom(id);
        if (ok) {
            DatabaseManager.getInstance().autoSave();
        }
        return ok;
    }

    public boolean updateRoomStatus(String roomId, RoomStatus status) {
        if (status == null || roomId == null || roomId.trim().isEmpty()) {
            return false;
        }
        Room room = roomRepository.findById(roomId.trim());
        if (room == null) {
            return false;
        }
        room.updateStatus(status);
        boolean ok = roomRepository.updateRoom(room);
        if (ok) {
            DatabaseManager.getInstance().autoSave();
        }
        return ok;
    }

    public Room findRoomById(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return null;
        }
        return roomRepository.findById(roomId.trim());
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getAvailableRooms() {
        List<Room> available = new ArrayList<>();
        for (Room r : roomRepository.findAll()) {
            if (r.isAvailable()) {
                available.add(r);
            }
        }
        return available;
    }
}
