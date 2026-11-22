package lk.smartstay;

import lk.smartstay.enums.BedType;
import lk.smartstay.enums.ReservationStatus;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Guest;
import lk.smartstay.model.Reservation;
import lk.smartstay.model.Room;
import lk.smartstay.repository.ReservationRepository;
import lk.smartstay.repository.RoomRepository;
import lk.smartstay.service.ReservationService;

import java.time.LocalDate;
import java.util.List;

public class Phase6VerificationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Phase 06 Verification Tests");
        System.out.println("==================================================");

        test1_roomWithoutReservationsIsAvailable();
        test2_roomWithOverlappingReservationIsUnavailable();
        test3_roomWithReservationsOnOtherDatesIsAvailable();
        test4_sameDayCheckoutAndNewCheckInIsAllowed();
        test5_underMaintenanceRoomIsUnavailable();
        test6_reservedAdministrativeStatusRoomIsUnavailable();
        test7_cancelledReservationDoesNotBlockAvailability();
        test8_invalidCheckOutDateIsRejected();
        test9_searchResultsMatchSelectedRoomType();
        test10_algorithmExamples();
        test11_nullValidations();
        test12_pastCheckInDateValidation();

        System.out.println("==================================================");
        System.out.println("Test Summary: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
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

    private static void test1_roomWithoutReservationsIsAvailable() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room r1 = new Room("R101", RoomType.STANDARD, BedType.SINGLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);

        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = LocalDate.now().plusDays(5);

        List<Room> available = service.searchAvailableRooms(RoomType.STANDARD, checkIn, checkOut);
        assertTrue("Test 1: Room without reservations is available", available.contains(r1) && available.size() == 1);
        assertTrue("Test 1: isRoomAvailableForDates returns true", service.isRoomAvailableForDates(r1, checkIn, checkOut));
    }

    private static void test2_roomWithOverlappingReservationIsUnavailable() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room r1 = new Room("R102", RoomType.DELUXE, BedType.DOUBLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);

        Guest guest = new Guest("GST001", "123456789V", "John Doe", "0771234567", "john@example.com");
        LocalDate resIn = LocalDate.now().plusDays(5);
        LocalDate resOut = LocalDate.now().plusDays(10);
        Reservation reservation = new Reservation("BKG001", guest, r1, 2, resIn, resOut);
        resRepo.addReservation(reservation);

        LocalDate reqIn = LocalDate.now().plusDays(7);
        LocalDate reqOut = LocalDate.now().plusDays(12);

        List<Room> available = service.searchAvailableRooms(RoomType.DELUXE, reqIn, reqOut);
        assertTrue("Test 2: Room with overlapping reservation is excluded from search", !available.contains(r1));
        assertTrue("Test 2: isRoomAvailableForDates returns false for overlapping dates", !service.isRoomAvailableForDates(r1, reqIn, reqOut));
    }

    private static void test3_roomWithReservationsOnOtherDatesIsAvailable() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room r1 = new Room("R103", RoomType.SUITE, BedType.KING, 4, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);

        Guest guest = new Guest("GST002", "987654321V", "Jane Smith", "0719876543", "jane@example.com");
        LocalDate resIn = LocalDate.now().plusDays(15);
        LocalDate resOut = LocalDate.now().plusDays(20);
        Reservation reservation = new Reservation("BKG002", guest, r1, 2, resIn, resOut);
        resRepo.addReservation(reservation);

        LocalDate reqIn = LocalDate.now().plusDays(5);
        LocalDate reqOut = LocalDate.now().plusDays(10);

        List<Room> available = service.searchAvailableRooms(RoomType.SUITE, reqIn, reqOut);
        assertTrue("Test 3: Room with reservation on other dates is available", available.contains(r1));
        assertTrue("Test 3: isRoomAvailableForDates returns true", service.isRoomAvailableForDates(r1, reqIn, reqOut));
    }

    private static void test4_sameDayCheckoutAndNewCheckInIsAllowed() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room r1 = new Room("R104", RoomType.STANDARD, BedType.SINGLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);

        Guest guest = new Guest("GST003", "555666777V", "Alice Bob", "0723456789", "alice@example.com");
        LocalDate resIn = LocalDate.now().plusDays(5);
        LocalDate resOut = LocalDate.now().plusDays(10);
        Reservation reservation = new Reservation("BKG003", guest, r1, 2, resIn, resOut);
        resRepo.addReservation(reservation);

        LocalDate reqIn = LocalDate.now().plusDays(10);
        LocalDate reqOut = LocalDate.now().plusDays(15);

        List<Room> availableAfter = service.searchAvailableRooms(RoomType.STANDARD, reqIn, reqOut);
        assertTrue("Test 4: Same-day checkout and check-in (after) is allowed", availableAfter.contains(r1));

        LocalDate reqIn2 = LocalDate.now().plusDays(1);
        LocalDate reqOut2 = LocalDate.now().plusDays(5);

        List<Room> availableBefore = service.searchAvailableRooms(RoomType.STANDARD, reqIn2, reqOut2);
        assertTrue("Test 4: Same-day check-in and checkout (before) is allowed", availableBefore.contains(r1));
    }

    private static void test5_underMaintenanceRoomIsUnavailable() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room r1 = new Room("R105", RoomType.DELUXE, BedType.DOUBLE, 2, RoomStatus.UNDER_MAINTENANCE);
        roomRepo.addRoom(r1);

        LocalDate reqIn = LocalDate.now().plusDays(3);
        LocalDate reqOut = LocalDate.now().plusDays(7);

        List<Room> available = service.searchAvailableRooms(RoomType.DELUXE, reqIn, reqOut);
        assertTrue("Test 5: UNDER_MAINTENANCE room is excluded from search results", !available.contains(r1));
        assertTrue("Test 5: isRoomAvailableForDates returns false for UNDER_MAINTENANCE", !service.isRoomAvailableForDates(r1, reqIn, reqOut));
    }

    private static void test6_reservedAdministrativeStatusRoomIsUnavailable() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room r1 = new Room("R106", RoomType.FAMILY_SUITE, BedType.KING, 5, RoomStatus.RESERVED);
        roomRepo.addRoom(r1);

        LocalDate reqIn = LocalDate.now().plusDays(3);
        LocalDate reqOut = LocalDate.now().plusDays(7);

        List<Room> available = service.searchAvailableRooms(RoomType.FAMILY_SUITE, reqIn, reqOut);
        assertTrue("Test 6: RESERVED administrative status room is excluded", !available.contains(r1));
        assertTrue("Test 6: isRoomAvailableForDates returns false for RESERVED room", !service.isRoomAvailableForDates(r1, reqIn, reqOut));
    }

    private static void test7_cancelledReservationDoesNotBlockAvailability() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room r1 = new Room("R107", RoomType.FAMILY_SUITE, BedType.KING, 4, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);

        Guest guest = new Guest("GST004", "111222333V", "Tom Hardy", "0771112223", "tom@example.com");
        LocalDate resIn = LocalDate.now().plusDays(5);
        LocalDate resOut = LocalDate.now().plusDays(10);
        Reservation cancelledRes = new Reservation("BKG004", guest, r1, 2, resIn, resOut,
                Reservation.DEFAULT_BOOKING_ADVANCE, ReservationStatus.CANCELLED);
        resRepo.addReservation(cancelledRes);

        List<Room> available = service.searchAvailableRooms(RoomType.FAMILY_SUITE, resIn, resOut);
        assertTrue("Test 7: CANCELLED reservation does not block room search", available.contains(r1));
        assertTrue("Test 7: isRoomAvailableForDates returns true when reservation is CANCELLED", service.isRoomAvailableForDates(r1, resIn, resOut));
    }

    private static void test8_invalidCheckOutDateIsRejected() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room r1 = new Room("R108", RoomType.STANDARD, BedType.SINGLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);

        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate sameDate = checkIn;
        LocalDate earlierDate = checkIn.minusDays(1);

        boolean sameDateRejected = false;
        try {
            service.searchAvailableRooms(RoomType.STANDARD, checkIn, sameDate);
        } catch (IllegalArgumentException ex) {
            sameDateRejected = true;
        }
        assertTrue("Test 8: checkOut == checkIn is rejected with IllegalArgumentException", sameDateRejected);

        boolean earlierDateRejected = false;
        try {
            service.searchAvailableRooms(RoomType.STANDARD, checkIn, earlierDate);
        } catch (IllegalArgumentException ex) {
            earlierDateRejected = true;
        }
        assertTrue("Test 8: checkOut < checkIn is rejected with IllegalArgumentException", earlierDateRejected);

        assertTrue("Test 8: isRoomAvailableForDates returns false for invalid checkout",
                !service.isRoomAvailableForDates(r1, checkIn, earlierDate));
    }

    private static void test9_searchResultsMatchSelectedRoomType() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room standardRoom = new Room("101", RoomType.STANDARD, BedType.SINGLE, 2, RoomStatus.AVAILABLE);
        Room deluxeRoom = new Room("102", RoomType.DELUXE, BedType.DOUBLE, 2, RoomStatus.AVAILABLE);
        Room suiteRoom = new Room("103", RoomType.SUITE, BedType.KING, 4, RoomStatus.AVAILABLE);

        roomRepo.addRoom(standardRoom);
        roomRepo.addRoom(deluxeRoom);
        roomRepo.addRoom(suiteRoom);

        LocalDate checkIn = LocalDate.now().plusDays(3);
        LocalDate checkOut = LocalDate.now().plusDays(7);

        List<Room> standardResults = service.searchAvailableRooms(RoomType.STANDARD, checkIn, checkOut);
        assertEquals("Test 9: Exactly 1 standard room found", 1, standardResults.size());
        assertEquals("Test 9: Standard room ID matches", "101", standardResults.get(0).getRoomId());

        List<Room> deluxeResults = service.searchAvailableRooms(RoomType.DELUXE, checkIn, checkOut);
        assertEquals("Test 9: Exactly 1 deluxe room found", 1, deluxeResults.size());
        assertEquals("Test 9: Deluxe room ID matches", "102", deluxeResults.get(0).getRoomId());

        List<Room> suiteResults = service.searchAvailableRooms(RoomType.SUITE, checkIn, checkOut);
        assertEquals("Test 9: Exactly 1 suite room found", 1, suiteResults.size());
        assertEquals("Test 9: Suite room ID matches", "103", suiteResults.get(0).getRoomId());

        List<Room> familyResults = service.searchAvailableRooms(RoomType.FAMILY_SUITE, checkIn, checkOut);
        assertEquals("Test 9: 0 family suites found", 0, familyResults.size());
    }

    private static void test10_algorithmExamples() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room testRoom = new Room("TEST-ALG", RoomType.DELUXE, BedType.DOUBLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(testRoom);

        Guest guest = new Guest("GST999", "777888999V", "Alg Tester", "0770001122", "alg@example.com");
        LocalDate exIn = LocalDate.of(2026, 11, 10);
        LocalDate exOut = LocalDate.of(2026, 11, 15);
        resRepo.addReservation(new Reservation("BKG-ALG", guest, testRoom, 2, exIn, exOut));

        boolean case1 = service.isRoomAvailableForDates(testRoom, LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 16));
        assertTrue("Test 10 (Case 1): 2026-11-12 to 2026-11-16 is unavailable (false)", !case1);

        boolean case2 = service.isRoomAvailableForDates(testRoom, LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18));
        assertTrue("Test 10 (Case 2): 2026-11-15 to 2026-11-18 is available (true)", case2);

        boolean case3 = service.isRoomAvailableForDates(testRoom, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 10));
        assertTrue("Test 10 (Case 3): 2026-11-05 to 2026-11-10 is available (true)", case3);

        boolean case4 = service.isRoomAvailableForDates(testRoom, LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 12));
        assertTrue("Test 10 (Case 4): 2026-11-11 to 2026-11-12 is unavailable (false)", !case4);
    }

    private static void test11_nullValidations() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        boolean nullTypeCaught = false;
        try {
            service.searchAvailableRooms(null, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        } catch (IllegalArgumentException ex) {
            nullTypeCaught = true;
        }
        assertTrue("Test 11: Null room type throws IllegalArgumentException", nullTypeCaught);

        boolean nullDatesCaught = false;
        try {
            service.searchAvailableRooms(RoomType.STANDARD, null, null);
        } catch (IllegalArgumentException ex) {
            nullDatesCaught = true;
        }
        assertTrue("Test 11: Null dates throw IllegalArgumentException", nullDatesCaught);
    }

    private static void test12_pastCheckInDateValidation() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        boolean pastCheckInCaught = false;
        try {
            service.searchAvailableRooms(RoomType.STANDARD, LocalDate.now().minusDays(1), LocalDate.now().plusDays(2));
        } catch (IllegalArgumentException ex) {
            pastCheckInCaught = true;
        }
        assertTrue("Test 12: Past check-in date throws IllegalArgumentException", pastCheckInCaught);
    }
}
