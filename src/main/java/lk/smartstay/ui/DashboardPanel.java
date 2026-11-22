package lk.smartstay.ui;

import lk.smartstay.enums.ReservationStatus;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Reservation;
import lk.smartstay.model.Room;
import lk.smartstay.repository.GuestRepository;
import lk.smartstay.repository.ReservationRepository;
import lk.smartstay.repository.RoomRepository;
import lk.smartstay.util.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;

    private JLabel lblTotalRoomsVal;
    private JLabel lblAvailableRoomsVal;
    private JLabel lblGuestsVal;
    private JLabel lblReservationsVal;
    private JLabel lblReservationsNote;

    private JLabel lblStandardRoomsCount;
    private JLabel lblDeluxeRoomsCount;
    private JLabel lblSuiteRoomsCount;
    private JLabel lblFamilySuiteRoomsCount;
    private JLabel lblMaintenanceRoomsCount;

    private JLabel lblConfirmedBookingsCount;
    private JLabel lblCancelledBookingsCount;
    private JLabel lblCompletedBookingsCount;
    private JLabel lblCustomerProfilesCount;

    public DashboardPanel(RoomRepository roomRepository,
                          GuestRepository guestRepository,
                          ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository != null ? roomRepository : new RoomRepository();
        this.guestRepository = guestRepository != null ? guestRepository : new GuestRepository();
        this.reservationRepository = reservationRepository != null ? reservationRepository : new ReservationRepository();

        initComponents();
        refreshMetrics();
    }

    public DashboardPanel() {
        this(new RoomRepository(), new GuestRepository(), new ReservationRepository());
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(createWelcomeBanner(), BorderLayout.NORTH);

        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setOpaque(false);

        contentContainer.add(createSummaryCardsPanel());
        contentContainer.add(Box.createVerticalStrut(18));
        contentContainer.add(createBreakdownOverviewPanel());
        contentContainer.add(Box.createVerticalStrut(18));
        contentContainer.add(createSystemOverviewPanel());

        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createWelcomeBanner() {
        JPanel banner = new JPanel();
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBackground(UIConstants.COLOR_CARD_BG);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JLabel titleLabel = new JLabel("SmartStay Hotel Booking & Operations Dashboard");
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel subtitleLabel = new JLabel("Live administrative summary — Real-time room inventory, registered guests, and booking records.");
        subtitleLabel.setFont(UIConstants.FONT_BODY);
        subtitleLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        banner.add(titleLabel);
        banner.add(Box.createVerticalStrut(4));
        banner.add(subtitleLabel);

        return banner;
    }

    private JPanel createSummaryCardsPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        gridPanel.setOpaque(false);
        gridPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 125));

        JPanel cardRooms = createMetricCardBase("Total Rooms", UIConstants.COLOR_PRIMARY);
        lblTotalRoomsVal = createMetricValueLabel("0");
        JLabel noteRooms = createMetricNoteLabel("Configured room inventory");
        cardRooms.add(lblTotalRoomsVal);
        cardRooms.add(Box.createVerticalStrut(4));
        cardRooms.add(noteRooms);
        gridPanel.add(cardRooms);

        JPanel cardAvailable = createMetricCardBase("Available Rooms", UIConstants.COLOR_SUCCESS);
        lblAvailableRoomsVal = createMetricValueLabel("0");
        lblAvailableRoomsVal.setForeground(new Color(21, 128, 61));
        JLabel noteAvailable = createMetricNoteLabel("Administrative status only (dates vary)");
        cardAvailable.add(lblAvailableRoomsVal);
        cardAvailable.add(Box.createVerticalStrut(4));
        cardAvailable.add(noteAvailable);
        gridPanel.add(cardAvailable);

        JPanel cardGuests = createMetricCardBase("Registered Guests", UIConstants.COLOR_ACCENT_GOLD);
        lblGuestsVal = createMetricValueLabel("0");
        JLabel noteGuests = createMetricNoteLabel("Customer database profiles");
        cardGuests.add(lblGuestsVal);
        cardGuests.add(Box.createVerticalStrut(4));
        cardGuests.add(noteGuests);
        gridPanel.add(cardGuests);

        JPanel cardReservations = createMetricCardBase("Total Reservations", new Color(79, 70, 229));
        lblReservationsVal = createMetricValueLabel("0");
        lblReservationsNote = createMetricNoteLabel("0 Confirmed · 0 Cancelled");
        cardReservations.add(lblReservationsVal);
        cardReservations.add(Box.createVerticalStrut(4));
        cardReservations.add(lblReservationsNote);
        gridPanel.add(cardReservations);

        return gridPanel;
    }

    private JPanel createMetricCardBase(String title, Color topAccent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIConstants.COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 1, 1, 1, topAccent),
                BorderFactory.createEmptyBorder(12, 16, 14, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIConstants.FONT_BODY_BOLD);
        titleLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        return card;
    }

    private JPanel createMetricCardBase(String title) {
        return createMetricCardBase(title, UIConstants.COLOR_CARD_BORDER);
    }

    private JLabel createMetricValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_CARD_VALUE);
        label.setForeground(UIConstants.COLOR_PRIMARY);
        return label;
    }

    private JLabel createMetricNoteLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_MUTED);
        label.setForeground(UIConstants.COLOR_TEXT_MUTED);
        return label;
    }

    private JPanel createBreakdownOverviewPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);

        JPanel cardLeft = new JPanel(new BorderLayout(0, 10));
        cardLeft.setBackground(UIConstants.COLOR_CARD_BG);
        cardLeft.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel titleLeft = new JLabel("Room Inventory Breakdown by Package");
        titleLeft.setFont(UIConstants.FONT_SECTION_TITLE);
        titleLeft.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        cardLeft.add(titleLeft, BorderLayout.NORTH);

        JPanel gridLeft = new JPanel(new GridBagLayout());
        gridLeft.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        addBreakdownRow(gridLeft, gbc, 0, "Standard Rooms (LKR 6,000):", lblStandardRoomsCount = createBoldValueLabel("0"));
        addBreakdownRow(gridLeft, gbc, 1, "Deluxe Rooms (LKR 9,000):", lblDeluxeRoomsCount = createBoldValueLabel("0"));
        addBreakdownRow(gridLeft, gbc, 2, "Suite Rooms (LKR 14,000):", lblSuiteRoomsCount = createBoldValueLabel("0"));
        addBreakdownRow(gridLeft, gbc, 3, "Family Suites (LKR 18,000):", lblFamilySuiteRoomsCount = createBoldValueLabel("0"));
        addBreakdownRow(gridLeft, gbc, 4, "Under Maintenance Rooms:", lblMaintenanceRoomsCount = createBoldValueLabel("0"));

        cardLeft.add(gridLeft, BorderLayout.CENTER);
        panel.add(cardLeft);

        JPanel cardRight = new JPanel(new BorderLayout(0, 10));
        cardRight.setBackground(UIConstants.COLOR_CARD_BG);
        cardRight.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel titleRight = new JLabel("Reservation Health & Guest Status");
        titleRight.setFont(UIConstants.FONT_SECTION_TITLE);
        titleRight.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        cardRight.add(titleRight, BorderLayout.NORTH);

        JPanel gridRight = new JPanel(new GridBagLayout());
        gridRight.setOpaque(false);

        addBreakdownRow(gridRight, gbc, 0, "Confirmed Reservations:", lblConfirmedBookingsCount = createColoredBoldLabel("0", new Color(21, 128, 61)));
        addBreakdownRow(gridRight, gbc, 1, "Cancelled Reservations:", lblCancelledBookingsCount = createColoredBoldLabel("0", new Color(220, 38, 38)));
        addBreakdownRow(gridRight, gbc, 2, "Completed Stays:", lblCompletedBookingsCount = createBoldValueLabel("0"));
        addBreakdownRow(gridRight, gbc, 3, "Registered Guest Profiles:", lblCustomerProfilesCount = createBoldValueLabel("0"));

        cardRight.add(gridRight, BorderLayout.CENTER);
        panel.add(cardRight);

        return panel;
    }

    private void addBreakdownRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JLabel valueLabel) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.7;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.COLOR_TEXT_MUTED);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(valueLabel, gbc);
    }

    private JLabel createBoldValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BODY_BOLD);
        label.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        return label;
    }

    private JLabel createColoredBoldLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BODY_BOLD);
        label.setForeground(color);
        return label;
    }

    private JPanel createSystemOverviewPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIConstants.COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JLabel sectionTitle = new JLabel("System Quick Navigation & Operational Rules");
        sectionTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        sectionTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        JLabel descLabel = new JLabel("<html>"
                + "<table cellpadding='3' cellspacing='0' style='font-size: 11px; color: #475569; width: 100%;'>"
                + "<tr><td style='width: 25%;'><b>Room Management:</b></td><td>Configure and maintain hotel room inventory, rates, capacities, and maintenance status.</td></tr>"
                + "<tr><td><b>Guest Registration:</b></td><td>Register local &amp; international guests with passport/NIC, email, and phone validation.</td></tr>"
                + "<tr><td><b>Search Rooms:</b></td><td>Real-time date range availability query using half-open [check-in, check-out) overlap detection.</td></tr>"
                + "<tr><td><b>Make Reservation:</b></td><td>Select registered guests, search rooms, enforce 2-day advance booking, and record advance deposits.</td></tr>"
                + "<tr><td><b>Reservation Management:</b></td><td>Search by ID, Guest Name, Check-in Date, inspect bookings, and enforce 24h cancellation rules.</td></tr>"
                + "<tr><td><b>Invoices &amp; Billing:</b></td><td>Generate itemized tax invoices with long-stay discounts (12%), extra guest surcharges, and advance credits.</td></tr>"
                + "</table>"
                + "</html>");

        panel.add(sectionTitle);
        panel.add(Box.createVerticalStrut(8));
        panel.add(descLabel);

        return panel;
    }

    public void refreshMetrics() {

        List<Room> allRooms = roomRepository.findAll();
        int totalRooms = allRooms.size();
        int availableRooms = 0;
        int standardCount = 0;
        int deluxeCount = 0;
        int suiteCount = 0;
        int familySuiteCount = 0;
        int maintenanceCount = 0;

        for (Room r : allRooms) {
            if (r.getStatus() == RoomStatus.AVAILABLE) {
                availableRooms++;
            } else if (r.getStatus() == RoomStatus.UNDER_MAINTENANCE) {
                maintenanceCount++;
            }

            if (r.getRoomType() == RoomType.STANDARD) standardCount++;
            else if (r.getRoomType() == RoomType.DELUXE) deluxeCount++;
            else if (r.getRoomType() == RoomType.SUITE) suiteCount++;
            else if (r.getRoomType() == RoomType.FAMILY_SUITE) familySuiteCount++;
        }

        lblTotalRoomsVal.setText(String.valueOf(totalRooms));
        lblAvailableRoomsVal.setText(String.valueOf(availableRooms));
        lblStandardRoomsCount.setText(standardCount + " rooms");
        lblDeluxeRoomsCount.setText(deluxeCount + " rooms");
        lblSuiteRoomsCount.setText(suiteCount + " rooms");
        lblFamilySuiteRoomsCount.setText(familySuiteCount + " rooms");
        lblMaintenanceRoomsCount.setText(maintenanceCount + " rooms");

        int guestCount = guestRepository.findAll().size();
        lblGuestsVal.setText(String.valueOf(guestCount));
        lblCustomerProfilesCount.setText(guestCount + " guests");

        List<Reservation> allReservations = reservationRepository.findAll();
        int totalReservations = allReservations.size();
        int confirmedCount = 0;
        int cancelledCount = 0;
        int completedCount = 0;

        for (Reservation res : allReservations) {
            if (res.getStatus() == ReservationStatus.CONFIRMED) {
                confirmedCount++;
            } else if (res.getStatus() == ReservationStatus.CANCELLED) {
                cancelledCount++;
            } else if (res.getStatus() == ReservationStatus.COMPLETED) {
                completedCount++;
            }
        }

        lblReservationsVal.setText(String.valueOf(totalReservations));
        lblReservationsNote.setText(confirmedCount + " Confirmed · " + cancelledCount + " Cancelled");
        lblConfirmedBookingsCount.setText(confirmedCount + " bookings");
        lblCancelledBookingsCount.setText(cancelledCount + " bookings");
        lblCompletedBookingsCount.setText(completedCount + " stays");
    }

    public JLabel getLblTotalRoomsVal() {
        return lblTotalRoomsVal;
    }

    public JLabel getLblAvailableRoomsVal() {
        return lblAvailableRoomsVal;
    }

    public JLabel getLblGuestsVal() {
        return lblGuestsVal;
    }

    public JLabel getLblReservationsVal() {
        return lblReservationsVal;
    }

    public int getTotalRoomsCount() {
        return roomRepository.findAll().size();
    }

    public int getAvailableRoomsCount() {
        int count = 0;
        for (Room r : roomRepository.findAll()) {
            if (r.getStatus() == RoomStatus.AVAILABLE) count++;
        }
        return count;
    }

    public int getRegisteredGuestsCount() {
        return guestRepository.findAll().size();
    }

    public int getTotalReservationsCount() {
        return reservationRepository.findAll().size();
    }
}
