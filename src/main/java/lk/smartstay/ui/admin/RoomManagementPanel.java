package lk.smartstay.ui.admin;

import lk.smartstay.enums.BedType;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Room;
import lk.smartstay.repository.RoomRepository;
import lk.smartstay.service.RoomService;
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
import java.util.List;

public class RoomManagementPanel extends JPanel {

    private final RoomService roomService;

    private JTextField txtRoomId;
    private JComboBox<RoomType> cmbRoomType;
    private JComboBox<BedType> cmbBedType;
    private JSpinner spinCapacity;
    private JComboBox<RoomStatus> cmbStatus;

    private JLabel lblDailyRateValue;
    private JLabel lblTaxRateValue;
    private JLabel lblExtraGuestValue;
    private JLabel lblBreakfastValue;

    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JLabel lblTableSummary;
    private JLabel lblStatusFeedback;

    public RoomManagementPanel(RoomService roomService) {
        this.roomService = roomService;
        initComponents();
        refreshTableData();
    }

    public RoomManagementPanel() {
        this(new RoomService(new RoomRepository()));
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(18, 0));
        mainContent.setOpaque(false);

        mainContent.add(createFormCard(), BorderLayout.WEST);
        mainContent.add(createTableCard(), BorderLayout.CENTER);

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

        JLabel titleLabel = new JLabel("Room Inventory Management");
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel descLabel = new JLabel("Administrator Module — Add, update, delete, and manage availability of hotel rooms.");
        descLabel.setFont(UIConstants.FONT_BODY);
        descLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(descLabel);

