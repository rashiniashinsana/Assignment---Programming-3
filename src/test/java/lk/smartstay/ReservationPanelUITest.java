package lk.smartstay;

import lk.smartstay.enums.BedType;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Guest;
import lk.smartstay.model.Room;
import lk.smartstay.repository.GuestRepository;
import lk.smartstay.repository.ReservationRepository;
import lk.smartstay.repository.RoomRepository;
import lk.smartstay.service.GuestService;
import lk.smartstay.service.ReservationService;
import lk.smartstay.ui.MainFrame;
import lk.smartstay.ui.guest.ReservationPanel;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
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

public class ReservationPanelUITest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-11-13T04:30:00Z"),
            COLOMBO_ZONE
    );

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Phase 07 ReservationPanel UI Integration Tests");
        System.out.println("==================================================");

        testPanelInitializationAndDefaults();
        testGuestDropdownPopulationAndSelection();
        testRoomSearchAndTablePopulation();
        testRoomSelectionUpdatesSummaries();
        testClearFormResetsState();
        testMainFrameWiringForReservationPanel();

        System.out.println("==================================================");
        System.out.println("ReservationPanel UI Test Summary: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
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

    private static void testPanelInitializationAndDefaults() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();
        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);
        GuestService guestService = new GuestService(guestRepo);

        ReservationPanel panel = new ReservationPanel(resService, guestService);
        assertTrue("UI Test 1: ReservationPanel instantiated without error", panel != null);

        JTextField txtIn = (JTextField) getPrivateField(panel, "txtCheckInDate");
        JTextField txtOut = (JTextField) getPrivateField(panel, "txtCheckOutDate");
        JSpinner spinGuests = (JSpinner) getPrivateField(panel, "spinGuests");
        JLabel lblAdvance = (JLabel) getPrivateField(panel, "lblAdvanceVal");

        assertEquals("UI Test 1: Default check-in is 2 days in advance", "2026-11-15", txtIn.getText());
        assertEquals("UI Test 1: Default check-out is 5 days in advance", "2026-11-18", txtOut.getText());
        assertEquals("UI Test 1: Default guest count is 1", 1, spinGuests.getValue());
        assertTrue("UI Test 1: Advance deposit displays LKR 10,000", lblAdvance.getText().contains("10,000"));
    }

    private static void testGuestDropdownPopulationAndSelection() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();
        Guest g1 = new Guest("GST101", "111222333V", "Nimal Silva", "0711122334", "nimal@example.com");
        guestRepo.addGuest(g1);

        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);
        GuestService guestService = new GuestService(guestRepo);

        ReservationPanel panel = new ReservationPanel(resService, guestService);

        @SuppressWarnings("unchecked")
        JComboBox<String> cmbGuests = (JComboBox<String>) getPrivateField(panel, "cmbRegisteredGuests");
        assertEquals("UI Test 2: Dropdown has placeholder + 1 guest", 2, cmbGuests.getItemCount());

        cmbGuests.setSelectedIndex(1);

        JLabel lblName = (JLabel) getPrivateField(panel, "lblGuestNameVal");
        JLabel lblNic = (JLabel) getPrivateField(panel, "lblGuestNicVal");
        assertEquals("UI Test 2: Selected guest name updated in summary", "Nimal Silva", lblName.getText());
        assertEquals("UI Test 2: Selected guest NIC updated in summary", "111222333V", lblNic.getText());
    }

    private static void testRoomSearchAndTablePopulation() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();

        Room r1 = new Room("R501", RoomType.SUITE, BedType.KING, 4, RoomStatus.AVAILABLE);
        Room r2 = new Room("R502", RoomType.SUITE, BedType.KING, 4, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);
        roomRepo.addRoom(r2);

        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);
        GuestService guestService = new GuestService(guestRepo);

        ReservationPanel panel = new ReservationPanel(resService, guestService);

        @SuppressWarnings("unchecked")
        JComboBox<RoomType> cmbType = (JComboBox<RoomType>) getPrivateField(panel, "cmbRoomType");
        cmbType.setSelectedItem(RoomType.SUITE);

        JButton btnSearch = findButtonByText(panel, "Search Available Rooms");
        assertTrue("UI Test 3: Search Available Rooms button located", btnSearch != null);
        btnSearch.doClick();

        DefaultTableModel model = (DefaultTableModel) getPrivateField(panel, "tableModel");
        assertEquals("UI Test 3: Exactly 2 suites found", 2, model.getRowCount());
        assertEquals("UI Test 3: First row room ID is R501", "R501", model.getValueAt(0, 0));
        assertEquals("UI Test 3: Second row room ID is R502", "R502", model.getValueAt(1, 0));
    }

    private static void testRoomSelectionUpdatesSummaries() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();

        Room r1 = new Room("R601", RoomType.DELUXE, BedType.DOUBLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);

        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);
        GuestService guestService = new GuestService(guestRepo);

        ReservationPanel panel = new ReservationPanel(resService, guestService);

        @SuppressWarnings("unchecked")
        JComboBox<RoomType> cmbType = (JComboBox<RoomType>) getPrivateField(panel, "cmbRoomType");
        cmbType.setSelectedItem(RoomType.DELUXE);

        JButton btnSearch = findButtonByText(panel, "Search Available Rooms");
        btnSearch.doClick();

        JTable table = (JTable) getPrivateField(panel, "roomTable");
        table.setRowSelectionInterval(0, 0);

        JLabel lblRoomId = (JLabel) getPrivateField(panel, "lblSelectedRoomId");
        assertTrue("UI Test 4: Selected room label contains R601", lblRoomId.getText().contains("R601"));

        JLabel lblSummaryRoom = (JLabel) getPrivateField(panel, "lblSummaryRoom");
        assertTrue("UI Test 4: Booking overview card displays Room R601", lblSummaryRoom.getText().contains("R601"));

        JLabel lblNights = (JLabel) getPrivateField(panel, "lblNightsVal");
        assertTrue("UI Test 4: Stay duration displays 3 nights", lblNights.getText().contains("3 nights"));
    }

    private static void testClearFormResetsState() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        GuestRepository guestRepo = new GuestRepository();
        Room r1 = new Room("R701", RoomType.STANDARD, BedType.SINGLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);

        ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo, FIXED_CLOCK);
        GuestService guestService = new GuestService(guestRepo);

        ReservationPanel panel = new ReservationPanel(resService, guestService);
        JButton btnSearch = findButtonByText(panel, "Search Available Rooms");
        btnSearch.doClick();

        DefaultTableModel model = (DefaultTableModel) getPrivateField(panel, "tableModel");
        assertTrue("UI Test 5: Rows present before clear", model.getRowCount() > 0);

        JButton btnClear = findButtonByText(panel, "Clear Form");
        assertTrue("UI Test 5: Clear Form button found", btnClear != null);
        btnClear.doClick();

        assertEquals("UI Test 5: Table emptied after clear", 0, model.getRowCount());
        JLabel lblSelectedRoom = (JLabel) getPrivateField(panel, "lblSelectedRoomId");
        assertTrue("UI Test 5: Selected room cleared", lblSelectedRoom.getText().contains("None"));
    }

    private static void testMainFrameWiringForReservationPanel() {
        MainFrame frame = new MainFrame();
        assertTrue("UI Test 6: MainFrame initialized", frame != null);
        frame.dispose();
    }
}
