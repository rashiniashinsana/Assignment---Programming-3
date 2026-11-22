package lk.smartstay;

import lk.smartstay.enums.BedType;
import lk.smartstay.enums.ReservationStatus;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Guest;
import lk.smartstay.model.Reservation;
import lk.smartstay.model.Room;
import lk.smartstay.repository.GuestRepository;
import lk.smartstay.repository.ReservationRepository;
import lk.smartstay.repository.RoomRepository;
import lk.smartstay.service.ReservationService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class Phase9CancellationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Phase 09 Reservation Cancellation & 24h Rule Tests");
        System.out.println("==================================================");

        test1_canCancelWellInAdvance();
        test2_boundaryExact24HoursBeforeCheckIn();
        test3_boundaryLessThan24HoursBeforeCheckIn();
        test4_cannotCancelAfterCheckInTime();
        test5_cannotCancelAlreadyCancelledReservation();
        test6_cannotCancelCompletedReservation();
        test7_cannotCancelNullOrInvalidReservation();
        test8_cancelReservationUnknownIdReturnsFalse();
        test9_cancelReservationSuccessUpdatesStatusAndPersists();
        test10_reservationHistoryPreservedNotDeleted();
        test11_cannotCancelSameReservationTwice();
        test12_roomAvailabilityRestoredAfterCancellation();
        test13_roomAdministrativeStatusIsolated();
        test14_refundAmountTracking();
        test15_cancellationIneligibilityReasons();

        System.out.println("==================================================");
        System.out.println("Phase 09 Test Summary: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
        System.out.println("==================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName);
            testsFailed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName + " - Expected: " + expected + ", Actual: " + actual);
            testsFailed++;
        }
    }

    private static ReservationService createServiceWithClock(String isoInstant) {
        Clock fixedClock = Clock.fixed(Instant.parse(isoInstant), COLOMBO_ZONE);
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();
        return new ReservationService(roomRepo, resRepo, guestRepo, fixedClock);
    }

    private static void test1_canCancelWellInAdvance() {

        ReservationService service = createServiceWithClock("2026-11-15T04:30:00Z");
        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Room room = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG001", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));

        assertTrue("Test 1: Can cancel 5 days before check-in", service.canCancel(res));
    }

    private static void test2_boundaryExact24HoursBeforeCheckIn() {
        ReservationService service = createServiceWithClock("2026-11-19T08:30:00Z");
        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Room room = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG002", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));

        assertTrue("Test 2: Exactly 24 hours before 14:00 check-in is permitted", service.canCancel(res));
    }

    private static void test3_boundaryLessThan24HoursBeforeCheckIn() {
        ReservationService service = createServiceWithClock("2026-11-19T08:31:00Z");
        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Room room = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG003", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));

        assertTrue("Test 3: 23 hours 59 minutes before check-in is rejected", !service.canCancel(res));
    }

    private static void test4_cannotCancelAfterCheckInTime() {
        ReservationService service = createServiceWithClock("2026-11-20T09:30:00Z");
        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Room room = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG004", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));

        assertTrue("Test 4: Post check-in cancellation rejected", !service.canCancel(res));
    }

    private static void test5_cannotCancelAlreadyCancelledReservation() {
        ReservationService service = createServiceWithClock("2026-11-15T04:30:00Z");
        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Room room = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG005", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23),
                10000.0, ReservationStatus.CANCELLED);

        assertTrue("Test 5: Already cancelled reservation cannot be cancelled", !service.canCancel(res));
    }

    private static void test6_cannotCancelCompletedReservation() {
        ReservationService service = createServiceWithClock("2026-11-15T04:30:00Z");
        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Room room = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG006", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23),
                10000.0, ReservationStatus.COMPLETED);

        assertTrue("Test 6: Completed reservation cannot be cancelled", !service.canCancel(res));
    }

    private static void test7_cannotCancelNullOrInvalidReservation() {
        ReservationService service = createServiceWithClock("2026-11-15T04:30:00Z");
        assertTrue("Test 7: Null reservation returns false", !service.canCancel(null));
    }

    private static void test8_cancelReservationUnknownIdReturnsFalse() {
        ReservationService service = createServiceWithClock("2026-11-15T04:30:00Z");
        assertTrue("Test 8a: Null bookingId returns false", !service.cancelReservation(null));
        assertTrue("Test 8b: Empty bookingId returns false", !service.cancelReservation("   "));
        assertTrue("Test 8c: Unknown bookingId returns false", !service.cancelReservation("BKG-NONEXISTENT"));
    }

    private static void test9_cancelReservationSuccessUpdatesStatusAndPersists() {
        ReservationService service = createServiceWithClock("2026-11-15T04:30:00Z");
        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Room room = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG009", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));
        service.getReservationRepository().addReservation(res);

        boolean success = service.cancelReservation("BKG009");
        assertTrue("Test 9a: cancelReservation returns true", success);

        Reservation persisted = service.getReservationRepository().findByBookingId("BKG009");
        assertEquals("Test 9b: Persisted status is CANCELLED", ReservationStatus.CANCELLED, persisted.getStatus());
        assertTrue("Test 9c: isCancelled() helper is true", persisted.isCancelled());
    }

    private static void test10_reservationHistoryPreservedNotDeleted() {
        ReservationService service = createServiceWithClock("2026-11-15T04:30:00Z");
        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Room room = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG010", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));
        service.getReservationRepository().addReservation(res);

        service.cancelReservation("BKG010");

        assertEquals("Test 10a: Repository total size is 1", 1, service.getReservationRepository().findAll().size());
        assertTrue("Test 10b: Can still lookup by bookingId", service.searchByBookingId("BKG010") != null);
        assertEquals("Test 10c: Search results include cancelled reservation", 1, service.searchByGuestName("Amal").size());
    }

    private static void test11_cannotCancelSameReservationTwice() {
        ReservationService service = createServiceWithClock("2026-11-15T04:30:00Z");
        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Room room = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG011", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));
        service.getReservationRepository().addReservation(res);

        assertTrue("Test 11a: First cancellation succeeds", service.cancelReservation("BKG011"));
        assertTrue("Test 11b: Second cancellation is rejected", !service.cancelReservation("BKG011"));
    }

    private static void test12_roomAvailabilityRestoredAfterCancellation() {
        ReservationService service = createServiceWithClock("2026-11-15T04:30:00Z");
        Room room = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        service.getRoomRepository().addRoom(room);

        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        service.getGuestRepository().addGuest(guest);

        LocalDate checkIn = LocalDate.of(2026, 11, 20);
        LocalDate checkOut = LocalDate.of(2026, 11, 25);

        List<Room> availableBefore = service.searchAvailableRooms(RoomType.DELUXE, checkIn, checkOut);
        assertEquals("Test 12a: Room available before booking", 1, availableBefore.size());

        Reservation res = service.createReservation(guest, room, 2, checkIn, checkOut);
        assertEquals("Test 12b: Booking created with CONFIRMED status", ReservationStatus.CONFIRMED, res.getStatus());

        List<Room> availableDuring = service.searchAvailableRooms(RoomType.DELUXE, checkIn, checkOut);
        assertEquals("Test 12c: Room not available while booked", 0, availableDuring.size());

        boolean cancelled = service.cancelReservation(res.getBookingId());
        assertTrue("Test 12d: Cancellation succeeds", cancelled);

        List<Room> availableAfter = service.searchAvailableRooms(RoomType.DELUXE, checkIn, checkOut);
        assertEquals("Test 12e: Room restored and searchable again for same dates", 1, availableAfter.size());
        assertEquals("Test 12f: Restored room is R101", "R101", availableAfter.get(0).getRoomId());
    }

    private static void test13_roomAdministrativeStatusIsolated() {
        ReservationService service = createServiceWithClock("2026-11-15T04:30:00Z");
        Room room = new Room("R201", RoomType.SUITE, BedType.QUEEN, 2, RoomStatus.AVAILABLE);
        service.getRoomRepository().addRoom(room);

        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        service.getGuestRepository().addGuest(guest);

        Reservation res = service.createReservation(guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 24));

        room.setStatus(RoomStatus.UNDER_MAINTENANCE);
        service.getRoomRepository().updateRoom(room);

        service.cancelReservation(res.getBookingId());

        Room currentRoom = service.getRoomRepository().findById("R201");
        assertEquals("Test 13a: Room remains UNDER_MAINTENANCE after cancellation",
                RoomStatus.UNDER_MAINTENANCE, currentRoom.getStatus());

        List<Room> available = service.searchAvailableRooms(RoomType.SUITE, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 24));
        assertEquals("Test 13b: Under maintenance room is excluded from availability search", 0, available.size());
    }

    private static void test14_refundAmountTracking() {
        ReservationService service = createServiceWithClock("2026-11-15T04:30:00Z");
        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Room room = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG014", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));

        assertEquals("Test 14a: Refund amount matches recorded advance", 10000.0, service.getRefundAmount(res));
        assertEquals("Test 14b: Null reservation refund is 0.0", 0.0, service.getRefundAmount(null));
    }

    private static void test15_cancellationIneligibilityReasons() {
        ReservationService service = createServiceWithClock("2026-11-19T10:00:00Z");
        Guest guest = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Room room = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);

        Reservation resWithin24 = new Reservation("BKG015A", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));
        String reasonWithin24 = service.getCancellationIneligibilityReason(resWithin24);
        assertTrue("Test 15a: Ineligibility reason mentions 24 hours",
                reasonWithin24 != null && reasonWithin24.contains("within 24 hours"));

        Reservation resCancelled = new Reservation("BKG015B", guest, room, 2, LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 28),
                10000.0, ReservationStatus.CANCELLED);
        String reasonCancelled = service.getCancellationIneligibilityReason(resCancelled);
        assertTrue("Test 15b: Ineligibility reason mentions already cancelled",
                reasonCancelled != null && reasonCancelled.contains("already cancelled"));

        Reservation resCompleted = new Reservation("BKG015C", guest, room, 2, LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 28),
                10000.0, ReservationStatus.COMPLETED);
        String reasonCompleted = service.getCancellationIneligibilityReason(resCompleted);
        assertTrue("Test 15c: Ineligibility reason mentions completed",
                reasonCompleted != null && reasonCompleted.contains("completed"));

        Reservation resEligible = new Reservation("BKG015D", guest, room, 2, LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 28));
        String reasonEligible = service.getCancellationIneligibilityReason(resEligible);
        assertEquals("Test 15d: Eligible reservation has null ineligibility reason", null, reasonEligible);
    }
}
