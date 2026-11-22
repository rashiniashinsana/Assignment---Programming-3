package lk.smartstay.ui.guest;

import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Room;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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

public class RoomSearchPanel extends JPanel {

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final DateTimeFormatter STRICT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final ReservationService reservationService;

    private JComboBox<RoomType> cmbRoomType;
    private JTextField txtCheckInDate;
    private JTextField txtCheckOutDate;

    private JLabel lblDailyRateValue;
    private JLabel lblTaxRateValue;
    private JLabel lblBreakfastValue;
    private JLabel lblExtraGuestValue;

    private JLabel lblSelectedRoomId;
    private JLabel lblSelectedRoomType;
    private JLabel lblSelectedRoomBed;
    private JLabel lblSelectedRoomCapacity;
    private JLabel lblSelectedRoomRate;
    private JLabel lblSelectedRoomNights;

    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JLabel lblTableSummary;
    private JLabel lblStatusFeedback;

    private List<Room> currentResults = new ArrayList<>();
    private LocalDate lastSearchedCheckIn;
    private LocalDate lastSearchedCheckOut;

    public RoomSearchPanel(ReservationService reservationService) {
        if (reservationService == null) {
            throw new IllegalArgumentException("ReservationService cannot be null.");
        }
        this.reservationService = reservationService;
        initComponents();
    }

    public RoomSearchPanel() {
        this(new ReservationService());
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(18, 0));
        mainContent.setOpaque(false);

