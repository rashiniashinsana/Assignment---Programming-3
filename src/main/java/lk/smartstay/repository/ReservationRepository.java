package lk.smartstay.repository;

import lk.smartstay.model.Reservation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReservationRepository {

    private final Map<String, Reservation> reservations;

    public ReservationRepository() {
        this.reservations = new HashMap<>();
    }

    public boolean addReservation(Reservation reservation) {
        if (reservation == null || reservation.getBookingId() == null || reservation.getBookingId().trim().isEmpty()) {
            return false;
        }
        String id = reservation.getBookingId().trim();
        if (reservations.containsKey(id)) {
            return false;
        }
        reservations.put(id, reservation);
        return true;
    }

    public boolean updateReservation(Reservation reservation) {
        if (reservation == null || reservation.getBookingId() == null || reservation.getBookingId().trim().isEmpty()) {
            return false;
        }
        String id = reservation.getBookingId().trim();
        if (!reservations.containsKey(id)) {
            return false;
        }
        reservations.put(id, reservation);
        return true;
    }

    public boolean removeReservation(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return false;
        }
        return reservations.remove(bookingId.trim()) != null;
    }

    public Reservation findByBookingId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return null;
        }
        return reservations.get(bookingId.trim());
    }

    public List<Reservation> findByGuestName(String guestName) {
        List<Reservation> matches = new ArrayList<>();
        if (guestName == null || guestName.trim().isEmpty()) {
            return matches;
        }
        String query = guestName.trim().toLowerCase(Locale.ROOT);
        for (Reservation r : reservations.values()) {
            if (r.getGuest() != null && r.getGuest().getFullName() != null) {
                if (r.getGuest().getFullName().toLowerCase(Locale.ROOT).contains(query)) {
                    matches.add(r);
                }
            }
        }
        return matches;
    }

    public List<Reservation> findByCheckInDate(LocalDate date) {
        List<Reservation> matches = new ArrayList<>();
        if (date == null) {
            return matches;
        }
        for (Reservation r : reservations.values()) {
            if (date.equals(r.getCheckInDate())) {
                matches.add(r);
            }
        }
        return matches;
    }

    public List<Reservation> findByRoomId(String roomId) {
        List<Reservation> matches = new ArrayList<>();
        if (roomId == null || roomId.trim().isEmpty()) {
            return matches;
        }
        String targetRoomId = roomId.trim();
        for (Reservation r : reservations.values()) {
            if (r.getRoom() != null && targetRoomId.equalsIgnoreCase(r.getRoom().getRoomId())) {
                matches.add(r);
            }
        }
        return matches;
    }

    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }

    public int count() {
        return reservations.size();
    }
}
