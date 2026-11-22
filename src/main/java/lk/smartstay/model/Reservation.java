package lk.smartstay.model;

import lk.smartstay.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Reservation {

    public static final double DEFAULT_BOOKING_ADVANCE = 10000.0;
    public static final int LONG_STAY_DISCOUNT_THRESHOLD_NIGHTS = 5;

    private final String bookingId;
    private final Guest guest;
    private Room room;
    private int numberOfGuests;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double bookingAdvance;
    private ReservationStatus status;

    public Reservation(String bookingId, Guest guest, Room room, int numberOfGuests,
                       LocalDate checkInDate, LocalDate checkOutDate) {
        this(bookingId, guest, room, numberOfGuests, checkInDate, checkOutDate,
                DEFAULT_BOOKING_ADVANCE, ReservationStatus.CONFIRMED);
    }

    public Reservation(String bookingId, Guest guest, Room room, int numberOfGuests,
                       LocalDate checkInDate, LocalDate checkOutDate,
                       double bookingAdvance, ReservationStatus status) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be null or empty.");
        }
        if (guest == null) {
            throw new IllegalArgumentException("Guest cannot be null.");
        }
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }
        if (numberOfGuests <= 0) {
            throw new IllegalArgumentException("Number of guests must be greater than zero.");
        }
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Check-in and check-out dates cannot be null.");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must logically be after check-in date.");
        }
        if (bookingAdvance < 0) {
            throw new IllegalArgumentException("Booking advance cannot be negative.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Reservation status cannot be null.");
        }

        this.bookingId = bookingId.trim();
        this.guest = guest;
        this.room = room;
        this.numberOfGuests = numberOfGuests;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingAdvance = bookingAdvance;
        this.status = status;
    }

    public long calculateNumberOfNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public boolean qualifiesForLongStayDiscount() {
        return calculateNumberOfNights() >= LONG_STAY_DISCOUNT_THRESHOLD_NIGHTS;
    }

    public boolean isCancelled() {
        return this.status == ReservationStatus.CANCELLED;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }
        this.room = room;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        if (numberOfGuests <= 0) {
            throw new IllegalArgumentException("Number of guests must be greater than zero.");
        }
        this.numberOfGuests = numberOfGuests;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        if (checkInDate == null) {
            throw new IllegalArgumentException("Check-in date cannot be null.");
        }
        if (this.checkOutDate != null && !this.checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date.");
        }
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        if (checkOutDate == null) {
            throw new IllegalArgumentException("Check-out date cannot be null.");
        }
        if (this.checkInDate != null && !checkOutDate.isAfter(this.checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
        this.checkOutDate = checkOutDate;
    }

    public double getBookingAdvance() {
        return bookingAdvance;
    }

    public void setBookingAdvance(double bookingAdvance) {
        if (bookingAdvance < 0) {
            throw new IllegalArgumentException("Booking advance cannot be negative.");
        }
        this.bookingAdvance = bookingAdvance;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Reservation status cannot be null.");
        }
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return Objects.equals(bookingId, that.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }

    @Override
    public String toString() {
        return "Booking " + bookingId + " - " + guest.getFullName() + " (" + room.getRoomId() + ", "
                + calculateNumberOfNights() + " nights, " + status.getDisplayName() + ")";
    }
}
