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
import lk.smartstay.service.GuestService;
import lk.smartstay.service.InvoiceService;
import lk.smartstay.service.ReservationService;
import lk.smartstay.service.RoomService;
import lk.smartstay.ui.DashboardPanel;
import lk.smartstay.ui.MainFrame;
import lk.smartstay.ui.admin.RoomManagementPanel;
import lk.smartstay.ui.guest.GuestRegistrationPanel;
import lk.smartstay.ui.guest.InvoicePanel;
import lk.smartstay.ui.guest.ReservationPanel;
import lk.smartstay.ui.guest.RoomSearchPanel;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class Phase11GUIRefinementTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");

    private static final String REFERENCE_INSTANT = "2026-11-13T04:30:00Z";

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("Running Phase 11 GUI Refinement, Validation & Regression Tests");
        System.out.println("======================================================================");

        try {
            test1_mainFrameNavigationSidebarEightItems();
            test2_mainFrameTabbedNavigationSynchronization();
            test3_dashboardDynamicMetricsAndDisclaimer();
            test4_dashboardAutoRefreshOnDataChange();
            test5_roomManagementValidationAndReferentialIntegrity();
            test6_guestRegistrationValidationAndInternationalSupport();
            test7_roomSearchValidationAndOverlapDetection();
            test8_reservationCreationAndTwoDayAdvancePolicy();
            test9_reservationManagementSearchAndCancellationRule();
            test10_invoiceCalculationsTaxDiscountAndZeroFloor();
            test11_invoicePanelPreviewAndFieldIntegrity();
        } catch (Exception e) {
            System.err.println("[UNEXPECTED EXCEPTION]: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }

        System.out.println("======================================================================");
        System.out.println("Phase 11 Test Summary: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
        System.out.println("======================================================================");

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
            System.err.println("[FAIL] " + testName + " - Expected: '" + expected + "', Actual: '" + actual + "'");
            testsFailed++;
        }
    }

    private static void test1_mainFrameNavigationSidebarEightItems() {
        System.out.println("\n--- Test 1: MainFrame 8-Item Navigation Sidebar ---");
        MainFrame frame = new MainFrame();

        List<JButton> navButtons = frame.getNavButtons();
        assertEquals("Test 1a: Exactly 8 navigation buttons in sidebar", 8, navButtons.size());

        String[] expectedLabels = {
                "Dashboard",
                "Room Management",
                "Guest Registration",
                "Search Rooms",
                "Make Reservation",
                "Reservation Management",
                "Invoices",
                "Exit"
        };

        for (int i = 0; i < expectedLabels.length; i++) {
            assertEquals("Test 1b: Navigation item " + (i + 1) + " label matches",
                    expectedLabels[i], navButtons.get(i).getText());
            assertTrue("Test 1b: Navigation button " + expectedLabels[i] + " is enabled",
                    navButtons.get(i).isEnabled());
        }

        assertTrue("Test 1c: Exit button is non-null", frame.getBtnExit() != null);
        assertEquals("Test 1c: Exit button text matches", "Exit", frame.getBtnExit().getText());
    }

    private static void test2_mainFrameTabbedNavigationSynchronization() {
        System.out.println("\n--- Test 2: Tabbed Navigation Synchronization ---");
        MainFrame frame = new MainFrame();
        ReservationPanel resPanel = frame.getReservationPanel();
        assertTrue("Test 2a: ReservationPanel is non-null", resPanel != null);

        JButton btnMakeRes = frame.getNavButtons().get(4);
        assertEquals("Test 2b: Button 4 is Make Reservation", "Make Reservation", btnMakeRes.getText());
        btnMakeRes.doClick();

        assertEquals("Test 2c: Make Reservation selects Tab 0 (Create Reservation)",
                0, resPanel.getTabbedPane().getSelectedIndex());

        JButton btnResMgmt = frame.getNavButtons().get(5);
        assertEquals("Test 2d: Button 5 is Reservation Management", "Reservation Management", btnResMgmt.getText());
        btnResMgmt.doClick();

        assertEquals("Test 2e: Reservation Management selects Tab 1 (Search & View Reservations)",
                1, resPanel.getTabbedPane().getSelectedIndex());

        btnMakeRes.doClick();
        assertEquals("Test 2f: Switching back selects Tab 0",
                0, resPanel.getTabbedPane().getSelectedIndex());
    }

    private static void test3_dashboardDynamicMetricsAndDisclaimer() {
        System.out.println("\n--- Test 3: Dashboard Dynamic Real-Time Metrics & Disclaimer ---");

        RoomRepository roomRepo = new RoomRepository();
        GuestRepository guestRepo = new GuestRepository();
        ReservationRepository resRepo = new ReservationRepository();

        Room r1 = new Room("R101", RoomType.STANDARD, BedType.SINGLE, 1, RoomStatus.AVAILABLE);
        Room r2 = new Room("R102", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Room r3 = new Room("R103", RoomType.SUITE, BedType.QUEEN, 2, RoomStatus.UNDER_MAINTENANCE);
        roomRepo.addRoom(r1);
        roomRepo.addRoom(r2);
        roomRepo.addRoom(r3);

        Guest g1 = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Guest g2 = new Guest("G002", "912345678V", "Sunil Fernando", "0712345678", "sunil@example.com");
        guestRepo.addGuest(g1);
        guestRepo.addGuest(g2);

        Reservation res1 = new Reservation("BKG001", g1, r1, 1,
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 10000.0, ReservationStatus.CONFIRMED);
        Reservation res2 = new Reservation("BKG002", g2, r2, 2,
                LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 28), 10000.0, ReservationStatus.CANCELLED);
        resRepo.addReservation(res1);
        resRepo.addReservation(res2);

        DashboardPanel dashboard = new DashboardPanel(roomRepo, guestRepo, resRepo);

        assertEquals("Test 3a: Total rooms count matches", 3, dashboard.getTotalRoomsCount());
        assertEquals("Test 3b: Total rooms label displays '3'", "3", dashboard.getLblTotalRoomsVal().getText());

        assertEquals("Test 3c: Administratively available rooms count matches", 2, dashboard.getAvailableRoomsCount());
        assertEquals("Test 3d: Available rooms label displays '2'", "2", dashboard.getLblAvailableRoomsVal().getText());

        assertEquals("Test 3e: Registered guests count matches", 2, dashboard.getRegisteredGuestsCount());
        assertEquals("Test 3f: Registered guests label displays '2'", "2", dashboard.getLblGuestsVal().getText());

        assertEquals("Test 3g: Total reservations count matches", 2, dashboard.getTotalReservationsCount());
        assertEquals("Test 3h: Total reservations label displays '2'", "2", dashboard.getLblReservationsVal().getText());
    }

    private static void test4_dashboardAutoRefreshOnDataChange() {
        System.out.println("\n--- Test 4: Dashboard Auto-Refresh On Data Change ---");

        RoomRepository roomRepo = new RoomRepository();
        GuestRepository guestRepo = new GuestRepository();
        ReservationRepository resRepo = new ReservationRepository();

        DashboardPanel dashboard = new DashboardPanel(roomRepo, guestRepo, resRepo);

        assertEquals("Test 4a: Initial total rooms is 0", "0", dashboard.getLblTotalRoomsVal().getText());
        assertEquals("Test 4b: Initial guests is 0", "0", dashboard.getLblGuestsVal().getText());

        roomRepo.addRoom(new Room("R501", RoomType.FAMILY_SUITE, BedType.QUEEN, 4, RoomStatus.AVAILABLE));
        guestRepo.addGuest(new Guest("G100", "851234567V", "Nimal Silva", "0779998888", "nimal@example.com"));

        dashboard.refreshMetrics();

        assertEquals("Test 4c: Refreshed total rooms is 1", "1", dashboard.getLblTotalRoomsVal().getText());
        assertEquals("Test 4d: Refreshed available rooms is 1", "1", dashboard.getLblAvailableRoomsVal().getText());
        assertEquals("Test 4e: Refreshed guests is 1", "1", dashboard.getLblGuestsVal().getText());
    }

    private static void test5_roomManagementValidationAndReferentialIntegrity() {
        System.out.println("\n--- Test 5: Room Management Validation & Referential Integrity ---");

        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        RoomService roomService = new RoomService(roomRepo, resRepo);
        RoomManagementPanel panel = new RoomManagementPanel(roomService);

        panel.getTxtRoomId().setText("   ");
        panel.executeAddRoom();
        assertTrue("Test 5a: Blank room ID rejected",
                panel.getLblStatusFeedback().getText().contains("Please enter a Room ID"));

        panel.getTxtRoomId().setText("R101");
        panel.getCmbRoomType().setSelectedItem(RoomType.DELUXE);
        panel.getCmbBedType().setSelectedItem(BedType.KING);
        panel.getSpinCapacity().setValue(2);
        panel.getCmbStatus().setSelectedItem(RoomStatus.AVAILABLE);
        panel.executeAddRoom();

        assertTrue("Test 5b: Room R101 added successfully", roomService.findRoomById("R101") != null);
        assertEquals("Test 5c: Table row count is 1", 1, panel.getTableModel().getRowCount());

        panel.getTxtRoomId().setText("R101");
        panel.executeAddRoom();
        assertTrue("Test 5d: Duplicate Room ID rejected",
                panel.getLblStatusFeedback().getText().contains("already exists"));

        panel.getTxtRoomId().setText("R101");
        panel.getCmbStatus().setSelectedItem(RoomStatus.UNDER_MAINTENANCE);
        panel.executeUpdateRoom();
        assertEquals("Test 5e: Room status updated to UNDER_MAINTENANCE",
                RoomStatus.UNDER_MAINTENANCE, roomService.findRoomById("R101").getStatus());

        Guest dummyGuest = new Guest("G001", "901234567V", "Test Guest", "0771234567", "test@test.com");
        Reservation res = new Reservation("BKG999", dummyGuest, roomService.findRoomById("R101"), 2,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5), 10000.0, ReservationStatus.CONFIRMED);
        resRepo.addReservation(res);

        panel.getTxtRoomId().setText("R101");
        panel.executeDeleteRoom();
        assertTrue("Test 5f: Room with reservation history cannot be deleted",
                roomService.findRoomById("R101") != null);
        assertTrue("Test 5g: Feedback warns about reservation history",
                panel.getLblStatusFeedback().getText().contains("referenced by existing reservation history")
                        || panel.getLblStatusFeedback().getText().contains("Could not delete room"));

        Room rFree = new Room("R999", RoomType.STANDARD, BedType.SINGLE, 1, RoomStatus.AVAILABLE);
        roomService.addRoom(rFree);
        panel.refreshTableData();
        panel.getTxtRoomId().setText("R999");
        panel.executeDeleteRoom();
        assertTrue("Test 5h: Unreferenced room R999 is successfully removed",
                roomService.findRoomById("R999") == null);
    }

    private static void test6_guestRegistrationValidationAndInternationalSupport() {
        System.out.println("\n--- Test 6: Guest Registration Validation & International Support ---");

        GuestRepository guestRepo = new GuestRepository();
        GuestService guestService = new GuestService(guestRepo);
        GuestRegistrationPanel panel = new GuestRegistrationPanel(guestService);

        panel.getTxtNicPassport().setText("");
        panel.getTxtFullName().setText("John Doe");
        panel.getTxtContactNumber().setText("0771234567");
        panel.getTxtEmail().setText("john@example.com");
        panel.executeRegisterGuest();
        assertTrue("Test 6a: Empty NIC/Passport rejected",
                panel.getLblStatusFeedback().getText().contains("Please enter NIC or Passport"));

        panel.getTxtNicPassport().setText("901234567V");
        panel.getTxtFullName().setText("John Doe");
        panel.getTxtContactNumber().setText("0771234567");
        panel.getTxtEmail().setText("john-at-example.com");
        panel.executeRegisterGuest();
        assertTrue("Test 6b: Invalid email rejected",
                panel.getLblStatusFeedback().getText().contains("valid email"));

        panel.getTxtNicPassport().setText("901234567V");
        panel.getTxtFullName().setText("Amal Perera");
        panel.getTxtContactNumber().setText("0771234567");
        panel.getTxtEmail().setText("amal@example.com");
        panel.executeRegisterGuest();
        assertTrue("Test 6c: Local guest registered", guestService.findGuestByNicOrPassport("901234567V") != null);

        panel.getTxtNicPassport().setText("901234567V");
        panel.getTxtFullName().setText("Another Person");
        panel.getTxtContactNumber().setText("0777654321");
        panel.getTxtEmail().setText("another@example.com");
        panel.executeRegisterGuest();
        assertTrue("Test 6d: Duplicate NIC/Passport rejected",
                panel.getLblStatusFeedback().getText().contains("already registered"));

        panel.getTxtNicPassport().setText("PA09876543");
        panel.getTxtFullName().setText("Alice Smith");
        panel.getTxtContactNumber().setText("+44 7911 123456");
        panel.getTxtEmail().setText("alice.smith@ukmail.co.uk");
        panel.executeRegisterGuest();
        assertTrue("Test 6e: International guest registered with passport & international phone",
                guestService.findGuestByNicOrPassport("PA09876543") != null);

        panel.getTxtSearch().setText("Alice");
        panel.getCmbSearchCriteria().setSelectedItem("Guest Name");
        panel.executeSearch();
        assertEquals("Test 6f: Search found 1 international guest", 1, panel.getTableModel().getRowCount());
    }

    private static void test7_roomSearchValidationAndOverlapDetection() {
        System.out.println("\n--- Test 7: Room Search Validation & Overlap Detection ---");

        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService resService = new ReservationService(roomRepo, resRepo);
        RoomSearchPanel panel = new RoomSearchPanel(resService);

        Room r1 = new Room("R101", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Room r2 = new Room("R102", RoomType.DELUXE, BedType.DOUBLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);
        roomRepo.addRoom(r2);

        Guest g = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");

        Reservation bkg = new Reservation("BKG001", g, r1, 2,
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 25), 10000.0, ReservationStatus.CONFIRMED);
        resRepo.addReservation(bkg);

        panel.getCmbRoomType().setSelectedItem(RoomType.DELUXE);
        panel.getTxtCheckInDate().setText("2026-11-25");
        panel.getTxtCheckOutDate().setText("2026-11-20");
        panel.executeSearch();
        assertTrue("Test 7a: Inverted dates rejected",
                panel.getLblStatusFeedback().getText().contains("must logically be after check-in"));

        panel.getTxtCheckInDate().setText("2026-11-22");
        panel.getTxtCheckOutDate().setText("2026-11-27");
        panel.executeSearch();
        assertEquals("Test 7b: Exactly 1 room available during overlap", 1, panel.getTableModel().getRowCount());
        assertEquals("Test 7c: Available room is R102", "R102", panel.getTableModel().getValueAt(0, 0));

        panel.getTxtCheckInDate().setText("2026-11-25");
        panel.getTxtCheckOutDate().setText("2026-11-30");
        panel.executeSearch();
        assertEquals("Test 7d: Touching boundary checkout allows both rooms", 2, panel.getTableModel().getRowCount());
    }

    private static void test8_reservationCreationAndTwoDayAdvancePolicy() {
        System.out.println("\n--- Test 8: Reservation Creation & 2-Day Advance Rule ---");

        Clock fixedClock = Clock.fixed(Instant.parse(REFERENCE_INSTANT), COLOMBO_ZONE);
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();

        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, fixedClock);
        GuestService guestService = new GuestService(guestRepo);

        Room room = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(room);
        Guest guest = new Guest("G001", "901234567V", "Sunil Fernando", "0712345678", "sunil@example.com");
        guestRepo.addGuest(guest);

        boolean advanceBlocked = false;
        try {
            resService.createReservation(guest, room, 2,
                    LocalDate.of(2026, 11, 14), LocalDate.of(2026, 11, 17));
        } catch (IllegalArgumentException ex) {
            advanceBlocked = true;
            assertTrue("Test 8a: Exception mentions 2-day advance policy",
                    ex.getMessage().contains("at least 2 calendar days"));
        }
        assertTrue("Test 8b: 1-day advance booking was rejected", advanceBlocked);

        Reservation validBooking = resService.createReservation(guest, room, 2,
                LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18));
        assertTrue("Test 8c: 2-day advance booking succeeded", validBooking != null);
        assertEquals("Test 8d: Booking advance is exactly LKR 10,000.00", 10000.0, validBooking.getBookingAdvance());
        assertTrue("Test 8e: Booking ID format is BKG...", validBooking.getBookingId().startsWith("BKG"));
        assertEquals("Test 8f: Reservation status is CONFIRMED", ReservationStatus.CONFIRMED, validBooking.getStatus());
    }

    private static void test9_reservationManagementSearchAndCancellationRule() {
        System.out.println("\n--- Test 9: Reservation Management Search & 24-Hour Cancellation Rule ---");

        Clock fixedClock = Clock.fixed(Instant.parse("2026-11-14T10:00:00Z"), COLOMBO_ZONE);
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();

        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, fixedClock);
        GuestService guestService = new GuestService(guestRepo);

        Room r1 = new Room("R101", RoomType.STANDARD, BedType.SINGLE, 1, RoomStatus.AVAILABLE);
        Room r2 = new Room("R102", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);
        roomRepo.addRoom(r2);

        Guest g1 = new Guest("G001", "901234567V", "Amal Perera", "0771234567", "amal@example.com");
        Guest g2 = new Guest("G002", "912345678V", "Kamal Silva", "0712345678", "kamal@example.com");
        guestRepo.addGuest(g1);
        guestRepo.addGuest(g2);

        Reservation resWithin24h = new Reservation("BKG101", g1, r1, 1,
                LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18), 10000.0, ReservationStatus.CONFIRMED);
        resRepo.addReservation(resWithin24h);

        Reservation resEligible = new Reservation("BKG102", g2, r2, 2,
                LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 21), 10000.0, ReservationStatus.CONFIRMED);
        resRepo.addReservation(resEligible);

        assertEquals("Test 9a: Search by Booking ID", "BKG101", resService.searchByBookingId("BKG101").getBookingId());
        assertEquals("Test 9b: Search by Guest Name partial match", 1, resService.searchByGuestName("kamal").size());
        assertEquals("Test 9c: Search by Check-in Date", 1, resService.getReservationsByCheckInDate(LocalDate.of(2026, 11, 15)).size());

        assertTrue("Test 9d: Booking within 24h cannot be cancelled", !resService.canCancel(resWithin24h));
        assertTrue("Test 9e: Ineligibility reason clearly stated",
                resService.getCancellationIneligibilityReason(resWithin24h).contains("within 24 hours"));

        assertTrue("Test 9f: Booking >= 24h can be cancelled", resService.canCancel(resEligible));

        boolean cancelled = resService.cancelReservation("BKG102");
        assertTrue("Test 9g: Cancellation succeeded", cancelled);
        assertEquals("Test 9h: Status updated to CANCELLED", ReservationStatus.CANCELLED, resService.searchByBookingId("BKG102").getStatus());

        assertTrue("Test 9i: Cancelled booking restores room availability",
                resService.isRoomAvailableForDates(r2, LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 21)));

        assertTrue("Test 9j: Already cancelled booking cannot be cancelled again", !resService.canCancel(resEligible));
    }

    private static void test10_invoiceCalculationsTaxDiscountAndZeroFloor() {
        System.out.println("\n--- Test 10: Invoice Calculations, Discount & Zero Floor ---");

        ReservationRepository resRepo = new ReservationRepository();
        InvoiceService invoiceService = new InvoiceService(resRepo);

        Guest guest = new Guest("G001", "901234567V", "Sunil Fernando", "0712345678", "sunil@example.com");

        Room deluxe = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Reservation res1 = new Reservation("BKG501", guest, deluxe, 3,
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 25), 10000.0, ReservationStatus.CONFIRMED);
        resRepo.addReservation(res1);

        Invoice inv1 = invoiceService.generateInvoice(res1);
        assertEquals("Test 10a: Base room charge", 45000.0, inv1.getBaseRoomCharge());
        assertEquals("Test 10b: Long stay discount (12% of base)", 5400.0, inv1.getDiscountAmount());
        assertEquals("Test 10c: Extra guest charge (1 * 1500 * 5)", 7500.0, inv1.getExtraGuestCharge());
        assertEquals("Test 10d: Tax amount (12% of taxable)", 5652.0, inv1.getTaxAmount());
        assertEquals("Test 10e: Final net payable amount", 42752.0, inv1.getFinalPayableAmount());
        assertEquals("Test 10f: Unused advance credit is 0", 0.0, inv1.getUnusedAdvanceCredit());

        Room standard = new Room("R101", RoomType.STANDARD, BedType.SINGLE, 1, RoomStatus.AVAILABLE);
        Reservation resSmall = new Reservation("BKG502", guest, standard, 1,
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 21), 10000.0, ReservationStatus.CONFIRMED);
        resRepo.addReservation(resSmall);

        Invoice invSmall = invoiceService.generateInvoice(resSmall);
        assertEquals("Test 10g: Final payable floored at 0.00", 0.0, invSmall.getFinalPayableAmount());
        assertEquals("Test 10h: Unused advance credit is 3,400.00", 3400.0, invSmall.getUnusedAdvanceCredit());
        assertTrue("Test 10i: hasUnusedAdvanceCredit is true", invSmall.hasUnusedAdvanceCredit());
    }

    private static void test11_invoicePanelPreviewAndFieldIntegrity() {
        System.out.println("\n--- Test 11: InvoicePanel Preview & Field Integrity ---");

        ReservationRepository resRepo = new ReservationRepository();
        RoomRepository roomRepo = new RoomRepository();
        GuestRepository guestRepo = new GuestRepository();
        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo);
        InvoiceService invoiceService = new InvoiceService(resRepo);

        Room room = new Room("R301", RoomType.SUITE, BedType.KING, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(room);
        Guest guest = new Guest("G001", "901234567V", "Sunil Fernando", "0712345678", "sunil@example.com");
        guestRepo.addGuest(guest);

        Reservation res = new Reservation("BKG888", guest, room, 2,
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 24), 10000.0, ReservationStatus.CONFIRMED);
        resRepo.addReservation(res);

        InvoicePanel panel = new InvoicePanel(invoiceService, resService);
        panel.selectBookingByIdForTesting("BKG888");

        Invoice generated = panel.generateInvoiceDirectly();
        assertTrue("Test 11a: Invoice generated via panel", generated != null);
        assertTrue("Test 11b: Print button enabled", panel.getBtnPrintInvoice().isEnabled());
        assertTrue("Test 11c: Save as text button enabled", panel.getBtnSaveAsText().isEnabled());

        String html = panel.getPreviewEditorPane().getText();
        String[] requiredFields = {
                "R301",
                "Suite",
                "King",
                "14,000.00",
                "Sunil Fernando",
                "901234567V",
                "2026-11-20",
                "2026-11-24",
                "4",
                "2",
                "10,000.00"
        };

        for (String field : requiredFields) {
            assertTrue("Test 11d: Mandatory invoice field present: " + field, html.contains(field));
        }
    }
}
