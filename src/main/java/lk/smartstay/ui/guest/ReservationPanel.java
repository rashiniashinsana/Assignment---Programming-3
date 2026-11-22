package lk.smartstay.ui.guest;

import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Guest;
import lk.smartstay.model.Reservation;
import lk.smartstay.model.Room;
import lk.smartstay.service.GuestService;
import lk.smartstay.service.ReservationService;
import lk.smartstay.util.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ReservationPanel extends JPanel {

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final DateTimeFormatter STRICT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final ReservationService reservationService;
    private final GuestService guestService;

    private JTabbedPane tabbedPane;

    private JTextField txtGuestSearch;
    private JComboBox<String> cmbGuestCriteria;
    private JComboBox<String> cmbRegisteredGuests;
    private JLabel lblGuestIdVal;
    private JLabel lblGuestNameVal;
    private JLabel lblGuestNicVal;
    private JLabel lblGuestContactVal;
    private JLabel lblGuestEmailVal;
    private Guest selectedGuest = null;

    private JComboBox<RoomType> cmbRoomType;
    private JTextField txtCheckInDate;
    private JTextField txtCheckOutDate;
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JLabel lblTableSummary;
    private JLabel lblSelectedRoomId;
    private JLabel lblSelectedRoomType;
    private JLabel lblSelectedRoomBed;
    private JLabel lblSelectedRoomCapacity;
    private JLabel lblSelectedRoomRate;
    private Room selectedRoom = null;
    private List<Room> currentAvailableRooms = new ArrayList<>();
    private boolean hasSearchedRooms = false;

    private JSpinner spinGuests;
    private JLabel lblNightsVal;
    private JLabel lblAdvanceVal;
    private JLabel lblSummaryGuest;
    private JLabel lblSummaryRoom;
    private JLabel lblSummaryDates;
    private JLabel lblSummaryAdvance;

    private JComboBox<String> cmbSearchBy;
    private JTextField txtSearchValue;
    private JButton btnSearch;
    private JButton btnClearSearch;
    private JButton btnShowAll;
    private JTable reservationTable;
    private DefaultTableModel reservationTableModel;
    private JLabel lblReservationTableSummary;
    private List<Reservation> currentReservationSearchResults = new ArrayList<>();

    private JLabel lblDetailBookingId;
    private JLabel lblDetailStatus;
    private JLabel lblDetailGuest;
    private JLabel lblDetailRoom;
    private JLabel lblDetailCheckIn;
    private JLabel lblDetailCheckOut;
    private JLabel lblDetailNights;
    private JLabel lblDetailGuests;
    private JLabel lblDetailAdvance;
    private JLabel lblDetailCancellationEligibility;
    private JButton btnCancelReservation;

    private JLabel lblStatusFeedback;

    public ReservationPanel(ReservationService reservationService, GuestService guestService) {
        if (reservationService == null) {
            throw new IllegalArgumentException("ReservationService cannot be null.");
        }
        if (guestService == null) {
            throw new IllegalArgumentException("GuestService cannot be null.");
        }
        this.reservationService = reservationService;
        this.guestService = guestService;

        initComponents();
        populateGuestDropdown();
        refreshReservationHistoryTable();
    }

    public ReservationPanel() {
        this(new ReservationService(), new GuestService(new lk.smartstay.repository.GuestRepository()));
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIConstants.FONT_BODY_BOLD);
        tabbedPane.setBackground(UIConstants.COLOR_CARD_BG);

        tabbedPane.addTab("Create Reservation", createBookingWorkflowPanel());
        tabbedPane.addTab("Search & View Reservations", createSearchAndManagementPanel());

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                refreshReservationHistoryTable();
                updateStatusFeedback("Reservation directory loaded. Search by ID, Guest Name, or Check-in Date.", false);
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(UIConstants.COLOR_CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JLabel titleLabel = new JLabel("Reservations Management & Booking");
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel descLabel = new JLabel("Guest Services Module — Create new bookings and search, filter, and inspect reservation records.");
        descLabel.setFont(UIConstants.FONT_BODY);
        descLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(3));
        header.add(descLabel);

        return header;
    }

    private JPanel createBookingWorkflowPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 4, 4, 4));

        panel.add(createBookingFormCard(), BorderLayout.WEST);
        panel.add(createRoomSelectionCard(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBookingFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(430, 0));
        card.setBackground(UIConstants.COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel formTitle = new JLabel("New Reservation Details");
        formTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        formTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        card.add(formTitle, BorderLayout.NORTH);

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);

        scrollContent.add(createGuestSelectionSection());
        scrollContent.add(Box.createVerticalStrut(12));
        scrollContent.add(createBookingParametersSection());

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        card.add(scrollPane, BorderLayout.CENTER);

        card.add(createActionButtonsPanel(), BorderLayout.SOUTH);

        return card;
    }

    private JPanel createGuestSelectionSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);

        JLabel sectionTitle = new JLabel("1. Registered Guest Selection");
        sectionTitle.setFont(UIConstants.FONT_BODY_BOLD);
        sectionTitle.setForeground(UIConstants.COLOR_PRIMARY);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(6));

        JPanel dropdownPanel = new JPanel(new BorderLayout(6, 0));
        dropdownPanel.setOpaque(false);
        cmbRegisteredGuests = new JComboBox<>();
        cmbRegisteredGuests.setFont(UIConstants.FONT_BODY);
        cmbRegisteredGuests.addActionListener(e -> handleGuestDropdownSelection());

        JButton btnRefreshGuests = createStyledButton("↻", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);
        btnRefreshGuests.setToolTipText("Refresh registered guests list");
        btnRefreshGuests.addActionListener(e -> {
            populateGuestDropdown();
            updateStatusFeedback("Guest directory refreshed.", false);
        });

        dropdownPanel.add(cmbRegisteredGuests, BorderLayout.CENTER);
        dropdownPanel.add(btnRefreshGuests, BorderLayout.EAST);
        section.add(dropdownPanel);
        section.add(Box.createVerticalStrut(6));

        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);

        txtGuestSearch = new JTextField();
        txtGuestSearch.setFont(UIConstants.FONT_BODY);
        txtGuestSearch.setToolTipText("Enter Guest ID, NIC/Passport, or Name");

        cmbGuestCriteria = new JComboBox<>(new String[]{"Guest ID", "NIC / Passport", "Guest Name"});
        cmbGuestCriteria.setFont(UIConstants.FONT_MUTED);

        JButton btnSearchGuest = createStyledButton("Find", UIConstants.COLOR_SIDEBAR_ACTIVE, Color.WHITE);
        btnSearchGuest.addActionListener(e -> handleGuestSearch());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        searchPanel.add(txtGuestSearch, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.35;
        searchPanel.add(cmbGuestCriteria, gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.15;
        searchPanel.add(btnSearchGuest, gbc);

        section.add(searchPanel);
        section.add(Box.createVerticalStrut(6));

        JPanel summaryBox = new JPanel(new GridBagLayout());
        summaryBox.setBackground(UIConstants.COLOR_BACKGROUND);
        summaryBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        GridBagConstraints sgbc = new GridBagConstraints();
        sgbc.fill = GridBagConstraints.HORIZONTAL;
        sgbc.insets = new Insets(2, 2, 2, 2);

        sgbc.gridx = 0; sgbc.gridy = 0; sgbc.weightx = 0.35;
        summaryBox.add(createMutedLabel("Guest ID:"), sgbc);
        lblGuestIdVal = createValueLabel("None Selected");
        sgbc.gridx = 1; sgbc.gridy = 0; sgbc.weightx = 0.65;
        summaryBox.add(lblGuestIdVal, sgbc);

        sgbc.gridx = 0; sgbc.gridy = 1; sgbc.weightx = 0.35;
        summaryBox.add(createMutedLabel("Full Name:"), sgbc);
        lblGuestNameVal = createValueLabel("-");
        sgbc.gridx = 1; sgbc.gridy = 1; sgbc.weightx = 0.65;
        summaryBox.add(lblGuestNameVal, sgbc);

        sgbc.gridx = 0; sgbc.gridy = 2; sgbc.weightx = 0.35;
        summaryBox.add(createMutedLabel("NIC / Passport:"), sgbc);
        lblGuestNicVal = createValueLabel("-");
        sgbc.gridx = 1; sgbc.gridy = 2; sgbc.weightx = 0.65;
        summaryBox.add(lblGuestNicVal, sgbc);

        sgbc.gridx = 0; sgbc.gridy = 3; sgbc.weightx = 0.35;
        summaryBox.add(createMutedLabel("Contact No:"), sgbc);
        lblGuestContactVal = createValueLabel("-");
        sgbc.gridx = 1; sgbc.gridy = 3; sgbc.weightx = 0.65;
        summaryBox.add(lblGuestContactVal, sgbc);

        sgbc.gridx = 0; sgbc.gridy = 4; sgbc.weightx = 0.35;
        summaryBox.add(createMutedLabel("Email:"), sgbc);
        lblGuestEmailVal = createValueLabel("-");
        sgbc.gridx = 1; sgbc.gridy = 4; sgbc.weightx = 0.65;
        summaryBox.add(lblGuestEmailVal, sgbc);

        section.add(summaryBox);
        return section;
    }

    private JPanel createBookingParametersSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);

        JLabel sectionTitle = new JLabel("3. Booking Parameters & Deposit");
        sectionTitle.setFont(UIConstants.FONT_BODY_BOLD);
        sectionTitle.setForeground(UIConstants.COLOR_PRIMARY);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(6));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 2, 3, 2);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.42;
        fieldsPanel.add(createFieldLabel("Number of Guests:"), gbc);
        spinGuests = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        spinGuests.setFont(UIConstants.FONT_BODY);
        spinGuests.addChangeListener(e -> updateBookingSummary());
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.58;
        fieldsPanel.add(spinGuests, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.42;
        fieldsPanel.add(createMutedLabel("Stay Duration:"), gbc);
        lblNightsVal = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.58;
        fieldsPanel.add(lblNightsVal, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.42;
        fieldsPanel.add(createMutedLabel("Booking Advance:"), gbc);
        lblAdvanceVal = createValueLabel("LKR 10,000.00");
        lblAdvanceVal.setForeground(new Color(21, 128, 61));
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.58;
        fieldsPanel.add(lblAdvanceVal, gbc);

        section.add(fieldsPanel);
        section.add(Box.createVerticalStrut(4));

        JLabel lblAdvanceNote = new JLabel("<html><i style='color: #64748b; font-size: 11px;'>Mandatory upfront deposit (offline coursework simulation).</i></html>");
        section.add(lblAdvanceNote);
        section.add(Box.createVerticalStrut(8));

        JPanel summaryCard = new JPanel(new GridBagLayout());
        summaryCard.setBackground(UIConstants.COLOR_BACKGROUND);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        GridBagConstraints cgbc = new GridBagConstraints();
        cgbc.fill = GridBagConstraints.HORIZONTAL;
        cgbc.insets = new Insets(2, 2, 2, 2);

        JLabel summaryTitle = new JLabel("Booking Overview");
        summaryTitle.setFont(UIConstants.FONT_MUTED);
        summaryTitle.setForeground(UIConstants.COLOR_PRIMARY);
        cgbc.gridx = 0; cgbc.gridy = 0; cgbc.gridwidth = 2;
        summaryCard.add(summaryTitle, cgbc);

        cgbc.gridwidth = 1;
        cgbc.gridx = 0; cgbc.gridy = 1; cgbc.weightx = 0.35;
        summaryCard.add(createMutedLabel("Guest:"), cgbc);
        lblSummaryGuest = createValueLabel("None");
        cgbc.gridx = 1; cgbc.gridy = 1; cgbc.weightx = 0.65;
        summaryCard.add(lblSummaryGuest, cgbc);

        cgbc.gridx = 0; cgbc.gridy = 2; cgbc.weightx = 0.35;
        summaryCard.add(createMutedLabel("Room:"), cgbc);
        lblSummaryRoom = createValueLabel("None Selected");
        cgbc.gridx = 1; cgbc.gridy = 2; cgbc.weightx = 0.65;
        summaryCard.add(lblSummaryRoom, cgbc);

        cgbc.gridx = 0; cgbc.gridy = 3; cgbc.weightx = 0.35;
        summaryCard.add(createMutedLabel("Stay Dates:"), cgbc);
        lblSummaryDates = createValueLabel("-");
        cgbc.gridx = 1; cgbc.gridy = 3; cgbc.weightx = 0.65;
        summaryCard.add(lblSummaryDates, cgbc);

        cgbc.gridx = 0; cgbc.gridy = 4; cgbc.weightx = 0.35;
        summaryCard.add(createMutedLabel("Advance:"), cgbc);
        lblSummaryAdvance = createValueLabel("LKR 10,000.00");
        cgbc.gridx = 1; cgbc.gridy = 4; cgbc.weightx = 0.65;
        summaryCard.add(lblSummaryAdvance, cgbc);

        section.add(summaryCard);
        return section;
    }

    private JPanel createActionButtonsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 8, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JButton btnConfirm = createStyledButton("Confirm Reservation", UIConstants.COLOR_ACCENT_GOLD, Color.WHITE);
        btnConfirm.setPreferredSize(new Dimension(0, 38));
        JButton btnClear = createStyledButton("Clear Form", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);

        btnConfirm.addActionListener(e -> handleConfirmReservation());
        btnClear.addActionListener(e -> handleClearForm());

        panel.add(btnConfirm);
        panel.add(btnClear);

        return panel;
    }

    private JPanel createRoomSelectionCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(UIConstants.COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JPanel topSearchPanel = new JPanel();
        topSearchPanel.setLayout(new BoxLayout(topSearchPanel, BoxLayout.Y_AXIS));
        topSearchPanel.setOpaque(false);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel tableTitle = new JLabel("2. Room Selection & Availability Query");
        tableTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        tableTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        lblTableSummary = new JLabel("Search required");
        lblTableSummary.setFont(UIConstants.FONT_MUTED);
        lblTableSummary.setForeground(UIConstants.COLOR_TEXT_MUTED);

        titleBar.add(tableTitle, BorderLayout.WEST);
        titleBar.add(lblTableSummary, BorderLayout.EAST);
        topSearchPanel.add(titleBar);
        topSearchPanel.add(Box.createVerticalStrut(8));

        JPanel controlsRow = new JPanel(new GridBagLayout());
        controlsRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);

        LocalDate defaultCheckIn = LocalDate.now(reservationService.getClock()).plusDays(2);
        LocalDate defaultCheckOut = LocalDate.now(reservationService.getClock()).plusDays(5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.28;
        controlsRow.add(createFieldLabel("Room Type:"), gbc);
        cmbRoomType = new JComboBox<>(RoomType.values());
        cmbRoomType.setFont(UIConstants.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.28;
        controlsRow.add(cmbRoomType, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.24;
        controlsRow.add(createFieldLabel("Check-in Date:"), gbc);
        txtCheckInDate = new JTextField(defaultCheckIn.format(STRICT_DATE_FORMATTER));
        txtCheckInDate.setFont(UIConstants.FONT_BODY);
        txtCheckInDate.setToolTipText("Format: yyyy-MM-dd (min 2 days ahead)");
        txtCheckInDate.addActionListener(e -> updateNightsDisplay());
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.24;
        controlsRow.add(txtCheckInDate, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.24;
        controlsRow.add(createFieldLabel("Check-out Date:"), gbc);
        txtCheckOutDate = new JTextField(defaultCheckOut.format(STRICT_DATE_FORMATTER));
        txtCheckOutDate.setFont(UIConstants.FONT_BODY);
        txtCheckOutDate.setToolTipText("Format: yyyy-MM-dd");
        txtCheckOutDate.addActionListener(e -> updateNightsDisplay());
        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.24;
        controlsRow.add(txtCheckOutDate, gbc);

        JButton btnSearchRooms = createStyledButton("Search Available Rooms", UIConstants.COLOR_PRIMARY, Color.WHITE);
        btnSearchRooms.addActionListener(e -> handleSearchAvailableRooms());
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.24;
        controlsRow.add(btnSearchRooms, gbc);

        topSearchPanel.add(controlsRow);
        topSearchPanel.add(Box.createVerticalStrut(4));

        JLabel lblNotice = new JLabel("<html><i style='color: #475569; font-size: 11px;'>* Policy: Advance reservations must be booked at least 2 calendar days ahead. Same-day checkout and check-in is allowed.</i></html>");
        topSearchPanel.add(lblNotice);
        topSearchPanel.add(Box.createVerticalStrut(6));

        card.add(topSearchPanel, BorderLayout.NORTH);

        String[] columnNames = {
                "Room ID", "Room Type", "Bed Type", "Capacity",
                "Daily Rate (LKR)", "Breakfast", "Tax Rate", "Status"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomTable = new JTable(tableModel);
        UIConstants.styleTable(roomTable);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        roomTable.getColumnModel().getColumn(0).setCellRenderer(UIConstants.createCenterAlignRenderer());
        roomTable.getColumnModel().getColumn(1).setCellRenderer(UIConstants.createLeftAlignRenderer());
        roomTable.getColumnModel().getColumn(2).setCellRenderer(UIConstants.createLeftAlignRenderer());
        roomTable.getColumnModel().getColumn(3).setCellRenderer(UIConstants.createCenterAlignRenderer());
        roomTable.getColumnModel().getColumn(4).setCellRenderer(UIConstants.createRightAlignRenderer());
        roomTable.getColumnModel().getColumn(5).setCellRenderer(UIConstants.createStatusBadgeRenderer());
        roomTable.getColumnModel().getColumn(6).setCellRenderer(UIConstants.createCenterAlignRenderer());
        roomTable.getColumnModel().getColumn(7).setCellRenderer(UIConstants.createStatusBadgeRenderer());

        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleRoomTableSelection();
            }
        });

        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        card.add(createSelectedRoomDetailsPanel(), BorderLayout.SOUTH);
        updateNightsDisplay();

        return card;
    }

    private JPanel createSelectedRoomDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIConstants.COLOR_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 4, 2, 4);

        JLabel title = new JLabel("Selected Room:");
        title.setFont(UIConstants.FONT_BODY_BOLD);
        title.setForeground(UIConstants.COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        panel.add(title, gbc);

        lblSelectedRoomId = createValueLabel("None (Select from table above)");
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(lblSelectedRoomId, gbc);

        lblSelectedRoomType = createMutedLabel("");
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.25;
        panel.add(lblSelectedRoomType, gbc);

        lblSelectedRoomRate = createValueLabel("");
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.25;
        panel.add(lblSelectedRoomRate, gbc);

        return panel;
    }

    private JPanel createSearchAndManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 4, 4, 4));

        panel.add(createSearchCriteriaCard(), BorderLayout.NORTH);

        JPanel centerSplit = new JPanel(new BorderLayout(0, 10));
        centerSplit.setOpaque(false);

        centerSplit.add(createReservationTableCard(), BorderLayout.CENTER);
        centerSplit.add(createReservationDetailCard(), BorderLayout.SOUTH);

        panel.add(centerSplit, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSearchCriteriaCard() {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setBackground(UIConstants.COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = new JLabel("Search & Filter Reservations");
        title.setFont(UIConstants.FONT_SECTION_TITLE);
        title.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.05;
        controls.add(createFieldLabel("Search By:"), gbc);

        cmbSearchBy = new JComboBox<>(new String[]{"Booking ID", "Guest Name", "Check-in Date"});
        cmbSearchBy.setFont(UIConstants.FONT_BODY);
        cmbSearchBy.addActionListener(e -> updateSearchPlaceholder());
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.22;
        controls.add(cmbSearchBy, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.05;
        controls.add(createFieldLabel("Search Value:"), gbc);

        txtSearchValue = new JTextField();
        txtSearchValue.setFont(UIConstants.FONT_BODY);
        txtSearchValue.setToolTipText("Enter exact Booking ID, partial Guest Name, or yyyy-MM-dd date");
        txtSearchValue.addActionListener(e -> handleSearchReservations());
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.35;
        controls.add(txtSearchValue, gbc);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnGroup.setOpaque(false);

        btnSearch = createStyledButton("Search", UIConstants.COLOR_PRIMARY, Color.WHITE);
        btnClearSearch = createStyledButton("Clear Search", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);
        btnShowAll = createStyledButton("Show All", UIConstants.COLOR_SIDEBAR_ACTIVE, Color.WHITE);

        btnSearch.addActionListener(e -> handleSearchReservations());
        btnClearSearch.addActionListener(e -> handleClearSearch());
        btnShowAll.addActionListener(e -> handleShowAllReservations());

        btnGroup.add(btnSearch);
        btnGroup.add(btnClearSearch);
        btnGroup.add(btnShowAll);

        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 0.33;
        controls.add(btnGroup, gbc);

        card.add(controls, BorderLayout.CENTER);
        return card;
    }

    private void updateSearchPlaceholder() {
        String criteria = (String) cmbSearchBy.getSelectedItem();
        if ("Booking ID".equals(criteria)) {
            txtSearchValue.setToolTipText("Enter exact Booking ID (e.g. BKG001)");
        } else if ("Guest Name".equals(criteria)) {
            txtSearchValue.setToolTipText("Enter full or partial guest name (case-insensitive)");
        } else {
            txtSearchValue.setToolTipText("Enter Check-in Date in yyyy-MM-dd format (e.g. 2026-11-15)");
        }
    }

    private JPanel createReservationTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UIConstants.COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JLabel tableTitle = new JLabel("Reservation Records Directory");
        tableTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        tableTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        lblReservationTableSummary = new JLabel("Total: 0 reservations");
        lblReservationTableSummary.setFont(UIConstants.FONT_MUTED);
        lblReservationTableSummary.setForeground(UIConstants.COLOR_TEXT_MUTED);

        titleBar.add(tableTitle, BorderLayout.WEST);
        titleBar.add(lblReservationTableSummary, BorderLayout.EAST);
        card.add(titleBar, BorderLayout.NORTH);

        String[] columns = {
                "Booking ID",
                "Guest Name",
                "Room ID",
                "Room Type",
                "Check-in",
                "Check-out",
                "Nights",
                "Guests",
                "Advance (LKR)",
                "Status"
        };

        reservationTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reservationTable = new JTable(reservationTableModel);
        UIConstants.styleTable(reservationTable);
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        reservationTable.getColumnModel().getColumn(0).setCellRenderer(UIConstants.createCenterAlignRenderer());
        reservationTable.getColumnModel().getColumn(1).setCellRenderer(UIConstants.createLeftAlignRenderer());
        reservationTable.getColumnModel().getColumn(2).setCellRenderer(UIConstants.createCenterAlignRenderer());
        reservationTable.getColumnModel().getColumn(3).setCellRenderer(UIConstants.createLeftAlignRenderer());
        reservationTable.getColumnModel().getColumn(4).setCellRenderer(UIConstants.createCenterAlignRenderer());
        reservationTable.getColumnModel().getColumn(5).setCellRenderer(UIConstants.createCenterAlignRenderer());
        reservationTable.getColumnModel().getColumn(6).setCellRenderer(UIConstants.createCenterAlignRenderer());
        reservationTable.getColumnModel().getColumn(7).setCellRenderer(UIConstants.createCenterAlignRenderer());
        reservationTable.getColumnModel().getColumn(8).setCellRenderer(UIConstants.createRightAlignRenderer());
        reservationTable.getColumnModel().getColumn(9).setCellRenderer(UIConstants.createStatusBadgeRenderer());

        reservationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleReservationTableSelection();
            }
        });

        JScrollPane scroll = new JScrollPane(reservationTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createReservationDetailCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UIConstants.COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = new JLabel("Reservation Detail View");
        title.setFont(UIConstants.FONT_SECTION_TITLE);
        title.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(UIConstants.COLOR_BACKGROUND);
        grid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        grid.add(createFieldLabel("Booking ID:"), gbc);
        lblDetailBookingId = createValueLabel("None (Select a reservation)");
        lblDetailBookingId.setForeground(UIConstants.COLOR_PRIMARY);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.35;
        grid.add(lblDetailBookingId, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.15;
        grid.add(createFieldLabel("Status:"), gbc);
        lblDetailStatus = createValueLabel("-");
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.35;
        grid.add(lblDetailStatus, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.15;
        grid.add(createFieldLabel("Guest Details:"), gbc);
        lblDetailGuest = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weightx = 0.85;
        grid.add(lblDetailGuest, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.15;
        grid.add(createFieldLabel("Room Details:"), gbc);
        lblDetailRoom = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3; gbc.weightx = 0.85;
        grid.add(lblDetailRoom, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.15;
        grid.add(createFieldLabel("Check-in Date:"), gbc);
        lblDetailCheckIn = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.35;
        grid.add(lblDetailCheckIn, gbc);

        gbc.gridx = 2; gbc.gridy = 3; gbc.weightx = 0.15;
        grid.add(createFieldLabel("Check-out Date:"), gbc);
        lblDetailCheckOut = createValueLabel("-");
        gbc.gridx = 3; gbc.gridy = 3; gbc.weightx = 0.35;
        grid.add(lblDetailCheckOut, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.15;
        grid.add(createFieldLabel("Stay Duration:"), gbc);
        lblDetailNights = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.35;
        grid.add(lblDetailNights, gbc);

        gbc.gridx = 2; gbc.gridy = 4; gbc.weightx = 0.15;
        grid.add(createFieldLabel("Number of Guests:"), gbc);
        lblDetailGuests = createValueLabel("-");
        gbc.gridx = 3; gbc.gridy = 4; gbc.weightx = 0.35;
        grid.add(lblDetailGuests, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.15;
        grid.add(createFieldLabel("Booking Advance:"), gbc);
        lblDetailAdvance = createValueLabel("-");
        lblDetailAdvance.setForeground(new Color(21, 128, 61));
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 3; gbc.weightx = 0.85;
        grid.add(lblDetailAdvance, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.15;
        grid.add(createFieldLabel("Cancellation Status:"), gbc);
        lblDetailCancellationEligibility = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 3; gbc.weightx = 0.85;
        grid.add(lblDetailCancellationEligibility, gbc);
        gbc.gridwidth = 1;

        card.add(grid, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        actionPanel.setOpaque(false);

        btnCancelReservation = createStyledButton("Cancel Reservation", new Color(220, 38, 38), Color.WHITE);
        btnCancelReservation.setEnabled(false);
        btnCancelReservation.setToolTipText("Cancel selected reservation (Allowed up to 24 hours prior to 14:00 check-in)");
        btnCancelReservation.addActionListener(e -> handleCancelReservationAction());

        actionPanel.add(btnCancelReservation);
        card.add(actionPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footer.setOpaque(false);

        lblStatusFeedback = new JLabel("Ready. Select a guest, search available rooms, or view existing bookings.");
        lblStatusFeedback.setFont(UIConstants.FONT_MUTED);
        lblStatusFeedback.setForeground(UIConstants.COLOR_TEXT_MUTED);

        footer.add(lblStatusFeedback);
        return footer;
    }

    private void handleSearchReservations() {
        String criteria = (String) cmbSearchBy.getSelectedItem();
        String query = txtSearchValue.getText().trim();

        if (query.isEmpty()) {
            showWarning("Please enter a search value.");
            txtSearchValue.requestFocus();
            return;
        }

        searchReservations(criteria, query);
    }

    public void searchReservations(String criteria, String query) {
        List<Reservation> results = new ArrayList<>();

        if ("Booking ID".equalsIgnoreCase(criteria)) {
            Reservation r = reservationService.searchByBookingId(query);
            if (r != null) {
                results.add(r);
            }
        } else if ("Guest Name".equalsIgnoreCase(criteria)) {
            results = reservationService.searchByGuestName(query);
        } else if ("Check-in Date".equalsIgnoreCase(criteria)) {
            if (!DATE_PATTERN.matcher(query.trim()).matches()) {
                showWarning("Please enter check-in date in yyyy-MM-dd format (e.g. 2026-11-15).");
                txtSearchValue.requestFocus();
                return;
            }
            LocalDate date;
            try {
                date = LocalDate.parse(query.trim(), STRICT_DATE_FORMATTER);
            } catch (DateTimeParseException ex) {
                showWarning("Invalid calendar date: '" + query + "'.");
                txtSearchValue.requestFocus();
                return;
            }
            results = reservationService.getReservationsByCheckInDate(date);
        }

        currentReservationSearchResults = results;
        renderReservationTableRows(results);
        clearReservationDetailView();

        if (results.isEmpty()) {
            lblReservationTableSummary.setText("Found: 0 reservations");
            updateStatusFeedback("No reservations found matching " + criteria + ": '" + query + "'.", false);
            if (isShowing()) {
                JOptionPane.showMessageDialog(this,
                        "No reservations found matching " + criteria + ": '" + query + "'.",
                        "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            lblReservationTableSummary.setText("Found: " + results.size() + " reservation" + (results.size() == 1 ? "" : "s"));
            updateStatusFeedback("Found " + results.size() + " matching reservation(s). Select a row to inspect details.", false);
            reservationTable.setRowSelectionInterval(0, 0);
        }
    }

    private void handleClearSearch() {
        clearSearch();
    }

    public void clearSearch() {
        txtSearchValue.setText("");
        if (cmbSearchBy.getItemCount() > 0) {
            cmbSearchBy.setSelectedIndex(0);
        }
        clearReservationDetailView();
        showAllReservations();
        updateStatusFeedback("Search cleared. Displaying all reservations.", false);
    }

    private void handleShowAllReservations() {
        showAllReservations();
    }

    public void showAllReservations() {
        txtSearchValue.setText("");
        List<Reservation> all = reservationService.getAllReservations();
        currentReservationSearchResults = all;
        renderReservationTableRows(all);
        lblReservationTableSummary.setText("Total: " + all.size() + " reservation" + (all.size() == 1 ? "" : "s"));
        clearReservationDetailView();
        updateStatusFeedback("Displaying all " + all.size() + " reservation record(s).", false);
    }

    public void refreshReservationHistoryTable() {
        List<Reservation> all = reservationService.getAllReservations();
        currentReservationSearchResults = all;
        renderReservationTableRows(all);
        lblReservationTableSummary.setText("Total: " + all.size() + " reservation" + (all.size() == 1 ? "" : "s"));
    }

    private void handleReservationTableSelection() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < reservationTableModel.getRowCount()) {
            String bookingId = (String) reservationTableModel.getValueAt(selectedRow, 0);
            Reservation found = reservationService.searchByBookingId(bookingId);

            if (found != null) {
                lblDetailBookingId.setText(found.getBookingId());
                lblDetailStatus.setText(found.getStatus().getDisplayName());

                Guest g = found.getGuest();
                if (g != null) {
                    lblDetailGuest.setText(g.getFullName() + " (ID: " + g.getGuestId() + ", NIC: " + g.getNicOrPassport() + ", Phone: " + g.getContactNumber() + ", Email: " + g.getEmail() + ")");
                } else {
                    lblDetailGuest.setText("-");
                }

                Room r = found.getRoom();
                if (r != null) {
                    lblDetailRoom.setText("Room " + r.getRoomId() + " [" + r.getRoomType().getDisplayName() + ", " + r.getBedType().getDisplayName() + ", Daily Rate: LKR " + String.format("%,.2f", r.getDailyRate()) + "]");
                } else {
                    lblDetailRoom.setText("-");
                }

                lblDetailCheckIn.setText(found.getCheckInDate() != null ? found.getCheckInDate().toString() : "-");
                lblDetailCheckOut.setText(found.getCheckOutDate() != null ? found.getCheckOutDate().toString() : "-");
                lblDetailNights.setText(found.calculateNumberOfNights() + " night" + (found.calculateNumberOfNights() == 1 ? "" : "s"));
                lblDetailGuests.setText(found.getNumberOfGuests() + " guest" + (found.getNumberOfGuests() == 1 ? "" : "s"));
                lblDetailAdvance.setText("LKR " + String.format("%,.2f", found.getBookingAdvance()) + " (Simulated Advance Payment)");

                updateCancellationState(found);

                updateStatusFeedback("Selected: Booking " + found.getBookingId() + " (" + (g != null ? g.getFullName() : "") + ").", false);
            }
        } else {
            clearReservationDetailView();
        }
    }

    private void updateCancellationState(Reservation reservation) {
        if (lblDetailCancellationEligibility == null) {
            return;
        }
        if (reservation == null) {
            lblDetailCancellationEligibility.setText("-");
            lblDetailCancellationEligibility.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
            if (btnCancelReservation != null) {
                btnCancelReservation.setEnabled(false);
            }
            return;
        }

        if (reservation.getStatus() == lk.smartstay.enums.ReservationStatus.CANCELLED) {
            lblDetailCancellationEligibility.setText("Cancelled — Refund Due: LKR "
                    + String.format("%,.2f", reservation.getBookingAdvance()) + " (Simulated Advance Payment Refund)");
            lblDetailCancellationEligibility.setForeground(new Color(217, 119, 6));
            if (btnCancelReservation != null) {
                btnCancelReservation.setEnabled(false);
            }
        } else if (reservation.getStatus() == lk.smartstay.enums.ReservationStatus.COMPLETED) {
            lblDetailCancellationEligibility.setText("Completed — Stay completed (Cannot be cancelled)");
            lblDetailCancellationEligibility.setForeground(UIConstants.COLOR_TEXT_MUTED);
            if (btnCancelReservation != null) {
                btnCancelReservation.setEnabled(false);
            }
        } else if (reservationService.canCancel(reservation)) {
            lblDetailCancellationEligibility.setText("Eligible for Cancellation (Refund Due: LKR "
                    + String.format("%,.2f", reservation.getBookingAdvance()) + ")");
            lblDetailCancellationEligibility.setForeground(new Color(22, 163, 74));
            if (btnCancelReservation != null) {
                btnCancelReservation.setEnabled(true);
            }
        } else {
            String reason = reservationService.getCancellationIneligibilityReason(reservation);
            lblDetailCancellationEligibility.setText("Ineligible: " + (reason != null ? reason : "Within 24 hours of check-in"));
            lblDetailCancellationEligibility.setForeground(new Color(220, 38, 38));
            if (btnCancelReservation != null) {
                btnCancelReservation.setEnabled(false);
            }
        }
    }

    private void handleCancelReservationAction() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= reservationTableModel.getRowCount()) {
            showWarning("Please select a reservation from the table to cancel.");
            return;
        }

        String bookingId = (String) reservationTableModel.getValueAt(selectedRow, 0);
        Reservation reservation = reservationService.searchByBookingId(bookingId);
        if (reservation == null) {
            showWarning("Reservation record not found for Booking ID: " + bookingId);
            return;
        }

        if (!reservationService.canCancel(reservation)) {
            String reason = reservationService.getCancellationIneligibilityReason(reservation);
            showWarning("Cancellation Ineligible:\n" + (reason != null ? reason : "Reservation cannot be cancelled within 24 hours of check-in."));
            return;
        }

        String confirmMessage = String.format(
                "Are you sure you want to cancel booking %s?\n\n"
                        + "Guest: %s\n"
                        + "Room: %s (%s)\n"
                        + "Check-in Date: %s (Standard check-in 14:00)\n"
                        + "Advance Refund Due: LKR %,.2f\n\n"
                        + "Note: This action marks the reservation as CANCELLED and restores room availability.",
                reservation.getBookingId(),
                reservation.getGuest() != null ? reservation.getGuest().getFullName() : "-",
                reservation.getRoom() != null ? reservation.getRoom().getRoomId() : "-",
                reservation.getRoom() != null ? reservation.getRoom().getRoomType().getDisplayName() : "-",
                reservation.getCheckInDate(),
                reservation.getBookingAdvance()
        );

        int confirmResult = JOptionPane.YES_OPTION;
        if (isShowing()) {
            confirmResult = JOptionPane.showConfirmDialog(
                    this,
                    confirmMessage,
                    "Confirm Reservation Cancellation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
        }

        if (confirmResult != JOptionPane.YES_OPTION) {
            updateStatusFeedback("Cancellation dismissed by user.", false);
            return;
        }

        boolean cancelled = reservationService.cancelReservation(reservation.getBookingId());
        if (cancelled) {
            String successMessage = String.format(
                    "<html><body style='font-family: Segoe UI, sans-serif; font-size: 13px;'>"
                            + "<h3 style='color: #b91c1c; margin-top: 0;'>Reservation Cancelled Successfully</h3>"
                            + "<p>Booking <b>%s</b> has been marked as <b>CANCELLED</b> in the system.</p>"
                            + "<p style='color: #15803d; font-size: 14px;'><b>Refund Due: LKR %,.2f</b></p>"
                            + "<p style='color: #64748b; font-size: 11px;'><i>Simulated Coursework Refund: Advance refund entitlement recorded.<br>"
                            + "Previously reserved dates are now restored and searchable for new bookings.</i></p>"
                            + "</body></html>",
                    reservation.getBookingId(),
                    reservation.getBookingAdvance()
            );

            if (isShowing()) {
                JOptionPane.showMessageDialog(this, successMessage, "Cancellation Completed", JOptionPane.INFORMATION_MESSAGE);
            }
            updateStatusFeedback("Booking " + reservation.getBookingId() + " cancelled successfully. Refund Due: LKR "
                    + String.format("%,.2f", reservation.getBookingAdvance()) + ".", false);

            refreshReservationHistoryTable();
            for (int i = 0; i < reservationTableModel.getRowCount(); i++) {
                if (bookingId.equals(reservationTableModel.getValueAt(i, 0))) {
                    reservationTable.setRowSelectionInterval(i, i);
                    break;
                }
            }

            if (hasSearchedRooms) {
                handleSearchAvailableRooms();
            }
        } else {
            showWarning("Failed to cancel reservation " + bookingId + ". Please check status and retry.");
        }
    }

    private void clearReservationDetailView() {
        lblDetailBookingId.setText("None (Select a reservation)");
        lblDetailStatus.setText("-");
        lblDetailGuest.setText("-");
        lblDetailRoom.setText("-");
        lblDetailCheckIn.setText("-");
        lblDetailCheckOut.setText("-");
        lblDetailNights.setText("-");
        lblDetailGuests.setText("-");
        lblDetailAdvance.setText("-");
        if (lblDetailCancellationEligibility != null) {
            lblDetailCancellationEligibility.setText("-");
            lblDetailCancellationEligibility.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        }
        if (btnCancelReservation != null) {
            btnCancelReservation.setEnabled(false);
        }
    }

    private void renderReservationTableRows(List<Reservation> reservations) {
        reservationTableModel.setRowCount(0);
        for (Reservation r : reservations) {
            reservationTableModel.addRow(new Object[]{
                    r.getBookingId(),
                    r.getGuest() != null ? r.getGuest().getFullName() : "-",
                    r.getRoom() != null ? r.getRoom().getRoomId() : "-",
                    r.getRoom() != null ? r.getRoom().getRoomType().getDisplayName() : "-",
                    r.getCheckInDate() != null ? r.getCheckInDate().toString() : "-",
                    r.getCheckOutDate() != null ? r.getCheckOutDate().toString() : "-",
                    r.calculateNumberOfNights(),
                    r.getNumberOfGuests(),
                    String.format("%,.2f", r.getBookingAdvance()),
                    r.getStatus() != null ? r.getStatus().getDisplayName() : "-"
            });
        }
    }

    public JTable getReservationHistoryTable() {
        return reservationTable;
    }

    public DefaultTableModel getReservationTableModel() {
        return reservationTableModel;
    }

    public void setSelectedTab(int index) {
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    public JButton getBtnCancelReservation() {
        return btnCancelReservation;
    }

    public JLabel getLblDetailCancellationEligibility() {
        return lblDetailCancellationEligibility;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public JComboBox<String> getCmbRegisteredGuests() {
        return cmbRegisteredGuests;
    }

    public JTextField getTxtGuestSearch() {
        return txtGuestSearch;
    }

    public JComboBox<RoomType> getCmbRoomType() {
        return cmbRoomType;
    }

    public JTextField getTxtCheckInDate() {
        return txtCheckInDate;
    }

    public JTextField getTxtCheckOutDate() {
        return txtCheckOutDate;
    }

    public JTable getRoomTable() {
        return roomTable;
    }

    public DefaultTableModel getRoomTableModel() {
        return tableModel;
    }

    public JSpinner getSpinGuests() {
        return spinGuests;
    }

    public Guest getSelectedGuest() {
        return selectedGuest;
    }

    public Room getSelectedRoom() {
        return selectedRoom;
    }

    public JComboBox<String> getCmbSearchBy() {
        return cmbSearchBy;
    }

    public JTextField getTxtSearchValue() {
        return txtSearchValue;
    }

    public JButton getBtnSearch() {
        return btnSearch;
    }

    public JButton getBtnClearSearch() {
        return btnClearSearch;
    }

    public JButton getBtnShowAll() {
        return btnShowAll;
    }

    public JLabel getLblStatusFeedback() {
        return lblStatusFeedback;
    }

    public void executeSearchAvailableRooms() {
        handleSearchAvailableRooms();
    }

    public void executeConfirmReservation() {
        handleConfirmReservation();
    }

    public void executeClearReservationForm() {
        handleClearForm();
    }

    public boolean cancelSelectedReservationDirectly() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= reservationTableModel.getRowCount()) {
            return false;
        }
        String bookingId = (String) reservationTableModel.getValueAt(selectedRow, 0);
        boolean cancelled = reservationService.cancelReservation(bookingId);
        if (cancelled) {
            refreshReservationHistoryTable();
            for (int i = 0; i < reservationTableModel.getRowCount(); i++) {
                if (bookingId.equals(reservationTableModel.getValueAt(i, 0))) {
                    reservationTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
            if (hasSearchedRooms) {
                handleSearchAvailableRooms();
            }
        }
        return cancelled;
    }

    public void populateGuestDropdown() {
        cmbRegisteredGuests.removeAllItems();
        cmbRegisteredGuests.addItem("-- Select a Registered Guest --");
        List<Guest> allGuests = guestService.getAllGuests();
        for (Guest g : allGuests) {
            cmbRegisteredGuests.addItem(g.getGuestId() + " - " + g.getFullName() + " (" + g.getNicOrPassport() + ")");
        }
    }

    private void handleGuestDropdownSelection() {
        int idx = cmbRegisteredGuests.getSelectedIndex();
        if (idx <= 0) {
            clearSelectedGuest();
            return;
        }

        List<Guest> allGuests = guestService.getAllGuests();
        if (idx - 1 < allGuests.size()) {
            setSelectedGuest(allGuests.get(idx - 1));
        }
    }

    private void handleGuestSearch() {
        String query = txtGuestSearch.getText().trim();
        if (query.isEmpty()) {
            showWarning("Please enter a search query (Guest ID, NIC/Passport, or Name).");
            txtGuestSearch.requestFocus();
            return;
        }

        String criteria = (String) cmbGuestCriteria.getSelectedItem();
        Guest found = null;

        if ("Guest ID".equals(criteria)) {
            found = guestService.findGuestById(query);
        } else if ("NIC / Passport".equals(criteria)) {
            found = guestService.findGuestByNicOrPassport(query);
        } else {
            List<Guest> matches = guestService.searchGuestsByName(query);
            if (!matches.isEmpty()) {
                found = matches.get(0);
            }
        }

        if (found != null) {
            setSelectedGuest(found);
            for (int i = 1; i < cmbRegisteredGuests.getItemCount(); i++) {
                String item = cmbRegisteredGuests.getItemAt(i);
                if (item.startsWith(found.getGuestId())) {
                    cmbRegisteredGuests.setSelectedIndex(i);
                    break;
                }
            }
            updateStatusFeedback("Guest profile loaded: " + found.getFullName() + " (" + found.getGuestId() + ").", false);
        } else {
            showWarning("No registered guest found matching '" + query + "'. Please check the registration database.");
            clearSelectedGuest();
        }
    }

    private void setSelectedGuest(Guest guest) {
        this.selectedGuest = guest;
        lblGuestIdVal.setText(guest.getGuestId());
        lblGuestNameVal.setText(guest.getFullName());
        lblGuestNicVal.setText(guest.getNicOrPassport());
        lblGuestContactVal.setText(guest.getContactNumber());
        lblGuestEmailVal.setText(guest.getEmail());

        updateBookingSummary();
        updateStatusFeedback("Selected Guest: " + guest.getFullName() + " (" + guest.getGuestId() + ").", false);
    }

    private void clearSelectedGuest() {
        this.selectedGuest = null;
        lblGuestIdVal.setText("None Selected");
        lblGuestNameVal.setText("-");
        lblGuestNicVal.setText("-");
        lblGuestContactVal.setText("-");
        lblGuestEmailVal.setText("-");
        updateBookingSummary();
    }

    private void handleSearchAvailableRooms() {
        RoomType type = (RoomType) cmbRoomType.getSelectedItem();
        if (type == null) {
            showWarning("Please select a room type.");
            return;
        }

        String checkInStr = txtCheckInDate.getText().trim();
        String checkOutStr = txtCheckOutDate.getText().trim();

        if (checkInStr.isEmpty() || checkOutStr.isEmpty()) {
            showWarning("Please enter both check-in and check-out dates (format: yyyy-MM-dd).");
            return;
        }

        if (!DATE_PATTERN.matcher(checkInStr).matches()) {
            showWarning("Check-in date must be formatted as yyyy-MM-dd (e.g. 2026-11-20).");
            txtCheckInDate.requestFocus();
            return;
        }
        if (!DATE_PATTERN.matcher(checkOutStr).matches()) {
            showWarning("Check-out date must be formatted as yyyy-MM-dd (e.g. 2026-11-25).");
            txtCheckOutDate.requestFocus();
            return;
        }

        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = LocalDate.parse(checkInStr, STRICT_DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            showWarning("Invalid check-in calendar date: '" + checkInStr + "'.");
            txtCheckInDate.requestFocus();
            return;
        }
        try {
            checkOut = LocalDate.parse(checkOutStr, STRICT_DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            showWarning("Invalid check-out calendar date: '" + checkOutStr + "'.");
            txtCheckOutDate.requestFocus();
            return;
        }

        LocalDate minAllowed = LocalDate.now(reservationService.getClock()).plusDays(2);
        if (checkIn.isBefore(minAllowed)) {
            showWarning("Hotel policy requires at least 2 calendar days advance booking.\nEarliest permissible check-in is " + minAllowed + ".");
            txtCheckInDate.requestFocus();
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            showWarning("Check-out date (" + checkOutStr + ") must logically be after check-in date (" + checkInStr + ").");
            txtCheckOutDate.requestFocus();
            return;
        }

        try {
            List<Room> available = reservationService.searchAvailableRooms(type, checkIn, checkOut);
            currentAvailableRooms = available;
            hasSearchedRooms = true;
            renderTableRows(available);
            clearSelectedRoom();

            lblTableSummary.setText("Found: " + available.size() + " available room" + (available.size() == 1 ? "" : "s"));
            updateNightsDisplay();

            if (available.isEmpty()) {
                updateStatusFeedback("No " + type.getDisplayName() + " rooms available between " + checkIn + " and " + checkOut + ".", false);
                if (isShowing()) {
                    JOptionPane.showMessageDialog(this,
                            "No available " + type.getDisplayName() + " rooms found for the requested dates (" + checkIn + " to " + checkOut + ").\nPlease try another room category or different dates.",
                            "No Rooms Available", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                updateStatusFeedback("Found " + available.size() + " available " + type.getDisplayName() + " room(s). Select a room to proceed.", false);
            }
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        }
    }

    private void handleRoomTableSelection() {
        int row = roomTable.getSelectedRow();
        if (row >= 0 && row < tableModel.getRowCount()) {
            String roomId = (String) tableModel.getValueAt(row, 0);
            Room found = null;
            for (Room r : currentAvailableRooms) {
                if (r.getRoomId().equalsIgnoreCase(roomId)) {
                    found = r;
                    break;
                }
            }

            if (found != null) {
                selectedRoom = found;
                lblSelectedRoomId.setText("Room " + found.getRoomId());
                lblSelectedRoomType.setText(found.getRoomType().getDisplayName() + " (" + found.getBedType().getDisplayName() + ")");
                lblSelectedRoomRate.setText("LKR " + String.format("%,.2f", found.getDailyRate()) + " / night");

                int recommendedCap = Math.max(1, found.getCapacity());
                if ((Integer) spinGuests.getValue() > recommendedCap * 2) {
                    spinGuests.setValue(recommendedCap);
                }

                updateBookingSummary();
                updateStatusFeedback("Selected Room " + found.getRoomId() + " (" + found.getRoomType().getDisplayName() + "). Ready to confirm.", false);
            }
        } else {
            clearSelectedRoom();
        }
    }

    private void clearSelectedRoom() {
        selectedRoom = null;
        lblSelectedRoomId.setText("None (Select from table above)");
        lblSelectedRoomType.setText("");
        lblSelectedRoomRate.setText("");
        updateBookingSummary();
    }

    private void updateNightsDisplay() {
        String inStr = txtCheckInDate.getText().trim();
        String outStr = txtCheckOutDate.getText().trim();
        if (DATE_PATTERN.matcher(inStr).matches() && DATE_PATTERN.matcher(outStr).matches()) {
            try {
                LocalDate inDate = LocalDate.parse(inStr, STRICT_DATE_FORMATTER);
                LocalDate outDate = LocalDate.parse(outStr, STRICT_DATE_FORMATTER);
                if (outDate.isAfter(inDate)) {
                    long nights = ChronoUnit.DAYS.between(inDate, outDate);
                    lblNightsVal.setText(nights + " night" + (nights == 1 ? "" : "s") + " (" + inDate + " to " + outDate + ")");
                    updateBookingSummary();
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        lblNightsVal.setText("-");
        updateBookingSummary();
    }

    private void updateBookingSummary() {
        if (selectedGuest != null) {
            lblSummaryGuest.setText(selectedGuest.getFullName() + " (" + selectedGuest.getGuestId() + ")");
        } else {
            lblSummaryGuest.setText("None Selected");
        }

        if (selectedRoom != null) {
            lblSummaryRoom.setText("Room " + selectedRoom.getRoomId() + " [" + selectedRoom.getRoomType().getDisplayName() + "]");
        } else {
            lblSummaryRoom.setText("None Selected");
        }

        String inStr = txtCheckInDate.getText().trim();
        String outStr = txtCheckOutDate.getText().trim();
        if (DATE_PATTERN.matcher(inStr).matches() && DATE_PATTERN.matcher(outStr).matches()) {
            try {
                LocalDate inDate = LocalDate.parse(inStr, STRICT_DATE_FORMATTER);
                LocalDate outDate = LocalDate.parse(outStr, STRICT_DATE_FORMATTER);
                if (outDate.isAfter(inDate)) {
                    long nights = ChronoUnit.DAYS.between(inDate, outDate);
                    lblSummaryDates.setText(inDate + " → " + outDate + " (" + nights + "n, " + spinGuests.getValue() + " guests)");
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        lblSummaryDates.setText("-");
    }

    private void handleConfirmReservation() {
        if (selectedGuest == null) {
            showWarning("Please select or find a registered guest first.");
            txtGuestSearch.requestFocus();
            return;
        }

        if (!hasSearchedRooms || selectedRoom == null) {
            showWarning("Please search and select an available room from the directory before confirming.");
            return;
        }

        String inStr = txtCheckInDate.getText().trim();
        String outStr = txtCheckOutDate.getText().trim();
        if (!DATE_PATTERN.matcher(inStr).matches() || !DATE_PATTERN.matcher(outStr).matches()) {
            showWarning("Dates must be valid calendar dates formatted as yyyy-MM-dd.");
            return;
        }

        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = LocalDate.parse(inStr, STRICT_DATE_FORMATTER);
            checkOut = LocalDate.parse(outStr, STRICT_DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            showWarning("Invalid calendar date specified.");
            return;
        }

        int numberOfGuests = (Integer) spinGuests.getValue();
        if (numberOfGuests <= 0) {
            showWarning("Number of guests must be greater than zero.");
            return;
        }

        Reservation confirmed;
        try {
            confirmed = reservationService.createReservation(
                    selectedGuest,
                    selectedRoom,
                    numberOfGuests,
                    checkIn,
                    checkOut
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showWarning("Booking Error: " + ex.getMessage());
            return;
        }

        long nights = confirmed.calculateNumberOfNights();
        String confirmationMessage = String.format(
                "<html>"
                        + "<body style='font-family: Segoe UI, sans-serif; font-size: 13px;'>"
                        + "<h2 style='color: #1e3a8a; margin-top: 0;'>Booking Confirmation</h2>"
                        + "<p>The reservation has been successfully confirmed and stored in the system.</p>"
                        + "<table cellpadding='4' cellspacing='0' style='border-collapse: collapse; width: 100%%;'>"
                        + "<tr style='background-color: #f1f5f9;'><td><b>Booking ID:</b></td><td style='color: #1e3a8a; font-weight: bold;'>%s</td></tr>"
                        + "<tr><td><b>Booking Status:</b></td><td style='color: #15803d; font-weight: bold;'>%s</td></tr>"
                        + "<tr style='background-color: #f1f5f9;'><td><b>Guest Name:</b></td><td>%s (ID: %s)</td></tr>"
                        + "<tr><td><b>Room Details:</b></td><td>Room %s (%s, %s)</td></tr>"
                        + "<tr style='background-color: #f1f5f9;'><td><b>Check-in Date:</b></td><td>%s</td></tr>"
                        + "<tr><td><b>Check-out Date:</b></td><td>%s</td></tr>"
                        + "<tr style='background-color: #f1f5f9;'><td><b>Stay Duration:</b></td><td>%d night%s</td></tr>"
                        + "<tr><td><b>Number of Guests:</b></td><td>%d</td></tr>"
                        + "<tr style='background-color: #f1f5f9;'><td><b>Daily Rate:</b></td><td>LKR %,.2f</td></tr>"
                        + "<tr><td><b>Advance Recorded:</b></td><td style='font-weight: bold; color: #15803d;'>LKR %,.2f</td></tr>"
                        + "</table>"
                        + "<br><i style='color: #64748b; font-size: 11px;'>Advance payment is recorded as an offline coursework simulation in ReservationRepository.</i>"
                        + "</body></html>",
                confirmed.getBookingId(),
                confirmed.getStatus().getDisplayName(),
                confirmed.getGuest().getFullName(),
                confirmed.getGuest().getGuestId(),
                confirmed.getRoom().getRoomId(),
                confirmed.getRoom().getRoomType().getDisplayName(),
                confirmed.getRoom().getBedType().getDisplayName(),
                confirmed.getCheckInDate(),
                confirmed.getCheckOutDate(),
                nights,
                nights == 1 ? "" : "s",
                confirmed.getNumberOfGuests(),
                confirmed.getRoom().getDailyRate(),
                confirmed.getBookingAdvance()
        );

        if (isShowing()) {
            JOptionPane.showMessageDialog(this, confirmationMessage, "Reservation Confirmed", JOptionPane.INFORMATION_MESSAGE);
        }

        updateStatusFeedback("Reservation " + confirmed.getBookingId() + " successfully confirmed for " + confirmed.getGuest().getFullName() + "!", false);

        handleSearchAvailableRooms();
        refreshReservationHistoryTable();
    }

    private void handleClearForm() {
        clearSelectedGuest();
        if (cmbRegisteredGuests.getItemCount() > 0) {
            cmbRegisteredGuests.setSelectedIndex(0);
        }
        txtGuestSearch.setText("");

        LocalDate defaultCheckIn = LocalDate.now(reservationService.getClock()).plusDays(2);
        LocalDate defaultCheckOut = LocalDate.now(reservationService.getClock()).plusDays(5);
        txtCheckInDate.setText(defaultCheckIn.format(STRICT_DATE_FORMATTER));
        txtCheckOutDate.setText(defaultCheckOut.format(STRICT_DATE_FORMATTER));

        if (cmbRoomType.getItemCount() > 0) {
            cmbRoomType.setSelectedIndex(0);
        }
        spinGuests.setValue(1);

        tableModel.setRowCount(0);
        currentAvailableRooms.clear();
        hasSearchedRooms = false;
        clearSelectedRoom();

        lblTableSummary.setText("Search required");
        updateNightsDisplay();
        updateStatusFeedback("Reservation form reset. Ready for new booking.", false);
    }

    private void renderTableRows(List<Room> rooms) {
        tableModel.setRowCount(0);
        for (Room r : rooms) {
            tableModel.addRow(new Object[]{
                    r.getRoomId(),
                    r.getRoomType().getDisplayName(),
                    r.getBedType().getDisplayName(),
                    r.getCapacity(),
                    String.format("%,.2f", r.getDailyRate()),
                    r.hasComplimentaryBreakfast() ? "Yes" : "No",
                    String.format("%.0f%%", r.getTaxRate() * 100),
                    r.getStatus().getDisplayName()
            });
        }
    }

    private void updateStatusFeedback(String message, boolean isError) {
        lblStatusFeedback.setText(message);
        lblStatusFeedback.setForeground(isError ? new Color(185, 28, 28) : UIConstants.COLOR_TEXT_MUTED);
    }

    private void showWarning(String message) {
        updateStatusFeedback(message, true);
        if (isShowing()) {
            JOptionPane.showMessageDialog(this, message, "Validation Notice", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BODY_BOLD);
        label.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        return label;
    }

    private JLabel createMutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_MUTED);
        label.setForeground(UIConstants.COLOR_TEXT_MUTED);
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BODY_BOLD);
        label.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        return label;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(UIConstants.FONT_BODY_BOLD);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        return button;
    }
}
