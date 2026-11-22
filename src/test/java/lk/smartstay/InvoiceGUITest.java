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
import lk.smartstay.service.ReservationService;
import lk.smartstay.ui.guest.InvoicePanel;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JTextField;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class InvoiceGUITest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Phase 10 InvoicePanel GUI Tests");
        System.out.println("==================================================");

        test1_initialComponentStates();
        test2_dropdownExcludesCancelledBookings();
        test3_searchByBookingIdPopulatesSummary();
        test4_generateInvoiceUpdatesPreviewAndEnablesButtons();
        test5_previewContainsAllFourteenRequiredFields();
        test6_criticalEdgeCaseDisplaysZeroPayableAndRefundCredit();
        test7_resetFormClearsPreviewAndDisablesButtons();

        System.out.println("==================================================");
        System.out.println("Phase 10 GUI Test Summary: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
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

    private record Fixture(InvoicePanel panel, InvoiceService invoiceService, ReservationService resService) {}

    private static Fixture createFixture() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-11-15T04:30:00Z"), COLOMBO_ZONE);
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();

        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, fixedClock);
        InvoiceService invoiceService = new InvoiceService(resRepo);

        Guest g1 = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Guest g2 = new Guest("G002", "912345678V", "Sunil Fernando", "0712345678", "sunil@example.com");
        guestRepo.addGuest(g1);
        guestRepo.addGuest(g2);

        Room r1 = new Room("R101", RoomType.STANDARD, BedType.QUEEN, 2, RoomStatus.AVAILABLE);
        Room r2 = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);
        roomRepo.addRoom(r2);

        Reservation bkg1 = new Reservation("BKG001", g1, r1, 1, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 21));
        resRepo.addReservation(bkg1);

        Reservation bkg2 = new Reservation("BKG002", g2, r2, 3, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 25));
        resRepo.addReservation(bkg2);

        Reservation bkg3 = new Reservation("BKG003", g1, r1, 2, LocalDate.of(2026, 11, 26), LocalDate.of(2026, 11, 28),
                10000.0, ReservationStatus.CANCELLED);
        resRepo.addReservation(bkg3);

        InvoicePanel panel = new InvoicePanel(invoiceService, resService);
        return new Fixture(panel, invoiceService, resService);
    }

    private static void test1_initialComponentStates() {
        Fixture f = createFixture();
        assertTrue("Test 1a: Generate button initially disabled", !f.panel.getBtnGenerateInvoice().isEnabled());
        assertTrue("Test 1b: Print button initially disabled", !f.panel.getBtnPrintInvoice().isEnabled());
        assertTrue("Test 1c: Save as Text initially disabled", !f.panel.getBtnSaveAsText().isEnabled());
        assertTrue("Test 1d: No reservation currently selected", f.panel.getCurrentSelectedReservation() == null);
        assertTrue("Test 1e: No invoice currently generated", f.panel.getCurrentGeneratedInvoice() == null);
    }

    private static void test2_dropdownExcludesCancelledBookings() {
        Fixture f = createFixture();
        JComboBox<String> cmb = f.panel.getCmbEligibleBookings();

        assertEquals("Test 2a: Exactly 3 items in dropdown (placeholder + 2 active bookings)", 3, cmb.getItemCount());

        boolean hasBkg1 = false;
        boolean hasBkg2 = false;
        boolean hasBkg3 = false;

        for (int i = 0; i < cmb.getItemCount(); i++) {
            String item = cmb.getItemAt(i);
            if (item.contains("BKG001")) hasBkg1 = true;
            if (item.contains("BKG002")) hasBkg2 = true;
            if (item.contains("BKG003")) hasBkg3 = true;
        }

        assertTrue("Test 2b: BKG001 present in dropdown", hasBkg1);
        assertTrue("Test 2c: BKG002 present in dropdown", hasBkg2);
        assertTrue("Test 2d: Cancelled BKG003 EXCLUDED from dropdown", !hasBkg3);
    }

    private static void test3_searchByBookingIdPopulatesSummary() {
        Fixture f = createFixture();
        f.panel.selectBookingByIdForTesting("BKG002");

        Reservation selected = f.panel.getCurrentSelectedReservation();
        assertTrue("Test 3a: Reservation selected non-null", selected != null);
        assertEquals("Test 3b: Selected Booking ID is BKG002", "BKG002", selected.getBookingId());
        assertTrue("Test 3c: Generate Invoice button enabled after selection", f.panel.getBtnGenerateInvoice().isEnabled());
    }

    private static void test4_generateInvoiceUpdatesPreviewAndEnablesButtons() {
        Fixture f = createFixture();
        f.panel.selectBookingByIdForTesting("BKG002");

        f.panel.handleGenerateInvoice();

        Invoice inv = f.panel.getCurrentGeneratedInvoice();
        assertTrue("Test 4a: Generated invoice non-null", inv != null);
        assertEquals("Test 4b: Invoice ID is INV-BKG002", "INV-BKG002", inv.getInvoiceId());
        assertTrue("Test 4c: Print Invoice button enabled", f.panel.getBtnPrintInvoice().isEnabled());
        assertTrue("Test 4d: Save as Text button enabled", f.panel.getBtnSaveAsText().isEnabled());
    }

    private static void test5_previewContainsAllFourteenRequiredFields() {
        Fixture f = createFixture();
        f.panel.selectBookingByIdForTesting("BKG002");
        f.panel.handleGenerateInvoice();

        JEditorPane preview = f.panel.getPreviewEditorPane();
        String html = preview.getText();

        assertTrue("Field 1: Room ID R201 in preview", html.contains("R201"));

        assertTrue("Field 2: Room Type Deluxe Room in preview", html.contains("Deluxe Room"));

        assertTrue("Field 3: Bed Type King in preview", html.contains("King Bed"));

        assertTrue("Field 4: Daily Room Rate 9,000 in preview", html.contains("9,000.00"));

        assertTrue("Field 5: Guest Name Sunil Fernando in preview", html.contains("Sunil Fernando"));
        assertTrue("Field 5: Guest NIC in preview", html.contains("912345678V"));

        assertTrue("Field 6: Check-in Date 2026-11-20 in preview", html.contains("2026-11-20"));

        assertTrue("Field 7: Check-out Date 2026-11-25 in preview", html.contains("2026-11-25"));

        assertTrue("Field 8: Number of Nights (5) in preview", html.contains("5"));

        assertTrue("Field 9: Number of Guests (3) in preview", html.contains("3"));

        assertTrue("Field 10: Extra Guest Charges 7,500 in preview", html.contains("7,500.00"));

        assertTrue("Field 11: Long Stay Discount 5,400 in preview", html.contains("5,400.00"));

        assertTrue("Field 12: Tax 5,652 in preview", html.contains("5,652.00"));

        assertTrue("Field 13: Booking Advance Deduction 10,000 in preview", html.contains("10,000.00"));

        assertTrue("Field 14: Final Payable Amount 42,752.00 in preview", html.contains("42,752.00"));
    }

    private static void test6_criticalEdgeCaseDisplaysZeroPayableAndRefundCredit() {
        Fixture f = createFixture();
        f.panel.selectBookingByIdForTesting("BKG001");
        f.panel.handleGenerateInvoice();

        Invoice inv = f.panel.getCurrentGeneratedInvoice();
        assertEquals("Test 6a: Net payable is exactly 0.00", 0.0, inv.getFinalPayableAmount());
        assertEquals("Test 6b: Unused credit is exactly 3400.00", 3400.0, inv.getUnusedAdvanceCredit());

        JEditorPane preview = f.panel.getPreviewEditorPane();
        String html = preview.getText();

        assertTrue("Test 6c: Preview displays LKR 0.00 payable", html.contains("0.00"));
        assertTrue("Test 6d: Preview displays Refundable Unused Advance Credit", html.contains("Refundable Unused Advance Credit"));
        assertTrue("Test 6e: Preview displays 3,400.00 credit amount", html.contains("3,400.00"));
    }

    private static void test7_resetFormClearsPreviewAndDisablesButtons() {
        Fixture f = createFixture();
        f.panel.selectBookingByIdForTesting("BKG002");
        f.panel.handleGenerateInvoice();

        assertTrue("Test 7a: Invoice generated before reset", f.panel.getCurrentGeneratedInvoice() != null);

        f.panel.getCmbEligibleBookings().setSelectedIndex(0);

        assertTrue("Test 7b: Selected reservation is null after reset", f.panel.getCurrentSelectedReservation() == null);
        assertTrue("Test 7c: Generate button disabled after reset", !f.panel.getBtnGenerateInvoice().isEnabled());
        assertTrue("Test 7d: Print button disabled after reset", !f.panel.getBtnPrintInvoice().isEnabled());
        assertTrue("Test 7e: Save as Text button disabled after reset", !f.panel.getBtnSaveAsText().isEnabled());
    }
}
