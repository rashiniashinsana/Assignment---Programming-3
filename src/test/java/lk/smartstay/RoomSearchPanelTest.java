package lk.smartstay;

import lk.smartstay.enums.BedType;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Room;
import lk.smartstay.repository.ReservationRepository;
import lk.smartstay.repository.RoomRepository;
import lk.smartstay.service.ReservationService;
import lk.smartstay.ui.MainFrame;
import lk.smartstay.ui.guest.RoomSearchPanel;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

public class RoomSearchPanelTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Phase 06 UI Component Integration Tests");
        System.out.println("==================================================");

        testPanelInitializationAndDefaults();
        testTableColumnsAndNonEditable();
        testSearchActionAndTablePopulation();
        testRowSelectionAndSummary();
        testClearAction();
        testMainFrameWiring();

        System.out.println("==================================================");
        System.out.println("UI Test Summary: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
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

    private static void testPanelInitializationAndDefaults() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        RoomSearchPanel panel = new RoomSearchPanel(service);
        assertTrue("UI Test 1: Panel instantiated without errors", panel != null);

        @SuppressWarnings("unchecked")
        JComboBox<RoomType> cmbRoomType = (JComboBox<RoomType>) getPrivateField(panel, "cmbRoomType");
        JTextField txtCheckIn = (JTextField) getPrivateField(panel, "txtCheckInDate");
        JTextField txtCheckOut = (JTextField) getPrivateField(panel, "txtCheckOutDate");

        assertTrue("UI Test 1: RoomType combobox populated", cmbRoomType.getItemCount() == RoomType.values().length);
        assertEquals("UI Test 1: Default check-in date is today", LocalDate.now().toString(), txtCheckIn.getText());
        assertEquals("UI Test 1: Default check-out date is tomorrow", LocalDate.now().plusDays(1).toString(), txtCheckOut.getText());
    }

    private static void testTableColumnsAndNonEditable() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        RoomSearchPanel panel = new RoomSearchPanel(service);
        JTable table = (JTable) getPrivateField(panel, "roomTable");
        DefaultTableModel model = (DefaultTableModel) getPrivateField(panel, "tableModel");

        String[] expectedCols = {
                "Room ID", "Room Type", "Bed Type", "Capacity",
                "Daily Rate (LKR)", "Breakfast", "Tax Rate", "Status"
        };

        assertEquals("UI Test 2: Exactly 8 table columns", 8, model.getColumnCount());
        for (int i = 0; i < expectedCols.length; i++) {
            assertEquals("UI Test 2: Column " + i + " name", expectedCols[i], model.getColumnName(i));
        }

        model.addRow(new Object[]{"101", "Standard", "Single", 2, "6,000.00", "No", "10%", "Available"});
        for (int c = 0; c < 8; c++) {
            assertTrue("UI Test 2: Cell (" + 0 + ", " + c + ") is non-editable", !model.isCellEditable(0, c));
        }
        model.setRowCount(0);
    }

    private static void testSearchActionAndTablePopulation() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room r1 = new Room("R201", RoomType.DELUXE, BedType.DOUBLE, 2, RoomStatus.AVAILABLE);
        Room r2 = new Room("R202", RoomType.DELUXE, BedType.QUEEN, 3, RoomStatus.AVAILABLE);
        Room r3 = new Room("R203", RoomType.STANDARD, BedType.SINGLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);
        roomRepo.addRoom(r2);
        roomRepo.addRoom(r3);

        RoomSearchPanel panel = new RoomSearchPanel(service);

        @SuppressWarnings("unchecked")
        JComboBox<RoomType> cmbRoomType = (JComboBox<RoomType>) getPrivateField(panel, "cmbRoomType");
        cmbRoomType.setSelectedItem(RoomType.DELUXE);

        JTextField txtCheckIn = (JTextField) getPrivateField(panel, "txtCheckInDate");
        JTextField txtCheckOut = (JTextField) getPrivateField(panel, "txtCheckOutDate");
        txtCheckIn.setText(LocalDate.now().plusDays(2).toString());
        txtCheckOut.setText(LocalDate.now().plusDays(5).toString());

        JButton searchButton = findButtonByText(panel, "Search Rooms");
        assertTrue("UI Test 3: Search Rooms button found", searchButton != null);
        searchButton.doClick();

        DefaultTableModel model = (DefaultTableModel) getPrivateField(panel, "tableModel");
        assertEquals("UI Test 3: Results table contains 2 deluxe rooms", 2, model.getRowCount());
        assertEquals("UI Test 3: First result Room ID is R201", "R201", model.getValueAt(0, 0));
        assertEquals("UI Test 3: Second result Room ID is R202", "R202", model.getValueAt(1, 0));
    }

    private static void testRowSelectionAndSummary() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room r1 = new Room("R301", RoomType.SUITE, BedType.KING, 4, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);

        RoomSearchPanel panel = new RoomSearchPanel(service);

        @SuppressWarnings("unchecked")
        JComboBox<RoomType> cmbRoomType = (JComboBox<RoomType>) getPrivateField(panel, "cmbRoomType");
        cmbRoomType.setSelectedItem(RoomType.SUITE);

        JTextField txtCheckIn = (JTextField) getPrivateField(panel, "txtCheckInDate");
        JTextField txtCheckOut = (JTextField) getPrivateField(panel, "txtCheckOutDate");
        txtCheckIn.setText(LocalDate.now().plusDays(3).toString());
        txtCheckOut.setText(LocalDate.now().plusDays(6).toString());

        JButton searchButton = findButtonByText(panel, "Search Rooms");
        searchButton.doClick();

        JTable table = (JTable) getPrivateField(panel, "roomTable");
        table.setRowSelectionInterval(0, 0);

        JLabel lblRoomId = (JLabel) getPrivateField(panel, "lblSelectedRoomId");
        JLabel lblRoomType = (JLabel) getPrivateField(panel, "lblSelectedRoomType");
        JLabel lblCapacity = (JLabel) getPrivateField(panel, "lblSelectedRoomCapacity");
        JLabel lblNights = (JLabel) getPrivateField(panel, "lblSelectedRoomNights");

        assertEquals("UI Test 4: Selected room ID displayed", "R301", lblRoomId.getText());
        assertEquals("UI Test 4: Selected room type displayed", "Suite", lblRoomType.getText());
        assertEquals("UI Test 4: Selected room capacity displayed", "4 Guests", lblCapacity.getText());
        assertTrue("UI Test 4: Selected room duration displayed 3 nights", lblNights.getText().contains("3 nights"));
    }

    private static void testClearAction() {
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        ReservationService service = new ReservationService(roomRepo, resRepo);

        Room r1 = new Room("R401", RoomType.STANDARD, BedType.SINGLE, 2, RoomStatus.AVAILABLE);
        roomRepo.addRoom(r1);

        RoomSearchPanel panel = new RoomSearchPanel(service);
        JButton searchButton = findButtonByText(panel, "Search Rooms");
        searchButton.doClick();

        DefaultTableModel model = (DefaultTableModel) getPrivateField(panel, "tableModel");
        assertTrue("UI Test 5: Rows present before clear", model.getRowCount() > 0);

        JButton clearButton = findButtonByText(panel, "Clear");
        assertTrue("UI Test 5: Clear button found", clearButton != null);
        clearButton.doClick();

        assertEquals("UI Test 5: Table emptied after clear", 0, model.getRowCount());
        JLabel lblRoomId = (JLabel) getPrivateField(panel, "lblSelectedRoomId");
        assertEquals("UI Test 5: Summary cleared", "None", lblRoomId.getText());
    }

    private static void testMainFrameWiring() {
        MainFrame frame = new MainFrame();
        assertTrue("UI Test 6: MainFrame initialized", frame != null);

        ReservationService resService = (ReservationService) getPrivateField(frame, "reservationService");
        assertTrue("UI Test 6: ReservationService injected into MainFrame", resService != null);
        assertTrue("UI Test 6: RoomRepository shared in ReservationService", resService.getRoomRepository() != null);
        assertTrue("UI Test 6: ReservationRepository shared in ReservationService", resService.getReservationRepository() != null);

        frame.dispose();
    }

    private static JButton findButtonByText(Container container, String text) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton btn && text.equals(btn.getText())) {
                return btn;
            }
            if (c instanceof Container childContainer) {
                JButton found = findButtonByText(childContainer, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