        mainContent.add(createSearchCriteriaCard(), BorderLayout.WEST);
        mainContent.add(createResultsTableCard(), BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(UIConstants.COLOR_CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JLabel titleLabel = new JLabel("Search Available Rooms");
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel descLabel = new JLabel("Guest Services Module — Find available rooms by category and stay dates with automatic overlap detection.");
        descLabel.setFont(UIConstants.FONT_BODY);
        descLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(descLabel);

        return header;
    }

    private JPanel createSearchCriteriaCard() {
        JPanel formCard = new JPanel(new BorderLayout(0, 10));
        formCard.setPreferredSize(new Dimension(380, 0));
        formCard.setBackground(UIConstants.COLOR_CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel formTitle = new JLabel("Search Criteria");
        formTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        formTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 2, 4, 2);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.38;
        fieldsPanel.add(createFieldLabel("Room Type:"), gbc);
        cmbRoomType = new JComboBox<>(RoomType.values());
        cmbRoomType.setFont(UIConstants.FONT_BODY);
        cmbRoomType.addActionListener(e -> updateDerivedPackageInfo());
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.62;
        fieldsPanel.add(cmbRoomType, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.38;
        fieldsPanel.add(createFieldLabel("Check-in Date:"), gbc);
        txtCheckInDate = new JTextField();
        txtCheckInDate.setFont(UIConstants.FONT_BODY);
        txtCheckInDate.setText(LocalDate.now().format(STRICT_DATE_FORMATTER));
        txtCheckInDate.setToolTipText("Format: yyyy-MM-dd");
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.62;
        fieldsPanel.add(txtCheckInDate, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.38;
        fieldsPanel.add(createFieldLabel("Check-out Date:"), gbc);
        txtCheckOutDate = new JTextField();
        txtCheckOutDate.setFont(UIConstants.FONT_BODY);
        txtCheckOutDate.setText(LocalDate.now().plusDays(1).format(STRICT_DATE_FORMATTER));
        txtCheckOutDate.setToolTipText("Format: yyyy-MM-dd");
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.62;
        fieldsPanel.add(txtCheckOutDate, gbc);

        centerContent.add(fieldsPanel);
        centerContent.add(Box.createVerticalStrut(8));

        centerContent.add(createPackageInfoBox());
        centerContent.add(Box.createVerticalStrut(8));

        centerContent.add(createSelectedRoomSummaryBox());

        JScrollPane centerScroll = new JScrollPane(centerContent);
        centerScroll.setBorder(null);
        centerScroll.setOpaque(false);
        centerScroll.getViewport().setOpaque(false);
        formCard.add(centerScroll, BorderLayout.CENTER);

        formCard.add(createActionButtonsPanel(), BorderLayout.SOUTH);

        updateDerivedPackageInfo();

        return formCard;
    }

    private JPanel createPackageInfoBox() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(UIConstants.COLOR_BACKGROUND);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);

        JLabel boxTitle = new JLabel("Room Package Information");
        boxTitle.setFont(UIConstants.FONT_MUTED);
        boxTitle.setForeground(UIConstants.COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        box.add(boxTitle, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.5;
        box.add(createMutedLabel("Daily Rate:"), gbc);
        lblDailyRateValue = createValueLabel("");
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.5;
        box.add(lblDailyRateValue, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.5;
        box.add(createMutedLabel("Breakfast:"), gbc);
        lblBreakfastValue = createValueLabel("");
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.5;
        box.add(lblBreakfastValue, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.5;
        box.add(createMutedLabel("Tax Rate:"), gbc);
        lblTaxRateValue = createValueLabel("");
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.5;
        box.add(lblTaxRateValue, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.5;
        box.add(createMutedLabel("Extra Guest Fee:"), gbc);
        lblExtraGuestValue = createValueLabel("");
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.5;
        box.add(lblExtraGuestValue, gbc);

        return box;
    }

    private JPanel createSelectedRoomSummaryBox() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(UIConstants.COLOR_BACKGROUND);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);

        JLabel boxTitle = new JLabel("Selected Room Summary");
        boxTitle.setFont(UIConstants.FONT_MUTED);
        boxTitle.setForeground(UIConstants.COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        box.add(boxTitle, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.45;
        box.add(createMutedLabel("Room ID:"), gbc);
        lblSelectedRoomId = createValueLabel("None");
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.55;
        box.add(lblSelectedRoomId, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.45;
        box.add(createMutedLabel("Package:"), gbc);
        lblSelectedRoomType = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.55;
        box.add(lblSelectedRoomType, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.45;
        box.add(createMutedLabel("Bed Config:"), gbc);
        lblSelectedRoomBed = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.55;
        box.add(lblSelectedRoomBed, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.45;
        box.add(createMutedLabel("Max Capacity:"), gbc);
        lblSelectedRoomCapacity = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.55;
        box.add(lblSelectedRoomCapacity, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.45;
        box.add(createMutedLabel("Daily Rate:"), gbc);
        lblSelectedRoomRate = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 0.55;
        box.add(lblSelectedRoomRate, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.45;
        box.add(createMutedLabel("Stay Duration:"), gbc);
        lblSelectedRoomNights = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 0.55;
        box.add(lblSelectedRoomNights, gbc);

        return box;
    }

    private JPanel createActionButtonsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 6, 6));
        panel.setOpaque(false);

        JButton btnSearch = createStyledButton("Search Rooms", UIConstants.COLOR_PRIMARY, Color.WHITE);
        JButton btnClear = createStyledButton("Clear", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);
        JButton btnRefresh = createStyledButton("Refresh Results", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);

        btnSearch.addActionListener(e -> handleSearch());
        btnClear.addActionListener(e -> handleClear());
        btnRefresh.addActionListener(e -> handleRefresh());

        panel.add(btnSearch);
        panel.add(btnClear);
        panel.add(btnRefresh);

        return panel;
    }

    private JPanel createResultsTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(UIConstants.COLOR_CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JPanel topInfo = new JPanel(new BorderLayout());
        topInfo.setOpaque(false);

        JLabel tableTitle = new JLabel("Available Rooms");
        tableTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        tableTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        lblTableSummary = new JLabel("Found: 0 rooms");
        lblTableSummary.setFont(UIConstants.FONT_MUTED);
        lblTableSummary.setForeground(UIConstants.COLOR_TEXT_MUTED);

        topInfo.add(tableTitle, BorderLayout.WEST);
        topInfo.add(lblTableSummary, BorderLayout.EAST);
        tableCard.add(topInfo, BorderLayout.NORTH);

        String[] columnNames = {
                "Room ID",
                "Room Type",
                "Bed Type",
                "Capacity",
                "Daily Rate (LKR)",
                "Breakfast",
                "Tax Rate",
                "Status"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomTable = new JTable(tableModel);
        roomTable.setFont(UIConstants.FONT_BODY);
        roomTable.setRowHeight(26);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.getTableHeader().setFont(UIConstants.FONT_BODY_BOLD);
        roomTable.getTableHeader().setBackground(new Color(241, 245, 249));
        roomTable.getTableHeader().setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        roomTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        roomTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        roomTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        roomTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        roomTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        roomTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        roomTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableRowSelection();
            }
        });

        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        return tableCard;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footer.setOpaque(false);

        lblStatusFeedback = new JLabel("Ready. Select room type and dates, then click 'Search Rooms'.");
        lblStatusFeedback.setFont(UIConstants.FONT_MUTED);
        lblStatusFeedback.setForeground(UIConstants.COLOR_TEXT_MUTED);

        footer.add(lblStatusFeedback);
        return footer;
    }

    private void handleSearch() {
        RoomType selectedType = (RoomType) cmbRoomType.getSelectedItem();
        if (selectedType == null) {
            showWarning("Please select a Room Type.");
            return;
        }

        String checkInStr = txtCheckInDate.getText().trim();
        String checkOutStr = txtCheckOutDate.getText().trim();

        if (checkInStr.isEmpty()) {
            showWarning("Please enter a check-in date in yyyy-MM-dd format.");
            txtCheckInDate.requestFocus();
            return;
        }
        if (checkOutStr.isEmpty()) {
            showWarning("Please enter a check-out date in yyyy-MM-dd format.");
            txtCheckOutDate.requestFocus();
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
        try {
            checkIn = LocalDate.parse(checkInStr, STRICT_DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            showWarning("Invalid check-in calendar date: '" + checkInStr + "'. Please check the year, month, and day.");
            txtCheckInDate.requestFocus();
            return;
        }

        LocalDate checkOut;
        try {
            checkOut = LocalDate.parse(checkOutStr, STRICT_DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            showWarning("Invalid check-out calendar date: '" + checkOutStr + "'. Please check the year, month, and day.");
            txtCheckOutDate.requestFocus();
            return;
        }

        if (checkIn.isBefore(LocalDate.now())) {
            showWarning("Check-in date cannot be in the past for new searches.");
            txtCheckInDate.requestFocus();
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            showWarning("Check-out date (" + checkOutStr + ") must logically be after check-in date (" + checkInStr + ").");
            txtCheckOutDate.requestFocus();
            return;
        }

        try {
            List<Room> availableRooms = reservationService.searchAvailableRooms(selectedType, checkIn, checkOut);
            currentResults = availableRooms;
            lastSearchedCheckIn = checkIn;
            lastSearchedCheckOut = checkOut;

            renderTableRows(availableRooms);
            clearSelectedRoomSummary();

            if (availableRooms.isEmpty()) {
                lblTableSummary.setText("Found: 0 available rooms");
                updateStatusFeedback("No available rooms found for " + selectedType.getDisplayName() + " between " + checkIn + " and " + checkOut + ".", false);
                if (isShowing()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No matching " + selectedType.getDisplayName() + " rooms are available for the selected dates ("
                                    + checkIn + " to " + checkOut + ").\nPlease try different dates or another room category.",
                            "No Matching Rooms Available",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } else {
                lblTableSummary.setText("Found: " + availableRooms.size() + " room" + (availableRooms.size() == 1 ? "" : "s"));
                updateStatusFeedback("Search successful: " + availableRooms.size() + " available " + selectedType.getDisplayName() + " room(s) found.", false);
            }
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        }
    }

    private void handleClear() {
        txtCheckInDate.setText(LocalDate.now().format(STRICT_DATE_FORMATTER));
        txtCheckOutDate.setText(LocalDate.now().plusDays(1).format(STRICT_DATE_FORMATTER));
        if (cmbRoomType.getItemCount() > 0) {
            cmbRoomType.setSelectedIndex(0);
        }
        currentResults.clear();
        lastSearchedCheckIn = null;
        lastSearchedCheckOut = null;
        tableModel.setRowCount(0);
        lblTableSummary.setText("Found: 0 rooms");
        clearSelectedRoomSummary();
        updateStatusFeedback("Search criteria reset. Enter dates and click 'Search Rooms'.", false);
    }

    private void handleRefresh() {
        handleSearch();
        updateStatusFeedback("Search results refreshed with latest room inventory.", false);
    }

    private void handleTableRowSelection() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
            String roomId = (String) tableModel.getValueAt(selectedRow, 0);
            Room selectedRoom = null;
            for (Room r : currentResults) {
                if (r.getRoomId().equalsIgnoreCase(roomId)) {
                    selectedRoom = r;
                    break;
                }
            }

            if (selectedRoom != null) {
                lblSelectedRoomId.setText(selectedRoom.getRoomId());
                lblSelectedRoomType.setText(selectedRoom.getRoomType().getDisplayName());
                lblSelectedRoomBed.setText(selectedRoom.getBedType().getDisplayName());
                lblSelectedRoomCapacity.setText(selectedRoom.getCapacity() + " Guests");
                lblSelectedRoomRate.setText("LKR " + String.format("%,.2f", selectedRoom.getDailyRate()));

                if (lastSearchedCheckIn != null && lastSearchedCheckOut != null) {
                    long nights = ChronoUnit.DAYS.between(lastSearchedCheckIn, lastSearchedCheckOut);
                    lblSelectedRoomNights.setText(nights + " night" + (nights == 1 ? "" : "s") + " (" + lastSearchedCheckIn + " to " + lastSearchedCheckOut + ")");
                } else {
                    lblSelectedRoomNights.setText("-");
                }

                updateStatusFeedback("Selected: Room " + selectedRoom.getRoomId() + " (" + selectedRoom.getRoomType().getDisplayName() + "). Ready for booking.", false);
            }
        } else {
            clearSelectedRoomSummary();
        }
    }

    private void clearSelectedRoomSummary() {
        lblSelectedRoomId.setText("None");
        lblSelectedRoomType.setText("-");
        lblSelectedRoomBed.setText("-");
        lblSelectedRoomCapacity.setText("-");
        lblSelectedRoomRate.setText("-");
        lblSelectedRoomNights.setText("-");
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

    private void updateDerivedPackageInfo() {
        RoomType selected = (RoomType) cmbRoomType.getSelectedItem();
        if (selected != null) {
            lblDailyRateValue.setText("LKR " + String.format("%,.2f", selected.getDailyRate()));
            lblBreakfastValue.setText(selected.hasComplimentaryBreakfast() ? "Included" : "None");
            lblTaxRateValue.setText(String.format("%.0f%%", selected.getTaxRate() * 100));
            lblExtraGuestValue.setText("LKR " + String.format("%,.2f", selected.getExtraGuestCharge()));
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
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        return button;
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

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JLabel getLblTableSummary() {
        return lblTableSummary;
    }

    public JLabel getLblStatusFeedback() {
        return lblStatusFeedback;
    }

    public void executeSearch() {
        handleSearch();
    }

    public void executeClear() {
        handleClear();
    }
}
