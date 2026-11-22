package lk.smartstay.util;

import lk.smartstay.enums.BedType;
import lk.smartstay.enums.ReservationStatus;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Guest;
import lk.smartstay.model.Reservation;
import lk.smartstay.model.Room;
import lk.smartstay.repository.DatabaseManager;
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

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;

public class ScreenshotGenerator {

    private static final int PANEL_WIDTH = 1060;
    private static final int PANEL_HEIGHT = 700;
    private static final int FRAME_WIDTH = 1260;
    private static final int FRAME_HEIGHT = 780;

    public static void main(String[] args) {
        System.out.println("Starting SmartStay GUI Screenshot Generation...");
        File outputDir = new File("docs/screenshots");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try {
            RoomRepository roomRepo = new RoomRepository();
            GuestRepository guestRepo = new GuestRepository();
            ReservationRepository resRepo = new ReservationRepository();

            populateRichData(roomRepo, guestRepo, resRepo);

            DatabaseManager.getInstance().save(roomRepo, guestRepo, resRepo);

            RoomService roomService = new RoomService(roomRepo, resRepo);
            GuestService guestService = new GuestService(guestRepo);
            ReservationService resService = new ReservationService(roomRepo, resRepo, guestRepo);
            InvoiceService invoiceService = new InvoiceService(resRepo);

            captureComponent(new DashboardPanel(roomRepo, guestRepo, resRepo),
                    PANEL_WIDTH, PANEL_HEIGHT, new File(outputDir, "01_dashboard.png"));

            RoomManagementPanel roomPanel = new RoomManagementPanel(roomService);
            captureComponent(roomPanel, PANEL_WIDTH, PANEL_HEIGHT, new File(outputDir, "02_room_management.png"));

            GuestRegistrationPanel guestPanel = new GuestRegistrationPanel(guestService);
            captureComponent(guestPanel, PANEL_WIDTH, PANEL_HEIGHT, new File(outputDir, "03_guest_registration.png"));

            RoomSearchPanel searchPanel = new RoomSearchPanel(resService);
            searchPanel.getTxtCheckInDate().setText(LocalDate.now().plusDays(4).toString());
            searchPanel.getTxtCheckOutDate().setText(LocalDate.now().plusDays(8).toString());
            searchPanel.executeSearch();
            captureComponent(searchPanel, PANEL_WIDTH, PANEL_HEIGHT, new File(outputDir, "04_room_search.png"));

            ReservationPanel resPanel = new ReservationPanel(resService, guestService);
            captureComponent(resPanel, PANEL_WIDTH, PANEL_HEIGHT, new File(outputDir, "05_make_reservation.png"));

            resPanel.getTabbedPane().setSelectedIndex(1);
            captureComponent(resPanel, PANEL_WIDTH, PANEL_HEIGHT, new File(outputDir, "06_reservation_management.png"));

            InvoicePanel invPanel = new InvoicePanel(invoiceService, resService);
            invPanel.selectBookingByIdForTesting("BKG001");
            invPanel.handleGenerateInvoice();
            captureComponent(invPanel, PANEL_WIDTH, PANEL_HEIGHT, new File(outputDir, "07_invoice_view.png"));

            MainFrame mainFrame = new MainFrame(roomRepo, guestRepo, resRepo);
            captureFrame(mainFrame, FRAME_WIDTH, FRAME_HEIGHT, new File(outputDir, "08_mainframe_overview.png"));

            System.out.println("All 8 GUI screenshots successfully generated in docs/screenshots/!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void populateRichData(RoomRepository roomRepo, GuestRepository guestRepo,
            ReservationRepository resRepo) {
        Room r101 = new Room("R101", RoomType.STANDARD, BedType.SINGLE, 1, RoomStatus.AVAILABLE);
        Room r102 = new Room("R102", RoomType.STANDARD, BedType.DOUBLE, 2, RoomStatus.AVAILABLE);
        Room r201 = new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE);
        Room r202 = new Room("R202", RoomType.DELUXE, BedType.QUEEN, 2, RoomStatus.AVAILABLE);
        Room r301 = new Room("R301", RoomType.SUITE, BedType.KING, 3, RoomStatus.AVAILABLE);
        Room r302 = new Room("R302", RoomType.SUITE, BedType.QUEEN, 3, RoomStatus.UNDER_MAINTENANCE);
        Room r401 = new Room("R401", RoomType.FAMILY_SUITE, BedType.KING, 4, RoomStatus.AVAILABLE);
        Room r402 = new Room("R402", RoomType.FAMILY_SUITE, BedType.KING, 4, RoomStatus.AVAILABLE);

        roomRepo.addRoom(r101);
        roomRepo.addRoom(r102);
        roomRepo.addRoom(r201);
        roomRepo.addRoom(r202);
        roomRepo.addRoom(r301);
        roomRepo.addRoom(r302);
        roomRepo.addRoom(r401);
        roomRepo.addRoom(r402);

        Guest g1 = new Guest("G001", "901234567V", "Sunil Fernando", "0771234567", "sunil@example.com");
        Guest g2 = new Guest("G002", "959876543V", "Nimal Perera", "0719876543", "nimal@example.com");
        Guest g3 = new Guest("G003", "PA1234567", "Alice Smith", "+447911123456", "alice@example.com");
        Guest g4 = new Guest("G004", "882345678V", "Kamal Jayawardena", "0755554433", "kamal@example.com");

        guestRepo.addGuest(g1);
        guestRepo.addGuest(g2);
        guestRepo.addGuest(g3);
        guestRepo.addGuest(g4);

        LocalDate now = LocalDate.now();
        Reservation res1 = new Reservation("BKG001", g1, r201, 2, now.plusDays(4), now.plusDays(9), 10000.0,
                ReservationStatus.CONFIRMED);
        Reservation res2 = new Reservation("BKG002", g2, r301, 3, now.plusDays(10), now.plusDays(13), 10000.0,
                ReservationStatus.CONFIRMED);
        Reservation res3 = new Reservation("BKG003", g3, r401, 4, now.plusDays(15), now.plusDays(21), 10000.0,
                ReservationStatus.CONFIRMED);

        resRepo.addReservation(res1);
        resRepo.addReservation(res2);
        resRepo.addReservation(res3);
    }

    private static void captureComponent(JComponent comp, int width, int height, File file) throws Exception {
        JFrame dummyFrame = new JFrame();
        dummyFrame.setUndecorated(true);
        dummyFrame.getContentPane().add(comp);
        dummyFrame.pack();
        dummyFrame.setSize(new Dimension(width, height));
        comp.setSize(new Dimension(width, height));
        comp.doLayout();
        dummyFrame.doLayout();
        dummyFrame.validate();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        comp.paint(g2);
        g2.dispose();
        dummyFrame.dispose();

        ImageIO.write(image, "PNG", file);
        System.out.println("Generated: " + file.getName() + " (" + width + "x" + height + ")");
    }

    private static void captureFrame(JFrame frame, int width, int height, File file) throws Exception {
        frame.setSize(new Dimension(width, height));
        frame.getContentPane().setSize(new Dimension(width, height));
        frame.doLayout();
        frame.getContentPane().doLayout();
        frame.validate();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        frame.getContentPane().paint(g2);
        g2.dispose();
        frame.dispose();

        ImageIO.write(image, "PNG", file);
        System.out.println("Generated: " + file.getName() + " (" + width + "x" + height + ")");
    }
}
