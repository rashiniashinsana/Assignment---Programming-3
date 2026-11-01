package lk.smartstay.ui;

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

/**
 * Main application window for the SmartStay Hotel Booking System.
 * Organizes the application using a BorderLayout comprising a top header,
 * a left navigation sidebar, and a central CardLayout content area.
 */
public class MainFrame extends JFrame {

    // Card identifiers
    private static final String CARD_DASHBOARD = "CARD_DASHBOARD";
    private static final String CARD_ROOMS = "CARD_ROOMS";
    private static final String CARD_GUESTS = "CARD_GUESTS";
    private static final String CARD_SEARCH = "CARD_SEARCH";
    private static final String CARD_RESERVATIONS = "CARD_RESERVATIONS";
    private static final String CARD_INVOICES = "CARD_INVOICES";

    // UI Components
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel headerTitleLabel;
    private final List<JButton> navButtons = new ArrayList<>();

    public MainFrame() {
        initWindow();
        initComponents();
    }

    /**
     * Configures window properties (title, dimensions, centering).
     */
    private void initWindow() {
        setTitle("SmartStay Hotel Booking System");
        setSize(UIConstants.WINDOW_SIZE);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Initializes and arranges main UI components.
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Top Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Left Navigation Sidebar
        add(createSidebarPanel(), BorderLayout.WEST);

        // 3. Central Content Area using CardLayout
        add(createContentPanel(), BorderLayout.CENTER);

        // Set default view to Dashboard
        navigateTo(CARD_DASHBOARD, "Dashboard Overview");
        if (!navButtons.isEmpty()) {
            updateActiveButton(navButtons.get(0));
        }
    }

    /**
     * Creates the top header bar displaying application branding and active context.
     */
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

    /**
     * Creates the left navigation sidebar containing system module buttons.
     */
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(UIConstants.SIDEBAR_SIZE);
        sidebar.setBackground(UIConstants.COLOR_SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        // Sidebar Brand Logo/Title
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

        // Navigation Buttons Container
        JPanel navContainer = new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setOpaque(false);

        JButton btnDashboard = createNavButton("Dashboard", CARD_DASHBOARD, "Dashboard Overview");
        JButton btnRooms = createNavButton("Room Management", CARD_ROOMS, "Room Management");
        JButton btnGuests = createNavButton("Guest Registration", CARD_GUESTS, "Guest Registration");
        JButton btnSearch = createNavButton("Search Rooms", CARD_SEARCH, "Room Search & Availability");
        JButton btnReservations = createNavButton("Reservations", CARD_RESERVATIONS, "Reservations Management");
        JButton btnInvoices = createNavButton("Invoices", CARD_INVOICES, "Invoices & Billing");

        navContainer.add(btnDashboard);
        navContainer.add(Box.createVerticalStrut(8));
        navContainer.add(btnRooms);
        navContainer.add(Box.createVerticalStrut(8));
        navContainer.add(btnGuests);
        navContainer.add(Box.createVerticalStrut(8));
        navContainer.add(btnSearch);
        navContainer.add(Box.createVerticalStrut(8));
        navContainer.add(btnReservations);
        navContainer.add(Box.createVerticalStrut(8));
        navContainer.add(btnInvoices);

        sidebar.add(navContainer, BorderLayout.CENTER);

        // Bottom Exit Button
        JButton btnExit = createExitButton();
        sidebar.add(btnExit, BorderLayout.SOUTH);

        return sidebar;
    }

    /**
     * Constructs a styled navigation button with active state management.
     */
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

    /**
     * Constructs the Exit button.
     */
    private JButton createExitButton() {
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(UIConstants.FONT_NAV_BUTTON);
        exitButton.setForeground(UIConstants.COLOR_TEXT_LIGHT);
        exitButton.setBackground(UIConstants.COLOR_SIDEBAR_BG);
        exitButton.setFocusPainted(false);
        exitButton.setBorderPainted(false);
        exitButton.setHorizontalAlignment(SwingConstants.LEFT);
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitButton.setPreferredSize(UIConstants.NAV_BUTTON_SIZE);
        exitButton.setMaximumSize(UIConstants.NAV_BUTTON_SIZE);
        exitButton.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        exitButton.addActionListener(e -> handleExit());
        return exitButton;
    }

    /**
     * Creates the central CardLayout container and registers all application panels.
     */
    private JPanel createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIConstants.COLOR_BACKGROUND);

        // Register panels
        contentPanel.add(new DashboardPanel(), CARD_DASHBOARD);
        contentPanel.add(new RoomManagementPanel(), CARD_ROOMS);
        contentPanel.add(new GuestRegistrationPanel(), CARD_GUESTS);
        contentPanel.add(new RoomSearchPanel(), CARD_SEARCH);
        contentPanel.add(new ReservationPanel(), CARD_RESERVATIONS);
        contentPanel.add(new InvoicePanel(), CARD_INVOICES);

        return contentPanel;
    }

    /**
     * Switches the active card in the content panel.
     */
    private void navigateTo(String cardName, String headerTitle) {
        cardLayout.show(contentPanel, cardName);
        if (headerTitleLabel != null) {
            headerTitleLabel.setText(headerTitle);
        }
    }

    /**
     * Updates visual styling of sidebar buttons to reflect current selection.
     */
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

    /**
     * Handles graceful application exit with user confirmation.
     */
    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit the SmartStay Hotel Booking System?",
                "Exit Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
}