        return header;
    }

    private JPanel createFormCard() {
        JPanel formCard = new JPanel(new BorderLayout(0, 12));
        formCard.setPreferredSize(new Dimension(380, 0));
        formCard.setBackground(UIConstants.COLOR_CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JLabel formTitle = new JLabel("Room Specifications");
        formTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        formTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 2, 5, 2);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        fieldsPanel.add(createFieldLabel("Room ID:"), gbc);
        txtRoomId = new JTextField();
        txtRoomId.setFont(UIConstants.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.65;
        fieldsPanel.add(txtRoomId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        fieldsPanel.add(createFieldLabel("Room Type:"), gbc);
        cmbRoomType = new JComboBox<>(RoomType.values());
        cmbRoomType.setFont(UIConstants.FONT_BODY);
        cmbRoomType.addActionListener(e -> updateDerivedPackageInfo());
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.65;
        fieldsPanel.add(cmbRoomType, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        fieldsPanel.add(createFieldLabel("Bed Type:"), gbc);
        cmbBedType = new JComboBox<>(BedType.values());
        cmbBedType.setFont(UIConstants.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.65;
        fieldsPanel.add(cmbBedType, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.35;
        fieldsPanel.add(createFieldLabel("Capacity (Guests):"), gbc);
        spinCapacity = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
        spinCapacity.setFont(UIConstants.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.65;
        fieldsPanel.add(spinCapacity, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.35;
        fieldsPanel.add(createFieldLabel("Status:"), gbc);
        cmbStatus = new JComboBox<>(RoomStatus.values());
        cmbStatus.setFont(UIConstants.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.65;
        fieldsPanel.add(cmbStatus, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(12, 2, 8, 2);
        fieldsPanel.add(createDerivedInfoBox(), gbc);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        formCard.add(createActionButtonsPanel(), BorderLayout.SOUTH);

        updateDerivedPackageInfo();

        return formCard;
    }

    private JPanel createDerivedInfoBox() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(UIConstants.COLOR_BACKGROUND);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 2, 3, 2);

        JLabel boxTitle = new JLabel("Fixed Package Information (Derived)");
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
        box.add(createMutedLabel("Tax Rate:"), gbc);
        lblTaxRateValue = createValueLabel("");
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.5;
        box.add(lblTaxRateValue, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.5;
        box.add(createMutedLabel("Extra Guest Fee:"), gbc);
        lblExtraGuestValue = createValueLabel("");
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.5;
        box.add(lblExtraGuestValue, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.5;
        box.add(createMutedLabel("Breakfast:"), gbc);
        lblBreakfastValue = createValueLabel("");
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.5;
        box.add(lblBreakfastValue, gbc);

        return box;
    }

    private JPanel createActionButtonsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setOpaque(false);

        JButton btnAdd = createStyledButton("Add Room", UIConstants.COLOR_ACCENT_GOLD, Color.WHITE);
        JButton btnUpdate = createStyledButton("Update Room", UIConstants.COLOR_PRIMARY, Color.WHITE);
        JButton btnDelete = createStyledButton("Delete Room", UIConstants.COLOR_DANGER, Color.WHITE);
        JButton btnClear = createStyledButton("Clear Form", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);
        JButton btnRefresh = createStyledButton("Refresh Table", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);

        btnAdd.addActionListener(e -> handleAddRoom());
        btnUpdate.addActionListener(e -> handleUpdateRoom());
        btnDelete.addActionListener(e -> handleDeleteRoom());
        btnClear.addActionListener(e -> handleClearForm());
        btnRefresh.addActionListener(e -> {
            refreshTableData();
            updateStatusFeedback("Table refreshed with latest room records.", false);
        });

        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnClear);
        panel.add(btnRefresh);

        return panel;
    }

    private JPanel createTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(UIConstants.COLOR_CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JPanel topInfo = new JPanel(new BorderLayout());
        topInfo.setOpaque(false);

        JLabel tableTitle = new JLabel("Configured Rooms Directory");
        tableTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        tableTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        lblTableSummary = new JLabel("Total: 0 rooms");
        lblTableSummary.setFont(UIConstants.FONT_MUTED);
        lblTableSummary.setForeground(UIConstants.COLOR_TEXT_MUTED);

        topInfo.add(tableTitle, BorderLayout.WEST);
        topInfo.add(lblTableSummary, BorderLayout.EAST);
        tableCard.add(topInfo, BorderLayout.NORTH);

        String[] columnNames = {
                "Room ID", "Room Type", "Bed Type", "Capacity",
                "Daily Rate (LKR)", "Breakfast", "Extra Guest Fee", "Tax", "Status"
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
        roomTable.getColumnModel().getColumn(6).setCellRenderer(UIConstants.createRightAlignRenderer());
        roomTable.getColumnModel().getColumn(7).setCellRenderer(UIConstants.createCenterAlignRenderer());
        roomTable.getColumnModel().getColumn(8).setCellRenderer(UIConstants.createStatusBadgeRenderer());

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

        lblStatusFeedback = new JLabel("Ready. Select a room record or fill the form to begin.");
        lblStatusFeedback.setFont(UIConstants.FONT_MUTED);
        lblStatusFeedback.setForeground(UIConstants.COLOR_TEXT_MUTED);

        footer.add(lblStatusFeedback);
        return footer;
    }

    private void handleAddRoom() {
        String roomId = txtRoomId.getText().trim();
        RoomType roomType = (RoomType) cmbRoomType.getSelectedItem();
        BedType bedType = (BedType) cmbBedType.getSelectedItem();
        int capacity = (Integer) spinCapacity.getValue();
        RoomStatus status = (RoomStatus) cmbStatus.getSelectedItem();

        if (roomId.isEmpty()) {
            showWarning("Please enter a Room ID.");
            txtRoomId.requestFocus();
            return;
        }
        if (roomType == null) {
            showWarning("Please select a Room Type.");
            return;
        }
        if (bedType == null) {
            showWarning("Please select a Bed Type.");
            return;
        }
        if (capacity <= 0) {
            showWarning("Room capacity must be greater than zero.");
            return;
        }
        if (status == null) {
            showWarning("Please select a Room Status.");
            return;
        }

        Room newRoom = new Room(roomId, roomType, bedType, capacity, status);
        boolean success = roomService.addRoom(newRoom);

        if (success) {
            refreshTableData();
            handleClearForm();
            updateStatusFeedback("Room " + roomId + " added successfully.", false);
            if (isShowing()) {
                JOptionPane.showMessageDialog(this, "Room " + roomId + " added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            showWarning("A room with ID '" + roomId + "' already exists.");
        }
    }

    private void handleUpdateRoom() {
        String roomId = txtRoomId.getText().trim();
        if (roomId.isEmpty()) {
            showWarning("Please select or enter the Room ID of the record you wish to update.");
            txtRoomId.requestFocus();
            return;
        }

        Room existing = roomService.findRoomById(roomId);
        if (existing == null) {
            showWarning("Room ID '" + roomId + "' does not exist. Cannot update.");
            return;
        }

        RoomType roomType = (RoomType) cmbRoomType.getSelectedItem();
        BedType bedType = (BedType) cmbBedType.getSelectedItem();
        int capacity = (Integer) spinCapacity.getValue();
        RoomStatus status = (RoomStatus) cmbStatus.getSelectedItem();

        if (capacity <= 0) {
            showWarning("Room capacity must be greater than zero.");
            return;
        }

        Room updatedRoom = new Room(roomId, roomType, bedType, capacity, status);
        boolean success = roomService.updateRoom(updatedRoom);

        if (success) {
            refreshTableData();
            updateStatusFeedback("Room " + roomId + " updated successfully.", false);
            if (isShowing()) {
                JOptionPane.showMessageDialog(this, "Room " + roomId + " updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            showWarning("Failed to update room '" + roomId + "'.");
        }
    }

    private void handleDeleteRoom() {
        String roomId = txtRoomId.getText().trim();
        if (roomId.isEmpty()) {
            showWarning("Please select a room from the table or enter a Room ID to delete.");
            return;
        }

        Room room = roomService.findRoomById(roomId);
        if (room == null) {
            showWarning("Room '" + roomId + "' does not exist in the repository.");
            return;
        }

        int confirm = JOptionPane.YES_OPTION;
        if (isShowing()) {
            confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to permanently delete Room " + roomId + " (" + room.getRoomType().getDisplayName() + ")?",
                    "Confirm Room Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
        }

        if (confirm == JOptionPane.YES_OPTION) {
            boolean removed = roomService.removeRoom(roomId);
            if (removed) {
                refreshTableData();
                handleClearForm();
                updateStatusFeedback("Room " + roomId + " removed successfully.", false);
                if (isShowing()) {
                    JOptionPane.showMessageDialog(this, "Room " + roomId + " was removed.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                showWarning("Could not delete room '" + roomId + "'. It may be referenced by existing reservation history or does not exist.");
            }
        }
    }

    private void handleClearForm() {
        txtRoomId.setText("");
        txtRoomId.setEditable(true);
        cmbRoomType.setSelectedIndex(0);
        cmbBedType.setSelectedIndex(0);
        spinCapacity.setValue(2);
        cmbStatus.setSelectedItem(RoomStatus.AVAILABLE);
        updateDerivedPackageInfo();
        roomTable.clearSelection();
        updateStatusFeedback("Form cleared.", false);
    }

    private void handleTableRowSelection() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
            String roomId = (String) tableModel.getValueAt(selectedRow, 0);
            Room room = roomService.findRoomById(roomId);
            if (room != null) {
                txtRoomId.setText(room.getRoomId());
                cmbRoomType.setSelectedItem(room.getRoomType());
                cmbBedType.setSelectedItem(room.getBedType());
                spinCapacity.setValue(room.getCapacity());
                cmbStatus.setSelectedItem(room.getStatus());
                updateDerivedPackageInfo();
                updateStatusFeedback("Editing Room: " + roomId + ". Make changes and click 'Update Room'.", false);
            }
        }
    }

    private void updateDerivedPackageInfo() {
        RoomType selected = (RoomType) cmbRoomType.getSelectedItem();
        if (selected != null) {
            lblDailyRateValue.setText(String.format("LKR %,.2f", selected.getDailyRate()));
            lblTaxRateValue.setText(String.format("%.0f%%", selected.getTaxRate() * 100));
            lblExtraGuestValue.setText(String.format("LKR %,.2f", selected.getExtraGuestCharge()));
            lblBreakfastValue.setText(selected.hasComplimentaryBreakfast() ? "Yes (Included)" : "No");
        }
    }

    public void refreshTableData() {
        tableModel.setRowCount(0);
        List<Room> allRooms = roomService.getAllRooms();
        for (Room r : allRooms) {
            tableModel.addRow(new Object[]{
                    r.getRoomId(),
                    r.getRoomType().getDisplayName(),
                    r.getBedType().getDisplayName(),
                    r.getCapacity(),
                    String.format("%,.2f", r.getDailyRate()),
                    r.hasComplimentaryBreakfast() ? "Yes" : "No",
                    String.format("%,.2f", r.getExtraGuestCharge()),
                    String.format("%.0f%%", r.getTaxRate() * 100),
                    r.getStatus().getDisplayName()
            });
        }
        lblTableSummary.setText("Total: " + allRooms.size() + " room" + (allRooms.size() == 1 ? "" : "s"));
    }

    private void updateStatusFeedback(String message, boolean isError) {
        lblStatusFeedback.setText(message);
        lblStatusFeedback.setForeground(isError ? new Color(185, 28, 28) : UIConstants.COLOR_TEXT_MUTED);
    }

    private void showWarning(String message) {
        updateStatusFeedback(message, true);
        if (isShowing()) {
            JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
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

    public JTextField getTxtRoomId() {
        return txtRoomId;
    }

    public JComboBox<RoomType> getCmbRoomType() {
        return cmbRoomType;
    }

    public JComboBox<BedType> getCmbBedType() {
        return cmbBedType;
    }

    public JSpinner getSpinCapacity() {
        return spinCapacity;
    }

    public JComboBox<RoomStatus> getCmbStatus() {
        return cmbStatus;
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

    public void executeAddRoom() {
        handleAddRoom();
    }

    public void executeUpdateRoom() {
        handleUpdateRoom();
    }

    public void executeDeleteRoom() {
        handleDeleteRoom();
    }

    public void executeClearForm() {
        handleClearForm();
    }
}
