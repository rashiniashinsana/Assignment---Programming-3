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
import lk.smartstay.service.RoomService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class Phase7ReservationWorkflowTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-11-13T04:30:00Z"),
            COLOMBO_ZONE
    );

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Phase 07 Reservation Workflow Verification Tests");
        System.out.println("==================================================");

        test1_createValidBookingForRegisteredGuest();
        test2_confirmBookingIdIsGeneratedAndUnique();
        test3_confirmAdvanceIsLKR10000();
        test4_rejectBookingForTomorrow();
        test5_allowBookingExactlyTwoCalendarDaysAhead();
        test6_rejectCheckOutEarlierThanOrEqualToCheckIn();
        test7_rejectZeroOrNegativeGuests();
        test8_rejectRoomUnderMaintenance();
        test9_rejectOverlappingReservation();
        test10_allowBookingSameRoomOnNonOverlappingDates();
        test11_checkBookingAppearsInReservationRepository();
        test12_roomAdministrativeStatusRemainsAvailableAfterBooking();
        test13_rejectUnregisteredGuestOrMissingRoom();
        test14_safeguardAgainstDeletingRoomWithReservationHistory();
        test15_getAllReservationsReflectsStoredRecords();

        System.out.println("==================================================");
        System.out.println("Phase 07 Test Summary: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
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

    private static TestContext createTestContext() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();
        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);
        RoomService roomService = new RoomService(roomRepo, resRepo);

        Guest guest = new Guest("GST001", "123456789V", "Kamal Perera", "0771234567", "kamal@example.com");
        guestRepo.addGuest(guest);

        Room room = new Room("101", RoomType.DELUXE, BedType.DOUBLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(room);

        return new TestContext(roomRepo, resRepo, guestRepo, resService, roomService, guest, room);
    }

    private record TestContext(
            RoomRepository roomRepo,
            ReservationRepository resRepo,
            GuestRepository guestRepo,
            ReservationService resService,
            RoomService roomService,
            Guest guest,
            Room room
    ) {}

    private static void test1_createValidBookingForRegisteredGuest() {
        TestContext ctx = createTestContext();

        LocalDate inDate = LocalDate.of(2026, 11, 15);
        LocalDate outDate = LocalDate.of(2026, 11, 18);

        Reservation res = ctx.resService.createReservation(ctx.guest, ctx.room, 2, inDate, outDate);

        assertTrue("Test 1: Reservation object returned non-null", res != null);
        assertEquals("Test 1: Guest associated with booking", ctx.guest, res.getGuest());
        assertEquals("Test 1: Room associated with booking", ctx.room, res.getRoom());
        assertEquals("Test 1: Number of guests matches", 2, res.getNumberOfGuests());
        assertEquals("Test 1: Check-in date matches", inDate, res.getCheckInDate());
        assertEquals("Test 1: Check-out date matches", outDate, res.getCheckOutDate());
        assertEquals("Test 1: Status is CONFIRMED", ReservationStatus.CONFIRMED, res.getStatus());
    }

    private static void test2_confirmBookingIdIsGeneratedAndUnique() {
        TestContext ctx = createTestContext();
        LocalDate inDate1 = LocalDate.of(2026, 11, 15);
        LocalDate outDate1 = LocalDate.of(2026, 11, 18);
        Reservation res1 = ctx.resService.createReservation(ctx.guest, ctx.room, 2, inDate1, outDate1);

        assertTrue("Test 2: Booking ID is non-null and not empty", res1.getBookingId() != null && !res1.getBookingId().isEmpty());
        assertTrue("Test 2: Booking ID matches BKG format", res1.getBookingId().startsWith("BKG"));

        Room room2 = new Room("102", RoomType.STANDARD, BedType.SINGLE, 2, RoomStatus.AVAILABLE);
        ctx.roomRepo.addRoom(room2);
        Reservation res2 = ctx.resService.createReservation(ctx.guest, room2, 1, inDate1, outDate1);

        assertTrue("Test 2: Sequential booking IDs are distinct", !res1.getBookingId().equalsIgnoreCase(res2.getBookingId()));
    }

    private static void test3_confirmAdvanceIsLKR10000() {
        TestContext ctx = createTestContext();
        LocalDate inDate = LocalDate.of(2026, 11, 15);
        LocalDate outDate = LocalDate.of(2026, 11, 18);
        Reservation res = ctx.resService.createReservation(ctx.guest, ctx.room, 2, inDate, outDate);

        assertEquals("Test 3: Booking advance is exactly LKR 10,000.00", 10000.0, res.getBookingAdvance());
    }

    private static void test4_rejectBookingForTomorrow() {
        TestContext ctx = createTestContext();

        LocalDate tomorrow = LocalDate.of(2026, 11, 14);
        LocalDate outDate = LocalDate.of(2026, 11, 17);

        assertTrue("Test 4: validateAdvanceBooking returns false for tomorrow", !ctx.resService.validateAdvanceBooking(tomorrow));

        boolean exceptionCaught = false;
        try {
            ctx.resService.createReservation(ctx.guest, ctx.room, 2, tomorrow, outDate);
        } catch (IllegalArgumentException ex) {
            exceptionCaught = true;
        }
        assertTrue("Test 4: createReservation rejects booking for tomorrow", exceptionCaught);

        LocalDate today = LocalDate.of(2026, 11, 13);
        assertTrue("Test 4: validateAdvanceBooking returns false for today", !ctx.resService.validateAdvanceBooking(today));
    }

    private static void test5_allowBookingExactlyTwoCalendarDaysAhead() {
        TestContext ctx = createTestContext();

        LocalDate exactlyTwoDays = LocalDate.of(2026, 11, 15);
        LocalDate outDate = LocalDate.of(2026, 11, 17);

        assertTrue("Test 5: validateAdvanceBooking returns true for exactly 2 days ahead",
                ctx.resService.validateAdvanceBooking(exactlyTwoDays));

        Reservation res = ctx.resService.createReservation(ctx.guest, ctx.room, 1, exactlyTwoDays, outDate);
        assertTrue("Test 5: Booking permitted exactly two calendar days ahead", res != null);
    }

    private static void test6_rejectCheckOutEarlierThanOrEqualToCheckIn() {
        TestContext ctx = createTestContext();
        LocalDate inDate = LocalDate.of(2026, 11, 15);
        LocalDate sameDate = LocalDate.of(2026, 11, 15);
        LocalDate earlierDate = LocalDate.of(2026, 11, 14);

        boolean sameDateCaught = false;
        try {
            ctx.resService.createReservation(ctx.guest, ctx.room, 2, inDate, sameDate);
        } catch (IllegalArgumentException ex) {
            sameDateCaught = true;
        }
        assertTrue("Test 6: checkOut == checkIn is rejected", sameDateCaught);

        boolean earlierDateCaught = false;
        try {
            ctx.resService.createReservation(ctx.guest, ctx.room, 2, inDate, earlierDate);
        } catch (IllegalArgumentException ex) {
            earlierDateCaught = true;
        }
        assertTrue("Test 6: checkOut < checkIn is rejected", earlierDateCaught);
    }

    private static void test7_rejectZeroOrNegativeGuests() {
        TestContext ctx = createTestContext();
        LocalDate inDate = LocalDate.of(2026, 11, 15);
        LocalDate outDate = LocalDate.of(2026, 11, 18);

        boolean zeroGuestsCaught = false;
        try {
            ctx.resService.createReservation(ctx.guest, ctx.room, 0, inDate, outDate);
        } catch (IllegalArgumentException ex) {
            zeroGuestsCaught = true;
        }
        assertTrue("Test 7: 0 guests is rejected", zeroGuestsCaught);

        boolean negativeGuestsCaught = false;
        try {
            ctx.resService.createReservation(ctx.guest, ctx.room, -2, inDate, outDate);
        } catch (IllegalArgumentException ex) {
            negativeGuestsCaught = true;
        }
        assertTrue("Test 7: negative guests is rejected", negativeGuestsCaught);
    }

    private static void test8_rejectRoomUnderMaintenance() {
        TestContext ctx = createTestContext();
        Room maintenanceRoom = new Room("103", RoomType.SUITE, BedType.KING, 4, RoomStatus.UNDER_MAINTENANCE);
        ctx.roomRepo.addRoom(maintenanceRoom);

        LocalDate inDate = LocalDate.of(2026, 11, 16);
        LocalDate outDate = LocalDate.of(2026, 11, 20);

        boolean maintenanceCaught = false;
        try {
            ctx.resService.createReservation(ctx.guest, maintenanceRoom, 2, inDate, outDate);
        } catch (IllegalStateException ex) {
            maintenanceCaught = true;
        }
        assertTrue("Test 8: Room UNDER_MAINTENANCE is rejected with IllegalStateException", maintenanceCaught);
    }

    private static void test9_rejectOverlappingReservation() {
        TestContext ctx = createTestContext();

        LocalDate inDate = LocalDate.of(2026, 11, 15);
        LocalDate outDate = LocalDate.of(2026, 11, 20);
        ctx.resService.createReservation(ctx.guest, ctx.room, 2, inDate, outDate);

        LocalDate overlapIn = LocalDate.of(2026, 11, 18);
        LocalDate overlapOut = LocalDate.of(2026, 11, 23);

        boolean overlapCaught = false;
        try {
            ctx.resService.createReservation(ctx.guest, ctx.room, 2, overlapIn, overlapOut);
        } catch (IllegalStateException ex) {
            overlapCaught = true;
        }
        assertTrue("Test 9: Overlapping booking on same room is rejected with IllegalStateException", overlapCaught);
    }

    private static void test10_allowBookingSameRoomOnNonOverlappingDates() {
        TestContext ctx = createTestContext();

        LocalDate inDate1 = LocalDate.of(2026, 11, 15);
        LocalDate outDate1 = LocalDate.of(2026, 11, 20);
        Reservation res1 = ctx.resService.createReservation(ctx.guest, ctx.room, 2, inDate1, outDate1);

        LocalDate inDate2 = LocalDate.of(2026, 11, 20);
        LocalDate outDate2 = LocalDate.of(2026, 11, 25);
        Reservation res2 = ctx.resService.createReservation(ctx.guest, ctx.room, 2, inDate2, outDate2);

        assertTrue("Test 10: First reservation confirmed", res1 != null);
        assertTrue("Test 10: Second non-overlapping reservation on same room confirmed", res2 != null);
        assertEquals("Test 10: Repository holds both bookings", 2, ctx.resRepo.findByRoomId(ctx.room.getRoomId()).size());
    }

    private static void test11_checkBookingAppearsInReservationRepository() {
        TestContext ctx = createTestContext();
        LocalDate inDate = LocalDate.of(2026, 11, 16);
        LocalDate outDate = LocalDate.of(2026, 11, 19);
        Reservation res = ctx.resService.createReservation(ctx.guest, ctx.room, 2, inDate, outDate);

        Reservation retrieved = ctx.resRepo.findByBookingId(res.getBookingId());
        assertTrue("Test 11: Booking retrieved from ReservationRepository by ID", retrieved != null);
        assertEquals("Test 11: Retrieved Booking ID matches", res.getBookingId(), retrieved.getBookingId());
        assertEquals("Test 11: Retrieved Guest ID matches", ctx.guest.getGuestId(), retrieved.getGuest().getGuestId());
    }

    private static void test12_roomAdministrativeStatusRemainsAvailableAfterBooking() {
        TestContext ctx = createTestContext();
        LocalDate inDate = LocalDate.of(2026, 11, 16);
        LocalDate outDate = LocalDate.of(2026, 11, 19);
        ctx.resService.createReservation(ctx.guest, ctx.room, 2, inDate, outDate);

        assertEquals("Test 12: Room.status is NOT changed globally to RESERVED", RoomStatus.AVAILABLE, ctx.room.getStatus());
        assertTrue("Test 12: Room.isAvailable() remains true for admin inventory", ctx.room.isAvailable());
    }

    private static void test13_rejectUnregisteredGuestOrMissingRoom() {
        TestContext ctx = createTestContext();
        Guest unregisteredGuest = new Guest("GST999", "999888777V", "Ghost User", "0770000000", "ghost@example.com");
        LocalDate inDate = LocalDate.of(2026, 11, 16);
        LocalDate outDate = LocalDate.of(2026, 11, 19);

        boolean unregisteredGuestCaught = false;
        try {
            ctx.resService.createReservation(unregisteredGuest, ctx.room, 1, inDate, outDate);
        } catch (IllegalArgumentException ex) {
            unregisteredGuestCaught = true;
        }
        assertTrue("Test 13: Unregistered guest is rejected", unregisteredGuestCaught);

        Room unmanagedRoom = new Room("999", RoomType.STANDARD, BedType.SINGLE, 2, RoomStatus.AVAILABLE);
        boolean unmanagedRoomCaught = false;
        try {
            ctx.resService.createReservation(ctx.guest, unmanagedRoom, 1, inDate, outDate);
        } catch (IllegalArgumentException ex) {
            unmanagedRoomCaught = true;
        }
        assertTrue("Test 13: Unmanaged room is rejected", unmanagedRoomCaught);
    }

    private static void test14_safeguardAgainstDeletingRoomWithReservationHistory() {
        TestContext ctx = createTestContext();
        LocalDate inDate = LocalDate.of(2026, 11, 16);
        LocalDate outDate = LocalDate.of(2026, 11, 19);
        ctx.resService.createReservation(ctx.guest, ctx.room, 2, inDate, outDate);

        boolean roomDeleted = ctx.roomService.removeRoom("101");
        assertTrue("Test 14: RoomService prevents deleting room with reservation history", !roomDeleted);
        assertTrue("Test 14: Room 101 still exists in RoomRepository", ctx.roomRepo.existsById("101"));

        Room emptyRoom = new Room("201", RoomType.STANDARD, BedType.SINGLE, 2, RoomStatus.AVAILABLE);
        ctx.roomRepo.addRoom(emptyRoom);
        boolean emptyDeleted = ctx.roomService.removeRoom("201");
        assertTrue("Test 14: Room without reservations can be removed normally", emptyDeleted);
    }

    private static void test15_getAllReservationsReflectsStoredRecords() {
        TestContext ctx = createTestContext();
        assertEquals("Test 15: Initially 0 reservations", 0, ctx.resService.getAllReservations().size());

        ctx.resService.createReservation(ctx.guest, ctx.room, 2, LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18));
        assertEquals("Test 15: 1 reservation retrieved via getAllReservations()", 1, ctx.resService.getAllReservations().size());
    }
}
