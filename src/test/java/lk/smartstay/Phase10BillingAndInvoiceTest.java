package lk.smartstay;

import lk.smartstay.enums.BedType;
import lk.smartstay.enums.ReservationStatus;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Guest;
import lk.smartstay.model.Invoice;
import lk.smartstay.model.Reservation;
import lk.smartstay.model.Room;
import lk.smartstay.repository.GuestRepository;
import lk.smartstay.repository.ReservationRepository;
import lk.smartstay.repository.RoomRepository;
import lk.smartstay.service.InvoiceService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Phase10BillingAndInvoiceTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Phase 10 Billing & Invoice Algorithm Tests");
        System.out.println("==================================================");

        test1_standardOneNightCriticalEdgeCase();
        test2_deluxeFiveNightDocumentedExample();
        test3_suiteTaxCalculation();
        test4_familySuiteTaxAndExtraGuests();
        test5_fourNightStayNoDiscount();
        test6_fiveNightStayTwelvePercentDiscount();
        test7_discountBasisExcludesExtraGuestCharge();
        test8_extraGuestsCalculatedAccurately();
        test9_noExtraGuestsWhenWithinCapacity();
        test10_advanceDeductionNormalStay();
        test11_advanceGreaterThanTotalBillZeroPayable();
        test12_cancelledReservationRejection();
        test13_nullReservationRejection();
        test14_stableInvoiceIdAndIdempotentCaching();
        test15_reservationDataNotModifiedDuringInvoiceGeneration();
        test16_allRequiredInvoiceFieldsAvailable();

        System.out.println("==================================================");
        System.out.println("Phase 10 Test Summary: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
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

    private static void assertDoubleEquals(String testName, double expected, double actual) {
        if (Math.abs(expected - actual) < 0.001) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName + " - Expected: " + expected + ", Actual: " + actual);
            testsFailed++;
        }
    }

    private static Guest createTestGuest() {
        return new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
    }

    private static void test1_standardOneNightCriticalEdgeCase() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R101", RoomType.STANDARD, BedType.QUEEN, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG001", guest, room, 1, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 21));

        Invoice invoice = service.generateInvoice(res);

        assertDoubleEquals("Test 1a: Base room charge", 6000.0, invoice.getBaseRoomCharge());
        assertDoubleEquals("Test 1b: Extra guest charge is 0", 0.0, invoice.getExtraGuestCharge());
        assertDoubleEquals("Test 1c: Discount is 0 (1 night < 5)", 0.0, invoice.getDiscountAmount());
        assertDoubleEquals("Test 1d: Taxable subtotal is 6000", 6000.0, invoice.getTaxableSubtotal());
        assertDoubleEquals("Test 1e: Tax (10% of 6000) is 600", 600.0, invoice.getTaxAmount());
        assertDoubleEquals("Test 1f: Total bill is 6600", 6600.0, invoice.getBillTotal());
        assertDoubleEquals("Test 1g: Advance recorded is 10000", 10000.0, invoice.getBookingAdvanceDeduction());
        assertDoubleEquals("Test 1h: Final net payable is 0.00 (not negative)", 0.0, invoice.getFinalPayableAmount());
        assertDoubleEquals("Test 1i: Unused advance credit is 3400.00", 3400.0, invoice.getUnusedAdvanceCredit());
        assertTrue("Test 1j: hasUnusedAdvanceCredit is true", invoice.hasUnusedAdvanceCredit());
    }

    private static void test2_deluxeFiveNightDocumentedExample() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG002", guest, room, 3, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 25));

        Invoice invoice = service.generateInvoice(res);

        assertDoubleEquals("Test 2a: Base room charge", 45000.0, invoice.getBaseRoomCharge());
        assertDoubleEquals("Test 2b: Extra guest charge (1 extra * 1500 * 5)", 7500.0, invoice.getExtraGuestCharge());
        assertDoubleEquals("Test 2c: Long-stay discount (12% of 45000)", 5400.0, invoice.getDiscountAmount());
        assertDoubleEquals("Test 2d: Taxable subtotal (45000 + 7500 - 5400)", 47100.0, invoice.getTaxableSubtotal());
        assertDoubleEquals("Test 2e: Government tax (12% of 47100)", 5652.0, invoice.getTaxAmount());
        assertDoubleEquals("Test 2f: Total stay bill (47100 + 5652)", 52752.0, invoice.getBillTotal());
        assertDoubleEquals("Test 2g: Advance deduction", 10000.0, invoice.getBookingAdvanceDeduction());
        assertDoubleEquals("Test 2h: Final net payable (52752 - 10000)", 42752.0, invoice.getFinalPayableAmount());
        assertDoubleEquals("Test 2i: Unused advance credit is 0", 0.0, invoice.getUnusedAdvanceCredit());
        assertTrue("Test 2j: hasUnusedAdvanceCredit is false", !invoice.hasUnusedAdvanceCredit());
    }

    private static void test3_suiteTaxCalculation() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R301", RoomType.SUITE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG003", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 22));

        Invoice invoice = service.generateInvoice(res);

        assertDoubleEquals("Test 3a: Suite base charge", 28000.0, invoice.getBaseRoomCharge());
        assertDoubleEquals("Test 3b: Suite tax (15% of 28000)", 4200.0, invoice.getTaxAmount());
        assertDoubleEquals("Test 3c: Suite bill total", 32200.0, invoice.getBillTotal());
        assertDoubleEquals("Test 3d: Suite payable after 10000 advance", 22200.0, invoice.getFinalPayableAmount());
    }

    private static void test4_familySuiteTaxAndExtraGuests() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R401", RoomType.FAMILY_SUITE, BedType.KING, 4, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG004", guest, room, 5, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));

        Invoice invoice = service.generateInvoice(res);

        assertDoubleEquals("Test 4a: Family suite base charge", 54000.0, invoice.getBaseRoomCharge());
        assertDoubleEquals("Test 4b: Family suite extra guest charge", 7500.0, invoice.getExtraGuestCharge());
        assertDoubleEquals("Test 4c: Family suite taxable subtotal", 61500.0, invoice.getTaxableSubtotal());
        assertDoubleEquals("Test 4d: Family suite tax (15%)", 9225.0, invoice.getTaxAmount());
        assertDoubleEquals("Test 4e: Family suite total", 70725.0, invoice.getBillTotal());
        assertDoubleEquals("Test 4f: Family suite final payable", 60725.0, invoice.getFinalPayableAmount());
    }

    private static void test5_fourNightStayNoDiscount() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG005", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 24));

        Invoice invoice = service.generateInvoice(res);

        assertEquals("Test 5a: Nights count is 4", 4L, res.calculateNumberOfNights());
        assertTrue("Test 5b: qualifiesForLongStayDiscount is false", !res.qualifiesForLongStayDiscount());
        assertDoubleEquals("Test 5c: Discount is 0 for 4 nights", 0.0, invoice.getDiscountAmount());
    }

    private static void test6_fiveNightStayTwelvePercentDiscount() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG006", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 25));

        Invoice invoice = service.generateInvoice(res);

        assertEquals("Test 6a: Nights count is 5", 5L, res.calculateNumberOfNights());
        assertTrue("Test 6b: qualifiesForLongStayDiscount is true", res.qualifiesForLongStayDiscount());
        assertDoubleEquals("Test 6c: Discount is exactly 12% (5400)", 5400.0, invoice.getDiscountAmount());
    }

    private static void test7_discountBasisExcludesExtraGuestCharge() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);

        Reservation res = new Reservation("BKG007", guest, room, 4, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 25));

        Invoice invoice = service.generateInvoice(res);

        double expectedBase = 45000.0;
        double expectedExtra = 2 * 1500.0 * 5;
        double expectedDiscount = 45000.0 * 0.12;

        assertDoubleEquals("Test 7a: Extra guest charge is 15000", expectedExtra, invoice.getExtraGuestCharge());
        assertDoubleEquals("Test 7b: Discount basis is base charge only (5400)", expectedDiscount, invoice.getDiscountAmount());
    }

    private static void test8_extraGuestsCalculatedAccurately() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R101", RoomType.STANDARD, BedType.QUEEN, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG008", guest, room, 4, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));

        Invoice invoice = service.generateInvoice(res);

        assertDoubleEquals("Test 8: Extra guest charges for 2 extra guests over 3 nights", 6000.0, invoice.getExtraGuestCharge());
    }

    private static void test9_noExtraGuestsWhenWithinCapacity() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG009", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 22));

        Invoice invoice = service.generateInvoice(res);

        assertDoubleEquals("Test 9: Extra guest charge is 0 when within capacity", 0.0, invoice.getExtraGuestCharge());
    }

    private static void test10_advanceDeductionNormalStay() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG010", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));

        Invoice invoice = service.generateInvoice(res);

        assertDoubleEquals("Test 10a: Total bill is 30240", 30240.0, invoice.getBillTotal());
        assertDoubleEquals("Test 10b: Advance deducted is 10000", 10000.0, invoice.getBookingAdvanceDeduction());
        assertDoubleEquals("Test 10c: Net payable is 20240", 20240.0, invoice.getFinalPayableAmount());
    }

    private static void test11_advanceGreaterThanTotalBillZeroPayable() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R101", RoomType.STANDARD, BedType.QUEEN, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG011", guest, room, 1, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 21));

        Invoice invoice = service.generateInvoice(res);

        assertDoubleEquals("Test 11a: Net payable is 0.00", 0.0, invoice.getFinalPayableAmount());
        assertDoubleEquals("Test 11b: Unused credit is exactly 3400.00", 3400.0, invoice.getUnusedAdvanceCredit());
        assertTrue("Test 11c: Unused credit flag is true", invoice.hasUnusedAdvanceCredit());
    }

    private static void test12_cancelledReservationRejection() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R101", RoomType.STANDARD, BedType.QUEEN, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG012", guest, room, 1, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 21),
                10000.0, ReservationStatus.CANCELLED);

        boolean exceptionThrown = false;
        try {
            service.generateInvoice(res);
        } catch (IllegalStateException ex) {
            exceptionThrown = true;
            assertTrue("Test 12a: Exception message mentions CANCELLED", ex.getMessage().contains("CANCELLED"));
        }
        assertTrue("Test 12b: IllegalStateException thrown for cancelled reservation", exceptionThrown);
    }

    private static void test13_nullReservationRejection() {
        InvoiceService service = new InvoiceService();
        boolean exceptionThrown = false;
        try {
            service.generateInvoice(null);
        } catch (IllegalArgumentException ex) {
            exceptionThrown = true;
        }
        assertTrue("Test 13: IllegalArgumentException thrown for null reservation", exceptionThrown);
    }

    private static void test14_stableInvoiceIdAndIdempotentCaching() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG014", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));

        Invoice inv1 = service.generateInvoice(res);
        Invoice inv2 = service.generateInvoice(res);

        assertEquals("Test 14a: Invoice ID is stable and linked to booking", "INV-BKG014", inv1.getInvoiceId());
        assertTrue("Test 14b: Repeated calls return exact same invoice instance", inv1 == inv2);
        assertEquals("Test 14c: Cache size is 1", 1, service.getAllInvoices().size());
    }

    private static void test15_reservationDataNotModifiedDuringInvoiceGeneration() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG015", guest, room, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));

        service.generateInvoice(res);

        assertEquals("Test 15a: Booking ID unchanged", "BKG015", res.getBookingId());
        assertEquals("Test 15b: Status remains CONFIRMED", ReservationStatus.CONFIRMED, res.getStatus());
        assertEquals("Test 15c: Guest count unchanged", 2, res.getNumberOfGuests());
        assertEquals("Test 15d: Check-in date unchanged", LocalDate.of(2026, 11, 20), res.getCheckInDate());
        assertEquals("Test 15e: Check-out date unchanged", LocalDate.of(2026, 11, 23), res.getCheckOutDate());
    }

    private static void test16_allRequiredInvoiceFieldsAvailable() {
        InvoiceService service = new InvoiceService();
        Guest guest = createTestGuest();
        Room room = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res = new Reservation("BKG016", guest, room, 3, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 25));

        Invoice inv = service.generateInvoice(res);

        assertEquals("Field 1: Room ID", "R201", inv.getReservation().getRoom().getRoomId());

        assertEquals("Field 2: Room Type", RoomType.DELUXE, inv.getReservation().getRoom().getRoomType());

        assertEquals("Field 3: Bed Type", BedType.KING, inv.getReservation().getRoom().getBedType());

        assertDoubleEquals("Field 4: Daily Room Rate", 9000.0, inv.getReservation().getRoom().getDailyRate());

        assertEquals("Field 5: Guest Name", "Amal Perera", inv.getReservation().getGuest().getFullName());
        assertEquals("Field 5: Guest NIC", "901234567V", inv.getReservation().getGuest().getNicOrPassport());

        assertEquals("Field 6: Check-in Date", LocalDate.of(2026, 11, 20), inv.getReservation().getCheckInDate());

        assertEquals("Field 7: Check-out Date", LocalDate.of(2026, 11, 25), inv.getReservation().getCheckOutDate());

        assertEquals("Field 8: Number of Nights", 5L, inv.getReservation().calculateNumberOfNights());

        assertEquals("Field 9: Number of Guests", 3, inv.getReservation().getNumberOfGuests());

        assertDoubleEquals("Field 10: Extra Guest Charges", 7500.0, inv.getExtraGuestCharge());

        assertDoubleEquals("Field 11: Discount", 5400.0, inv.getDiscountAmount());

        assertDoubleEquals("Field 12: Tax", 5652.0, inv.getTaxAmount());

        assertDoubleEquals("Field 13: Booking Advance Deduction", 10000.0, inv.getBookingAdvanceDeduction());

        assertDoubleEquals("Field 14: Final Payable Amount", 42752.0, inv.getFinalPayableAmount());
    }
}
