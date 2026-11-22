package lk.smartstay;

import lk.smartstay.enums.BedType;
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class ReservationSearchGUITest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-11-13T04:30:00Z"),
            COLOMBO_ZONE
    );

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Phase 08 Reservation Search GUI Tests");
        System.out.println("==================================================");

        testTabbedPaneStructure();
        testSearchControlsAndCriteria();
        testReservationTable10ColumnsAndNonEditable();
        testSearchActionPopulatesTable();
        testRowSelectionPopulatesDetailView();
        testShowAllAndClearSearchButtons();

        System.out.println("==================================================");
        System.out.println("Reservation Search GUI Test Summary: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
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

    private static Object getPrivateField(Object target, String fieldName) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Cannot access field: " + fieldName, e);
        }
    }

    private static JButton findButtonByText(Container container, String text) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton btn && text.equals(btn.getText())) {
                return btn;
            }
            if (c instanceof Container child) {
                JButton found = findButtonByText(child, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static void testTabbedPaneStructure() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();
        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);
        GuestService guestService = new GuestService(guestRepo);

        ReservationPanel panel = new ReservationPanel(resService, guestService);
        JTabbedPane tabbedPane = (JTabbedPane) getPrivateField(panel, "tabbedPane");

        assertEquals("UI Test 1: Exactly 2 tabs present", 2, tabbedPane.getTabCount());
        assertEquals("UI Test 1: Tab 0 is Create Reservation", "Create Reservation", tabbedPane.getTitleAt(0));
        assertEquals("UI Test 1: Tab 1 is Search & View Reservations", "Search & View Reservations", tabbedPane.getTitleAt(1));
    }

    private static void testSearchControlsAndCriteria() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();
        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);
        GuestService guestService = new GuestService(guestRepo);

        ReservationPanel panel = new ReservationPanel(resService, guestService);

        @SuppressWarnings("unchecked")
        JComboBox<String> cmbSearch = (JComboBox<String>) getPrivateField(panel, "cmbSearchBy");
        assertEquals("UI Test 2: Exactly 3 search criteria", 3, cmbSearch.getItemCount());
        assertEquals("UI Test 2: Option 0 is Booking ID", "Booking ID", cmbSearch.getItemAt(0));
        assertEquals("UI Test 2: Option 1 is Guest Name", "Guest Name", cmbSearch.getItemAt(1));
        assertEquals("UI Test 2: Option 2 is Check-in Date", "Check-in Date", cmbSearch.getItemAt(2));

        JButton btnSearch = (JButton) getPrivateField(panel, "btnSearch");
        JButton btnClear = (JButton) getPrivateField(panel, "btnClearSearch");
        JButton btnShowAll = (JButton) getPrivateField(panel, "btnShowAll");

        assertTrue("UI Test 2: Search button present", btnSearch != null);
        assertTrue("UI Test 2: Clear Search button present", btnClear != null);
        assertTrue("UI Test 2: Show All button present", btnShowAll != null);
    }

    private static void testReservationTable10ColumnsAndNonEditable() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();
        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);
        GuestService guestService = new GuestService(guestRepo);

        ReservationPanel panel = new ReservationPanel(resService, guestService);
        DefaultTableModel model = (DefaultTableModel) getPrivateField(panel, "reservationTableModel");

        String[] expectedCols = {
                "Booking ID", "Guest Name", "Room ID", "Room Type",
                "Check-in", "Check-out", "Nights", "Guests", "Advance (LKR)", "Status"
        };

        assertEquals("UI Test 3: Exactly 10 columns in reservation table", 10, model.getColumnCount());
        for (int i = 0; i < expectedCols.length; i++) {
            assertEquals("UI Test 3: Column " + i + " header matches", expectedCols[i], model.getColumnName(i));
        }

        model.addRow(new Object[]{"BKG001", "Kamal", "101", "Deluxe", "2026-11-15", "2026-11-18", 3, 2, "10,000.00", "Confirmed"});
        for (int c = 0; c < 10; c++) {
            assertTrue("UI Test 3: Cell (0, " + c + ") is non-editable", !model.isCellEditable(0, c));
        }
        model.setRowCount(0);
    }

    private static void testSearchActionPopulatesTable() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();
        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);
        GuestService guestService = new GuestService(guestRepo);

        Guest g = new Guest("GST201", "222333444V", "Priyani Fernando", "0778889990", "priyani@example.com");
        guestRepo.addGuest(g);
        Room r = new Room("201", RoomType.DELUXE, BedType.DOUBLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r);
        Reservation res = resService.createReservation(g, r, 2, LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18));

        ReservationPanel panel = new ReservationPanel(resService, guestService);

        panel.searchReservations("Booking ID", res.getBookingId());

        DefaultTableModel model = panel.getReservationTableModel();
        assertEquals("UI Test 4: Exactly 1 result found for Booking ID search", 1, model.getRowCount());
        assertEquals("UI Test 4: Row 0 Booking ID matches", res.getBookingId(), model.getValueAt(0, 0));
        assertEquals("UI Test 4: Row 0 Guest Name matches", "Priyani Fernando", model.getValueAt(0, 1));
        assertEquals("UI Test 4: Row 0 Room ID matches", "201", model.getValueAt(0, 2));
    }

    private static void testRowSelectionPopulatesDetailView() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();
        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);
        GuestService guestService = new GuestService(guestRepo);

        Guest g = new Guest("GST301", "333444555V", "Malini Fonseka", "0712223344", "malini@example.com");
        guestRepo.addGuest(g);
        Room r = new Room("301", RoomType.SUITE, BedType.KING, 4, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r);
        Reservation res = resService.createReservation(g, r, 2, LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 20));

        ReservationPanel panel = new ReservationPanel(resService, guestService);
        panel.searchReservations("Guest Name", "Malini");

        JTable table = panel.getReservationHistoryTable();
        table.setRowSelectionInterval(0, 0);

        JLabel lblId = (JLabel) getPrivateField(panel, "lblDetailBookingId");
        JLabel lblGuest = (JLabel) getPrivateField(panel, "lblDetailGuest");
        JLabel lblRoom = (JLabel) getPrivateField(panel, "lblDetailRoom");
        JLabel lblNights = (JLabel) getPrivateField(panel, "lblDetailNights");
        JLabel lblAdvance = (JLabel) getPrivateField(panel, "lblDetailAdvance");

        assertEquals("UI Test 5: Detail View displays Booking ID", res.getBookingId(), lblId.getText());
        assertTrue("UI Test 5: Detail View displays Guest name and ID", lblGuest.getText().contains("Malini Fonseka"));
        assertTrue("UI Test 5: Detail View displays Room 301", lblRoom.getText().contains("301"));
        assertTrue("UI Test 5: Detail View displays 4 nights", lblNights.getText().contains("4 nights"));
        assertTrue("UI Test 5: Detail View displays LKR 10,000", lblAdvance.getText().contains("10,000"));
    }

    private static void testShowAllAndClearSearchButtons() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();
        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);
        GuestService guestService = new GuestService(guestRepo);

        Guest g1 = new Guest("GST401", "444555666V", "Guest One", "0771110001", "g1@example.com");
        Guest g2 = new Guest("GST402", "555666777V", "Guest Two", "0771110002", "g2@example.com");
        guestRepo.addGuest(g1);
        guestRepo.addGuest(g2);
        Room r1 = new Room("401", RoomType.STANDARD, BedType.SINGLE, 2, RoomStatus.AVAILABLE);
        Room r2 = new Room("402", RoomType.DELUXE, BedType.DOUBLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);
        roomRepo.addRoom(r2);

        resService.createReservation(g1, r1, 1, LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18));
        resService.createReservation(g2, r2, 2, LocalDate.of(2026, 11, 19), LocalDate.of(2026, 11, 22));

        ReservationPanel panel = new ReservationPanel(resService, guestService);

        panel.searchReservations("Guest Name", "Guest One");
        assertEquals("UI Test 6: 1 row after search", 1, panel.getReservationTableModel().getRowCount());

        panel.showAllReservations();
        assertEquals("UI Test 6: 2 rows after Show All", 2, panel.getReservationTableModel().getRowCount());

        panel.searchReservations("Guest Name", "Guest Two");
        panel.clearSearch();
        assertEquals("UI Test 6: 2 rows after Clear Search", 2, panel.getReservationTableModel().getRowCount());
    }
}
