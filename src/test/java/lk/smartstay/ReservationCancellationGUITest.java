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
import lk.smartstay.service.GuestService;
import lk.smartstay.service.ReservationService;
import lk.smartstay.ui.guest.ReservationPanel;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class ReservationCancellationGUITest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Phase 09 Reservation Cancellation GUI Tests");
        System.out.println("==================================================");

        test1_initialButtonStateDisabledWhenNoSelection();
        test2_eligibleReservationEnablesCancelButton();
        test3_ineligibleReservationWithin24HoursDisablesCancelButton();
        test4_alreadyCancelledReservationDisablesCancelButtonAndShowsRefundNotice();
        test5_completedReservationDisablesCancelButton();
        test6_executeCancellationUpdatesTableAndDetailView();
        test7_consecutiveCancellationBlockedInUI();

        System.out.println("==================================================");
        System.out.println("Phase 09 GUI Test Summary: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
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

    private static Fixture createFixture(String isoInstant) {
        Clock fixedClock = Clock.fixed(Instant.parse(isoInstant), COLOMBO_ZONE);
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();

        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, fixedClock);
        GuestService guestService = new GuestService(guestRepo);

        Guest g1 = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Guest g2 = new Guest("G002", "912345678V", "Sunil Fernando", "0712345678", "sunil@example.com");
        guestRepo.addGuest(g1);
        guestRepo.addGuest(g2);

        Room r1 = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Room r2 = new Room("R201", RoomType.SUITE, BedType.QUEEN, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);
        roomRepo.addRoom(r2);

        Reservation bkg1 = new Reservation("BKG001", g1, r1, 2, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 24));
        resRepo.addReservation(bkg1);

        Reservation bkg2 = new Reservation("BKG002", g2, r2, 2, LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18));
        resRepo.addReservation(bkg2);

        Reservation bkg3 = new Reservation("BKG003", g1, r1, 2, LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 28),
                10000.0, ReservationStatus.CANCELLED);
        resRepo.addReservation(bkg3);

        Reservation bkg4 = new Reservation("BKG004", g2, r2, 2, LocalDate.of(2026, 11, 01), LocalDate.of(2026, 11, 04),
                10000.0, ReservationStatus.COMPLETED);
        resRepo.addReservation(bkg4);

        ReservationPanel panel = new ReservationPanel(resService, guestService);
        panel.setSelectedTab(1);

        return new Fixture(panel, resService, guestService);
    }

    private record Fixture(ReservationPanel panel, ReservationService resService, GuestService guestService) {}

    private static void test1_initialButtonStateDisabledWhenNoSelection() {
        Fixture f = createFixture("2026-11-15T04:30:00Z");
        JButton btnCancel = f.panel.getBtnCancelReservation();
        JLabel lblEligibility = f.panel.getLblDetailCancellationEligibility();

        f.panel.getReservationHistoryTable().clearSelection();
        assertTrue("Test 1a: Cancel button is disabled with no selection", !btnCancel.isEnabled());
        assertEquals("Test 1b: Eligibility label shows '-' with no selection", "-", lblEligibility.getText());
    }

    private static void test2_eligibleReservationEnablesCancelButton() {
        Fixture f = createFixture("2026-11-15T04:30:00Z");
        JTable table = f.panel.getReservationHistoryTable();
        DefaultTableModel model = f.panel.getReservationTableModel();

        int targetRow = findRowIndex(model, "BKG001");
        assertTrue("Test 2a: BKG001 exists in table", targetRow >= 0);

        table.setRowSelectionInterval(targetRow, targetRow);

        JButton btnCancel = f.panel.getBtnCancelReservation();
        JLabel lblEligibility = f.panel.getLblDetailCancellationEligibility();

        assertTrue("Test 2b: Cancel button is enabled for BKG001", btnCancel.isEnabled());
        assertTrue("Test 2c: Eligibility label indicates eligible and refund",
                lblEligibility.getText().contains("Eligible for Cancellation") && lblEligibility.getText().contains("10,000.00"));
    }

    private static void test3_ineligibleReservationWithin24HoursDisablesCancelButton() {
        Fixture f = createFixture("2026-11-15T04:30:00Z");
        JTable table = f.panel.getReservationHistoryTable();
        DefaultTableModel model = f.panel.getReservationTableModel();

        int targetRow = findRowIndex(model, "BKG002");
        assertTrue("Test 3a: BKG002 exists in table", targetRow >= 0);

        table.setRowSelectionInterval(targetRow, targetRow);

        JButton btnCancel = f.panel.getBtnCancelReservation();
        JLabel lblEligibility = f.panel.getLblDetailCancellationEligibility();

        assertTrue("Test 3b: Cancel button is disabled for booking within 24h", !btnCancel.isEnabled());
        assertTrue("Test 3c: Eligibility label displays ineligibility reason",
                lblEligibility.getText().contains("Ineligible") && lblEligibility.getText().contains("24 hours"));
    }

    private static void test4_alreadyCancelledReservationDisablesCancelButtonAndShowsRefundNotice() {
        Fixture f = createFixture("2026-11-15T04:30:00Z");
        JTable table = f.panel.getReservationHistoryTable();
        DefaultTableModel model = f.panel.getReservationTableModel();

        int targetRow = findRowIndex(model, "BKG003");
        assertTrue("Test 4a: BKG003 exists in table", targetRow >= 0);

        table.setRowSelectionInterval(targetRow, targetRow);

        JButton btnCancel = f.panel.getBtnCancelReservation();
        JLabel lblEligibility = f.panel.getLblDetailCancellationEligibility();

        assertTrue("Test 4b: Cancel button disabled for already cancelled booking", !btnCancel.isEnabled());
        assertTrue("Test 4c: Eligibility label shows cancelled status and refund due",
                lblEligibility.getText().contains("Cancelled") && lblEligibility.getText().contains("Refund Due: LKR 10,000.00"));
    }

    private static void test5_completedReservationDisablesCancelButton() {
        Fixture f = createFixture("2026-11-15T04:30:00Z");
        JTable table = f.panel.getReservationHistoryTable();
        DefaultTableModel model = f.panel.getReservationTableModel();

        int targetRow = findRowIndex(model, "BKG004");
        assertTrue("Test 5a: BKG004 exists in table", targetRow >= 0);

        table.setRowSelectionInterval(targetRow, targetRow);

        JButton btnCancel = f.panel.getBtnCancelReservation();
        JLabel lblEligibility = f.panel.getLblDetailCancellationEligibility();

        assertTrue("Test 5b: Cancel button disabled for completed stay", !btnCancel.isEnabled());
        assertTrue("Test 5c: Eligibility label shows stay completed",
                lblEligibility.getText().contains("Completed"));
    }

    private static void test6_executeCancellationUpdatesTableAndDetailView() {
        Fixture f = createFixture("2026-11-15T04:30:00Z");
        JTable table = f.panel.getReservationHistoryTable();
        DefaultTableModel model = f.panel.getReservationTableModel();

        int targetRow = findRowIndex(model, "BKG001");
        table.setRowSelectionInterval(targetRow, targetRow);

        boolean cancelled = f.panel.cancelSelectedReservationDirectly();
        assertTrue("Test 6a: Direct cancellation returns true", cancelled);

        int updatedRow = findRowIndex(model, "BKG001");
        String statusInTable = (String) model.getValueAt(updatedRow, 9);
        assertEquals("Test 6b: Table status column updated to 'Cancelled'", "Cancelled", statusInTable);

        JLabel lblEligibility = f.panel.getLblDetailCancellationEligibility();
        assertTrue("Test 6c: Detail view displays Refund Due",
                lblEligibility.getText().contains("Refund Due: LKR 10,000.00"));

        JButton btnCancel = f.panel.getBtnCancelReservation();
        assertTrue("Test 6d: Cancel button is now disabled after cancellation", !btnCancel.isEnabled());
    }

    private static void test7_consecutiveCancellationBlockedInUI() {
        Fixture f = createFixture("2026-11-15T04:30:00Z");
        JTable table = f.panel.getReservationHistoryTable();
        DefaultTableModel model = f.panel.getReservationTableModel();

        int targetRow = findRowIndex(model, "BKG001");
        table.setRowSelectionInterval(targetRow, targetRow);

        assertTrue("Test 7a: First cancellation succeeds", f.panel.cancelSelectedReservationDirectly());
        assertTrue("Test 7b: Second cancellation attempt fails", !f.panel.cancelSelectedReservationDirectly());
    }

    private static int findRowIndex(DefaultTableModel model, String bookingId) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (bookingId.equals(model.getValueAt(i, 0))) {
                return i;
            }
        }
        return -1;
    }
}
