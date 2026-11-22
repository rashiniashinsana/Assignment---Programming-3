package lk.smartstay.ui;

import lk.smartstay.repository.GuestRepository;
import lk.smartstay.repository.ReservationRepository;
import lk.smartstay.repository.RoomRepository;
import lk.smartstay.service.GuestService;
import lk.smartstay.service.InvoiceService;
import lk.smartstay.service.ReservationService;
import lk.smartstay.service.RoomService;
import lk.smartstay.ui.admin.RoomManagementPanel;
import lk.smartstay.ui.guest.GuestRegistrationPanel;
import lk.smartstay.ui.guest.InvoicePanel;
import lk.smartstay.ui.guest.ReservationPanel;
import lk.smartstay.ui.guest.RoomSearchPanel;
import lk.smartstay.util.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private static final String CARD_DASHBOARD = "CARD_DASHBOARD";
    private static final String CARD_ROOMS = "CARD_ROOMS";
    private static final String CARD_GUESTS = "CARD_GUESTS";
    private static final String CARD_SEARCH = "CARD_SEARCH";
    private static final String CARD_RESERVATIONS = "CARD_RESERVATIONS";
    private static final String CARD_INVOICES = "CARD_INVOICES";

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel headerTitleLabel;
    private final List<JButton> navButtons = new ArrayList<>();
    private JButton btnExit;

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;
    private final RoomService roomService;
    private final GuestService guestService;
    private final ReservationService reservationService;
    private final InvoiceService invoiceService;

    private DashboardPanel dashboardPanel;
    private RoomManagementPanel roomManagementPanel;
    private GuestRegistrationPanel guestRegistrationPanel;
    private RoomSearchPanel roomSearchPanel;
    private ReservationPanel reservationPanel;
    private InvoicePanel invoicePanel;

    public MainFrame() {
        this.roomRepository = new RoomRepository();
        this.guestRepository = new GuestRepository();
        this.reservationRepository = new ReservationRepository();
        this.roomService = new RoomService(roomRepository, reservationRepository);
        this.guestService = new GuestService(guestRepository);
        this.reservationService = new ReservationService(roomRepository, reservationRepository, guestRepository);
        this.invoiceService = new InvoiceService(reservationRepository);
        initWindow();
        initComponents();
    }

    private void initWindow() {
        setTitle("SmartStay Hotel Booking System");
        setSize(UIConstants.WINDOW_SIZE);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);

        add(createSidebarPanel(), BorderLayout.WEST);

        add(createContentPanel(), BorderLayout.CENTER);

        navigateTo(CARD_DASHBOARD, "Dashboard Overview");
        if (!navButtons.isEmpty()) {
            updateActiveButton(navButtons.get(0));
        }
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(UIConstants.HEADER_SIZE);
        header.setBackground(UIConstants.COLOR_CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.COLOR_CARD_BORDER),
                BorderFactory.createEmptyBorder(0, 24, 0, 24)
        ));

        headerTitleLabel = new JLabel("Dashboard Overview");
        headerTitleLabel.setFont(UIConstants.FONT_SECTION_TITLE);
        headerTitleLabel.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        JLabel rightTagLabel = new JLabel("CI6115 Programming III | In-Memory Mode");
        rightTagLabel.setFont(UIConstants.FONT_MUTED);
        rightTagLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        header.add(headerTitleLabel, BorderLayout.WEST);
        header.add(rightTagLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(UIConstants.SIDEBAR_SIZE);
        sidebar.setBackground(UIConstants.COLOR_SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 20, 8));

        JLabel brandTitle = new JLabel("SmartStay");
        brandTitle.setFont(UIConstants.FONT_APP_TITLE);
        brandTitle.setForeground(UIConstants.COLOR_TEXT_LIGHT);

        JLabel brandSubtitle = new JLabel("HOTEL BOOKING SYSTEM");
        brandSubtitle.setFont(UIConstants.FONT_MUTED);
        brandSubtitle.setForeground(UIConstants.COLOR_TEXT_MUTED);

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createVerticalStrut(3));
        brandPanel.add(brandSubtitle);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        JPanel navContainer = new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setOpaque(false);

        JButton btnDashboard = createNavButton("Dashboard", CARD_DASHBOARD, "Dashboard Overview");
        JButton btnRooms = createNavButton("Room Management", CARD_ROOMS, "Room Management");
        JButton btnGuests = createNavButton("Guest Registration", CARD_GUESTS, "Guest Registration");
        JButton btnSearch = createNavButton("Search Rooms", CARD_SEARCH, "Room Search & Availability");
        JButton btnMakeReservation = createNavButtonWithAction("Make Reservation", () -> {
            navigateTo(CARD_RESERVATIONS, "Make Reservation");
            if (reservationPanel != null) {
                reservationPanel.setSelectedTab(0);
                reservationPanel.populateGuestDropdown();
            }
        });
        JButton btnReservationMgmt = createNavButtonWithAction("Reservation Management", () -> {
            navigateTo(CARD_RESERVATIONS, "Reservation Management & Search");
            if (reservationPanel != null) {
                reservationPanel.setSelectedTab(1);
                reservationPanel.refreshReservationHistoryTable();
            }
        });
        JButton btnInvoices = createNavButton("Invoices", CARD_INVOICES, "Invoices & Billing");

        navContainer.add(btnDashboard);
        navContainer.add(Box.createVerticalStrut(6));
        navContainer.add(btnRooms);
        navContainer.add(Box.createVerticalStrut(6));
        navContainer.add(btnGuests);
        navContainer.add(Box.createVerticalStrut(6));
        navContainer.add(btnSearch);
        navContainer.add(Box.createVerticalStrut(6));
        navContainer.add(btnMakeReservation);
        navContainer.add(Box.createVerticalStrut(6));
        navContainer.add(btnReservationMgmt);
        navContainer.add(Box.createVerticalStrut(6));
        navContainer.add(btnInvoices);

        sidebar.add(navContainer, BorderLayout.CENTER);

        JButton btnExit = createExitButton();
        sidebar.add(btnExit, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName, String headerTitle) {
        JButton button = new JButton(text);
        button.setFont(UIConstants.FONT_NAV_BUTTON);
        button.setForeground(UIConstants.COLOR_TEXT_LIGHT);
        button.setBackground(UIConstants.COLOR_SIDEBAR_BG);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(UIConstants.NAV_BUTTON_SIZE);
        button.setMaximumSize(UIConstants.NAV_BUTTON_SIZE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        button.addActionListener(e -> {
            navigateTo(cardName, headerTitle);
            updateActiveButton(button);
        });

        navButtons.add(button);
        return button;
    }

    private JButton createNavButtonWithAction(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(UIConstants.FONT_NAV_BUTTON);
        button.setForeground(UIConstants.COLOR_TEXT_LIGHT);
        button.setBackground(UIConstants.COLOR_SIDEBAR_BG);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(UIConstants.NAV_BUTTON_SIZE);
        button.setMaximumSize(UIConstants.NAV_BUTTON_SIZE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        button.addActionListener(e -> {
            action.run();
            updateActiveButton(button);
        });

        navButtons.add(button);
        return button;
    }

    private JButton createExitButton() {
        btnExit = new JButton("Exit");
        btnExit.setFont(UIConstants.FONT_NAV_BUTTON);
        btnExit.setForeground(UIConstants.COLOR_TEXT_LIGHT);
        btnExit.setBackground(UIConstants.COLOR_SIDEBAR_BG);
        btnExit.setFocusPainted(false);
        btnExit.setBorderPainted(false);
        btnExit.setHorizontalAlignment(SwingConstants.LEFT);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.setPreferredSize(UIConstants.NAV_BUTTON_SIZE);
        btnExit.setMaximumSize(UIConstants.NAV_BUTTON_SIZE);
        btnExit.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        btnExit.addActionListener(e -> handleExit());
        navButtons.add(btnExit);
        return btnExit;
    }

    private JPanel createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIConstants.COLOR_BACKGROUND);

        dashboardPanel = new DashboardPanel(roomRepository, guestRepository, reservationRepository);
        roomManagementPanel = new RoomManagementPanel(roomService);
        guestRegistrationPanel = new GuestRegistrationPanel(guestService);
        roomSearchPanel = new RoomSearchPanel(reservationService);
        reservationPanel = new ReservationPanel(reservationService, guestService);
        invoicePanel = new InvoicePanel(invoiceService, reservationService);

        contentPanel.add(dashboardPanel, CARD_DASHBOARD);
        contentPanel.add(roomManagementPanel, CARD_ROOMS);
        contentPanel.add(guestRegistrationPanel, CARD_GUESTS);
        contentPanel.add(roomSearchPanel, CARD_SEARCH);
        contentPanel.add(reservationPanel, CARD_RESERVATIONS);
        contentPanel.add(invoicePanel, CARD_INVOICES);

        return contentPanel;
    }

    private void navigateTo(String cardName, String headerTitle) {
        cardLayout.show(contentPanel, cardName);
        if (headerTitleLabel != null) {
            headerTitleLabel.setText(headerTitle);
        }

        if (CARD_DASHBOARD.equals(cardName) && dashboardPanel != null) {
            dashboardPanel.refreshMetrics();
        } else if (CARD_ROOMS.equals(cardName) && roomManagementPanel != null) {
            roomManagementPanel.refreshTableData();
        } else if (CARD_GUESTS.equals(cardName) && guestRegistrationPanel != null) {
            guestRegistrationPanel.refreshTableData();
        } else if (CARD_RESERVATIONS.equals(cardName) && reservationPanel != null) {
            reservationPanel.populateGuestDropdown();
            reservationPanel.refreshReservationHistoryTable();
        } else if (CARD_INVOICES.equals(cardName) && invoicePanel != null) {
            invoicePanel.populateEligibleBookingsDropdown();
        }
    }

    private void updateActiveButton(JButton activeButton) {
        for (JButton btn : navButtons) {
            if (btn == activeButton) {
                btn.setBackground(UIConstants.COLOR_SIDEBAR_ACTIVE);
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, UIConstants.COLOR_PRIMARY_HOVER),
                        BorderFactory.createEmptyBorder(10, 12, 10, 16)
                ));
            } else {
                btn.setBackground(UIConstants.COLOR_SIDEBAR_BG);
                btn.setForeground(UIConstants.COLOR_TEXT_LIGHT);
                btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            }
        }
    }

    public InvoiceService getInvoiceService() {
        return invoiceService;
    }

    public void navigateToCard(String cardName, String headerTitle) {
        navigateTo(cardName, headerTitle);
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public RoomManagementPanel getRoomManagementPanel() {
        return roomManagementPanel;
    }

    public GuestRegistrationPanel getGuestRegistrationPanel() {
        return guestRegistrationPanel;
    }

    public RoomSearchPanel getRoomSearchPanel() {
        return roomSearchPanel;
    }

    public ReservationPanel getReservationPanel() {
        return reservationPanel;
    }

    public InvoicePanel getInvoicePanel() {
        return invoicePanel;
    }

    public List<JButton> getNavButtons() {
        return navButtons;
    }

    public JButton getBtnExit() {
        return btnExit;
    }

    public RoomRepository getRoomRepository() {
        return roomRepository;
    }

    public GuestRepository getGuestRepository() {
        return guestRepository;
    }

    public ReservationRepository getReservationRepository() {
        return reservationRepository;
    }

    private void handleExit() {
        int choice = JOptionPane.YES_OPTION;
        if (isShowing()) {
            choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to exit the SmartStay Hotel Booking System?",
                    "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
        }

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
}
