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

public class Phase8ReservationSearchTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-11-13T04:30:00Z"),
            COLOMBO_ZONE
    );

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Phase 08 Reservation Search & Filtering Tests");
        System.out.println("==================================================");

        test1_findExistingBookingUsingBookingId();
        test2_searchNonExistentBookingId();
        test3_searchByFullGuestName();
        test4_searchByPartialGuestName();
        test5_searchUsingDifferentLetterCases();
        test6_filterReservationsByCheckInDate();
        test7_searchDateWithNoBookings();
        test8_showAllReservationsReturnsAllSorted();
        test9_selectReservationAndInspectDetails();
        test10_verifyBookingsNotModifiedBySearching();
        test11_cancelledReservationsIncludedInSearchHistory();
        test12_resultsSortedByCheckInDateAndBookingId();

        System.out.println("==================================================");
        System.out.println("Phase 08 Test Summary: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
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

    private static TestFixtures setupFixtures() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);

        Guest g1 = new Guest("GST001", "123456789V", "Kasun Wickramasinghe", "0771234567", "kasun@example.com");
        Guest g2 = new Guest("GST002", "987654321V", "Amara Silva", "0719876543", "amara@example.com");
        Guest g3 = new Guest("GST003", "555666777V", "Sunil Perera", "0725556667", "sunil@example.com");
        guestRepo.addGuest(g1);
        guestRepo.addGuest(g2);
        guestRepo.addGuest(g3);

        Room r101 = new Room("101", RoomType.STANDARD, BedType.SINGLE, 2, RoomStatus.AVAILABLE);
        Room r102 = new Room("102", RoomType.DELUXE, BedType.DOUBLE, 2, RoomStatus.AVAILABLE);
        Room r103 = new Room("103", RoomType.SUITE, BedType.KING, 4, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r101);
        roomRepo.addRoom(r102);
        roomRepo.addRoom(r103);

        Reservation b1 = service.createReservation(g1, r101, 1, LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18));

        Reservation b2 = service.createReservation(g2, r102, 2, LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 20));

        Reservation b3 = service.createReservation(g3, r103, 3, LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 25));

        return new TestFixtures(roomRepo, resRepo, guestRepo, service, b1, b2, b3, g1, g2, g3);
    }

    private record TestFixtures(
            RoomRepository roomRepo,
            ReservationRepository resRepo,
            GuestRepository guestRepo,
            ReservationService service,
            Reservation b1,
            Reservation b2,
            Reservation b3,
            Guest g1,
            Guest g2,
            Guest g3
    ) {}

    private static void test1_findExistingBookingUsingBookingId() {
        TestFixtures f = setupFixtures();
        Reservation found = f.service.searchByBookingId(f.b1.getBookingId());

        assertTrue("Test 1: Booking found by exact ID", found != null);
        assertEquals("Test 1: Booking ID matches", f.b1.getBookingId(), found.getBookingId());
        assertEquals("Test 1: Guest matches", f.g1.getFullName(), found.getGuest().getFullName());
    }

    private static void test2_searchNonExistentBookingId() {
        TestFixtures f = setupFixtures();
        Reservation found = f.service.searchByBookingId("BKG999");
        assertTrue("Test 2: Non-existent Booking ID returns null", found == null);

        Reservation foundBlank = f.service.searchByBookingId("   ");
        assertTrue("Test 2: Blank Booking ID returns null", foundBlank == null);

        Reservation foundNull = f.service.searchByBookingId(null);
        assertTrue("Test 2: Null Booking ID returns null", foundNull == null);
    }

    private static void test3_searchByFullGuestName() {
        TestFixtures f = setupFixtures();
        List<Reservation> results = f.service.searchByGuestName("Kasun Wickramasinghe");

        assertEquals("Test 3: Exactly 1 result for full guest name", 1, results.size());
        assertEquals("Test 3: Result booking ID matches", f.b1.getBookingId(), results.get(0).getBookingId());
    }

    private static void test4_searchByPartialGuestName() {
        TestFixtures f = setupFixtures();

        List<Reservation> results = f.service.searchByGuestName("Wickrama");
        assertEquals("Test 4: Partial substring matches Kasun", 1, results.size());
        assertEquals("Test 4: Result matches BKG001", f.b1.getBookingId(), results.get(0).getBookingId());

        List<Reservation> silvaResults = f.service.searchByGuestName("Silva");
        assertEquals("Test 4: Partial substring matches Silva", 1, silvaResults.size());
        assertEquals("Test 4: Result matches BKG002", f.b2.getBookingId(), silvaResults.get(0).getBookingId());
    }

    private static void test5_searchUsingDifferentLetterCases() {
        TestFixtures f = setupFixtures();

        List<Reservation> lower = f.service.searchByGuestName("kasun");
        assertEquals("Test 5: Lowercase query 'kasun' matches", 1, lower.size());

        List<Reservation> upper = f.service.searchByGuestName("KASUN");
        assertEquals("Test 5: Uppercase query 'KASUN' matches", 1, upper.size());

        List<Reservation> mixed = f.service.searchByGuestName("kAsUn WiCkRaMa");
        assertEquals("Test 5: Mixed case query matches", 1, mixed.size());
    }

    private static void test6_filterReservationsByCheckInDate() {
        TestFixtures f = setupFixtures();

        List<Reservation> nov15Results = f.service.getReservationsByCheckInDate(LocalDate.of(2026, 11, 15));
        assertEquals("Test 6: Exactly 2 bookings on 2026-11-15", 2, nov15Results.size());

        List<Reservation> nov22Results = f.service.getReservationsByCheckInDate(LocalDate.of(2026, 11, 22));
        assertEquals("Test 6: Exactly 1 booking on 2026-11-22", 1, nov22Results.size());
        assertEquals("Test 6: Nov 22 booking ID matches", f.b3.getBookingId(), nov22Results.get(0).getBookingId());
    }

    private static void test7_searchDateWithNoBookings() {
        TestFixtures f = setupFixtures();
        List<Reservation> emptyResults = f.service.getReservationsByCheckInDate(LocalDate.of(2026, 12, 1));
        assertTrue("Test 7: Date with no bookings returns empty list", emptyResults != null && emptyResults.isEmpty());
    }

    private static void test8_showAllReservationsReturnsAllSorted() {
        TestFixtures f = setupFixtures();
        List<Reservation> all = f.service.getAllReservations();
        assertEquals("Test 8: getAllReservations returns all 3 bookings", 3, all.size());
    }

    private static void test9_selectReservationAndInspectDetails() {
        TestFixtures f = setupFixtures();
        Reservation res = f.service.searchByBookingId(f.b2.getBookingId());

        assertTrue("Test 9: Reservation found", res != null);
        assertEquals("Test 9: Guest ID", f.g2.getGuestId(), res.getGuest().getGuestId());
        assertEquals("Test 9: Guest NIC", "987654321V", res.getGuest().getNicOrPassport());
        assertEquals("Test 9: Room ID", "102", res.getRoom().getRoomId());
        assertEquals("Test 9: Room Type", RoomType.DELUXE, res.getRoom().getRoomType());
        assertEquals("Test 9: Check-in", LocalDate.of(2026, 11, 15), res.getCheckInDate());
        assertEquals("Test 9: Check-out", LocalDate.of(2026, 11, 20), res.getCheckOutDate());
        assertEquals("Test 9: Nights", 5L, res.calculateNumberOfNights());
        assertEquals("Test 9: Guests", 2, res.getNumberOfGuests());
        assertEquals("Test 9: Advance", 10000.0, res.getBookingAdvance());
        assertEquals("Test 9: Status", ReservationStatus.CONFIRMED, res.getStatus());
    }

    private static void test10_verifyBookingsNotModifiedBySearching() {
        TestFixtures f = setupFixtures();
        int initialCount = f.resRepo.count();

        f.service.searchByBookingId(f.b1.getBookingId());
        f.service.searchByGuestName("Silva");
        f.service.getReservationsByCheckInDate(LocalDate.of(2026, 11, 15));
        List<Reservation> listCopy = f.service.getAllReservations();

        listCopy.clear();

        assertEquals("Test 10: Repository count unchanged after searches", initialCount, f.resRepo.count());
        assertTrue("Test 10: BKG001 still intact", f.resRepo.findByBookingId(f.b1.getBookingId()) != null);
        assertTrue("Test 10: BKG002 still intact", f.resRepo.findByBookingId(f.b2.getBookingId()) != null);
        assertTrue("Test 10: BKG003 still intact", f.resRepo.findByBookingId(f.b3.getBookingId()) != null);
    }

    private static void test11_cancelledReservationsIncludedInSearchHistory() {
        TestFixtures f = setupFixtures();

        f.b1.setStatus(ReservationStatus.CANCELLED);

        Reservation byId = f.service.searchByBookingId(f.b1.getBookingId());
        assertTrue("Test 11: Cancelled booking found by ID", byId != null);
        assertEquals("Test 11: Status is CANCELLED", ReservationStatus.CANCELLED, byId.getStatus());

        List<Reservation> byName = f.service.searchByGuestName("Kasun");
        assertTrue("Test 11: Cancelled booking returned in guest name search", !byName.isEmpty());
        assertEquals("Test 11: Status is CANCELLED in name search", ReservationStatus.CANCELLED, byName.get(0).getStatus());

        List<Reservation> byDate = f.service.getReservationsByCheckInDate(LocalDate.of(2026, 11, 15));
        assertTrue("Test 11: Cancelled booking returned in check-in date search", byDate.contains(f.b1));
    }

    private static void test12_resultsSortedByCheckInDateAndBookingId() {
        TestFixtures f = setupFixtures();
        List<Reservation> all = f.service.getAllReservations();

        assertTrue("Test 12: First record is on Nov 15", all.get(0).getCheckInDate().equals(LocalDate.of(2026, 11, 15)));
        assertTrue("Test 12: Second record is on Nov 15", all.get(1).getCheckInDate().equals(LocalDate.of(2026, 11, 15)));
        assertTrue("Test 12: Third record is on Nov 22", all.get(2).getCheckInDate().equals(LocalDate.of(2026, 11, 22)));

        assertTrue("Test 12: Tie-breaker by Booking ID", all.get(0).getBookingId().compareTo(all.get(1).getBookingId()) <= 0);
    }
}
