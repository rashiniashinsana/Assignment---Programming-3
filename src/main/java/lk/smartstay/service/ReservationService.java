package lk.smartstay.service;

import lk.smartstay.enums.ReservationStatus;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Guest;
import lk.smartstay.model.Reservation;
import lk.smartstay.model.Room;
import lk.smartstay.repository.GuestRepository;
import lk.smartstay.repository.ReservationRepository;
import lk.smartstay.repository.RoomRepository;
import lk.smartstay.util.IdGenerator;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReservationService {

    public static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");
    public static final int MINIMUM_ADVANCE_BOOKING_DAYS = 2;
    public static final double STANDARD_BOOKING_ADVANCE_LKR = Reservation.DEFAULT_BOOKING_ADVANCE;
    public static final LocalTime CHECK_IN_TIME = LocalTime.of(14, 0);
    public static final int CANCELLATION_LOCK_HOURS = 24;

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final Clock clock;

    public ReservationService(RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            GuestRepository guestRepository,
            Clock clock) {
        if (roomRepository == null) {
            throw new IllegalArgumentException("RoomRepository cannot be null.");
        }
        if (reservationRepository == null) {
            throw new IllegalArgumentException("ReservationRepository cannot be null.");
        }
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.guestRepository = guestRepository;
        this.clock = (clock != null) ? clock : Clock.system(COLOMBO_ZONE);
    }

    public ReservationService(RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            GuestRepository guestRepository) {
        this(roomRepository, reservationRepository, guestRepository, Clock.system(COLOMBO_ZONE));
    }

    public ReservationService(RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            Clock clock) {
        this(roomRepository, reservationRepository, null, clock);
    }

    public ReservationService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this(roomRepository, reservationRepository, null, Clock.system(COLOMBO_ZONE));
    }

    public ReservationService() {
        this(new RoomRepository(), new ReservationRepository(), new GuestRepository(), Clock.system(COLOMBO_ZONE));
    }

    public List<Room> searchAvailableRooms(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        if (type == null) {
            throw new IllegalArgumentException("Room type cannot be null.");
        }
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates cannot be null.");
        }
        if (checkIn.isBefore(LocalDate.now(clock))) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must logically be after check-in date.");
        }

        List<Room> availableRooms = new ArrayList<>();
        List<Room> allRooms = roomRepository.findAll();

        for (Room room : allRooms) {
            if (room.getRoomType() == type && isRoomAvailableForDates(room, checkIn, checkOut)) {
                availableRooms.add(room);
            }
        }

        availableRooms.sort(Comparator.comparing(Room::getRoomId));
        return availableRooms;
    }

    public boolean isRoomAvailableForDates(Room room, LocalDate checkIn, LocalDate checkOut) {
        if (room == null || !room.isAvailable()) {
            return false;
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return false;
        }

        List<Reservation> roomReservations = reservationRepository.findByRoomId(room.getRoomId());
        for (Reservation res : roomReservations) {
            if (res.isCancelled()) {
                continue;
            }
            // Check for collision between requested stay interval and existing reservation
            // interval
            if (hasDateOverlap(checkIn, checkOut, res.getCheckInDate(), res.getCheckOutDate())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Algorithm: 2-Day Advance Booking Window Validation.
     * Enforces the business rule that reservations must be submitted at least
     * 2 calendar days prior to the arrival date (relative to Asia/Colombo clock).
     *
     * @param checkIn The intended check-in date
     * @return true if check-in is at least 2 days ahead of today
     */
    public boolean validateAdvanceBooking(LocalDate checkIn) {
        if (checkIn == null) {
            return false;
        }
        LocalDate minAllowedCheckIn = LocalDate.now(clock).plusDays(MINIMUM_ADVANCE_BOOKING_DAYS);
        return !checkIn.isBefore(minAllowedCheckIn);
    }

    public Reservation createReservation(Guest guest, Room room, int numberOfGuests,
            LocalDate checkIn, LocalDate checkOut) {

        if (guest == null || guest.getGuestId() == null || guest.getGuestId().trim().isEmpty()) {
            throw new IllegalArgumentException("A valid registered guest is required to create a reservation.");
        }
        if (room == null || room.getRoomId() == null || room.getRoomId().trim().isEmpty()) {
            throw new IllegalArgumentException("A valid hotel room must be selected for booking.");
        }
        if (numberOfGuests <= 0) {
            throw new IllegalArgumentException("Number of guests must be greater than zero.");
        }
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates cannot be null.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must logically be after check-in date.");
        }

        Guest canonicalGuest;
        if (guestRepository != null) {
            canonicalGuest = guestRepository.findById(guest.getGuestId().trim());
            if (canonicalGuest == null) {
                throw new IllegalArgumentException(
                        "Guest with ID '" + guest.getGuestId().trim() + "' is not registered in GuestRepository.");
            }
        } else {
            canonicalGuest = guest;
        }

        Room canonicalRoom = roomRepository.findById(room.getRoomId().trim());
        if (canonicalRoom == null) {
            throw new IllegalArgumentException(
                    "Room with ID '" + room.getRoomId().trim() + "' does not exist in RoomRepository.");
        }

        if (!validateAdvanceBooking(checkIn)) {
            LocalDate minAllowedCheckIn = LocalDate.now(clock).plusDays(MINIMUM_ADVANCE_BOOKING_DAYS);
            throw new IllegalArgumentException(
                    "Reservations must be made at least 2 calendar days before check-in. Earliest allowed check-in date is "
                            + minAllowedCheckIn + ".");
        }

        if (canonicalRoom.getStatus() != RoomStatus.AVAILABLE) {
            throw new IllegalStateException("Room " + canonicalRoom.getRoomId() + " is currently "
                    + canonicalRoom.getStatus().getDisplayName() + " and cannot be booked.");
        }
        if (!isRoomAvailableForDates(canonicalRoom, checkIn, checkOut)) {
            throw new IllegalStateException(
                    "Room " + canonicalRoom.getRoomId() + " is not available for requested dates ("
                            + checkIn + " to " + checkOut + ") due to an existing non-cancelled reservation.");
        }

        String bookingId;
        do {
            bookingId = IdGenerator.generateBookingId();
        } while (reservationRepository.findByBookingId(bookingId) != null);

        Reservation reservation = new Reservation(
                bookingId,
                canonicalGuest,
                canonicalRoom,
                numberOfGuests,
                checkIn,
                checkOut,
                STANDARD_BOOKING_ADVANCE_LKR,
                ReservationStatus.CONFIRMED);

        boolean saved = reservationRepository.addReservation(reservation);
        if (!saved) {
            throw new IllegalStateException(
                    "Failed to save reservation record: Booking ID '" + bookingId + "' already exists.");
        }

        return reservation;
    }

    public Reservation searchByBookingId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return null;
        }
        return reservationRepository.findByBookingId(bookingId.trim());
    }

    public List<Reservation> searchByGuestName(String guestName) {
        if (guestName == null || guestName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<Reservation> matches = reservationRepository.findByGuestName(guestName.trim());
        matches.sort(Comparator.comparing(Reservation::getCheckInDate).thenComparing(Reservation::getBookingId));
        return matches;
    }

    public List<Reservation> getReservationsByCheckInDate(LocalDate date) {
        if (date == null) {
            return Collections.emptyList();
        }
        List<Reservation> matches = reservationRepository.findByCheckInDate(date);
        matches.sort(Comparator.comparing(Reservation::getBookingId));
        return matches;
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> all = reservationRepository.findAll();
        all.sort(Comparator.comparing(Reservation::getCheckInDate).thenComparing(Reservation::getBookingId));
        return all;
    }

    public boolean hasReservationHistoryForRoom(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return false;
        }
        return !reservationRepository.findByRoomId(roomId.trim()).isEmpty();
    }

    /**
     * Algorithm: 24-Hour Cancellation Policy Evaluation.
     * Evaluates cancellation eligibility against hotel business rules:
     * 1. Reservation must currently be in CONFIRMED status.
     * 2. Check-in occurs at 14:00 (Asia/Colombo) on the arrival date.
     * 3. Current system time must be at least 24 hours prior to standard check-in
     * time.
     *
     * @param reservation The booking record to evaluate
     * @return true if reservation is eligible for cancellation and refund
     */
    public boolean canCancel(Reservation reservation) {
        if (reservation == null) {
            return false;
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return false;
        }
        LocalDate checkInDate = reservation.getCheckInDate();
        if (checkInDate == null) {
            return false;
        }

        ZonedDateTime checkInDateTime = checkInDate.atTime(CHECK_IN_TIME).atZone(clock.getZone());
        ZonedDateTime now = ZonedDateTime.now(clock);
        Duration remaining = Duration.between(now, checkInDateTime);

        if (remaining.isNegative()) {
            return false;
        }
        return remaining.compareTo(Duration.ofHours(CANCELLATION_LOCK_HOURS)) >= 0;
    }

    public String getCancellationIneligibilityReason(Reservation reservation) {
        if (reservation == null) {
            return "Reservation record does not exist.";
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return "Reservation is already cancelled. Cancelled reservations cannot be cancelled again.";
        }
        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            return "Reservation is completed. Past stays cannot be cancelled.";
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return "Only confirmed reservations can be cancelled (current status: "
                    + reservation.getStatus().getDisplayName() + ").";
        }
        if (reservation.getCheckInDate() == null) {
            return "Reservation check-in date is missing.";
        }

        ZonedDateTime checkInDateTime = reservation.getCheckInDate().atTime(CHECK_IN_TIME).atZone(clock.getZone());
        ZonedDateTime now = ZonedDateTime.now(clock);
        Duration remaining = Duration.between(now, checkInDateTime);

        if (remaining.isNegative()) {
            return "Check-in time (" + reservation.getCheckInDate() + " at " + CHECK_IN_TIME + ") has already passed.";
        }
        if (remaining.compareTo(Duration.ofHours(CANCELLATION_LOCK_HOURS)) < 0) {
            long remainingHours = remaining.toHours();
            long remainingMinutes = remaining.toMinutes() % 60;
            return "Reservation cannot be cancelled within 24 hours of check-in (Standard check-in: "
                    + reservation.getCheckInDate() + " at " + CHECK_IN_TIME
                    + "; Remaining: " + remainingHours + "h " + remainingMinutes + "m).";
        }

        return null;
    }

    public boolean cancelReservation(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return false;
        }
        Reservation reservation = reservationRepository.findByBookingId(bookingId.trim());
        if (reservation == null) {
            return false;
        }
        if (!canCancel(reservation)) {
            return false;
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.updateReservation(reservation);
    }

    public double getRefundAmount(Reservation reservation) {
        if (reservation == null) {
            return 0.0;
        }
        return reservation.getBookingAdvance();
    }

    /**
     * Algorithm: Date Overlap Interval Collision Detection.
     * Evaluates whether two date intervals collision occurs.
     * Formally, two half-open intervals [A_start, A_end) and [B_start, B_end)
     * overlap
     * if and only if:
     * (A_start < B_end) AND (B_start < A_end)
     *
     * Under hotel check-in/check-out semantics (Check-out at 12:00, Check-in at
     * 14:00),
     * a check-out on the same date as a subsequent check-in (A_end == B_start)
     * does NOT overlap and is permitted as a same-day turnover.
     *
     * @param requestedCheckIn  Requested check-in date
     * @param requestedCheckOut Requested check-out date
     * @param existingCheckIn   Existing reservation check-in date
     * @param existingCheckOut  Existing reservation check-out date
     * @return true if dates overlap; false otherwise
     */
    private boolean hasDateOverlap(LocalDate requestedCheckIn, LocalDate requestedCheckOut,
            LocalDate existingCheckIn, LocalDate existingCheckOut) {
        if (requestedCheckIn == null || requestedCheckOut == null
                || existingCheckIn == null || existingCheckOut == null) {
            return false;
        }
        return requestedCheckIn.isBefore(existingCheckOut) && existingCheckIn.isBefore(requestedCheckOut);
    }

    public RoomRepository getRoomRepository() {
        return roomRepository;
    }

    public ReservationRepository getReservationRepository() {
        return reservationRepository;
    }

    public GuestRepository getGuestRepository() {
        return guestRepository;
    }

    public Clock getClock() {
        return clock;
    }
}
