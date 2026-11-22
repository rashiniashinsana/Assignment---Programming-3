package lk.smartstay.ui.guest;

import lk.smartstay.model.Guest;
import lk.smartstay.repository.GuestRepository;
import lk.smartstay.service.GuestService;
import lk.smartstay.util.IdGenerator;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GuestRegistrationPanel extends JPanel {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s-]{7,15}$");

    private final GuestService guestService;

    private JTextField txtGuestId;
    private JTextField txtNicPassport;
    private JTextField txtFullName;
    private JTextField txtContactNumber;
    private JTextField txtEmail;

    private JTextField txtSearch;
    private JComboBox<String> cmbSearchCriteria;

    private JTable guestTable;
    private DefaultTableModel tableModel;
    private JLabel lblTableSummary;
    private JLabel lblStatusFeedback;

    public GuestRegistrationPanel(GuestService guestService) {
        this.guestService = guestService;
        initComponents();
        refreshTableData();
    }

    public GuestRegistrationPanel() {
        this(new GuestService(new GuestRepository()));
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

        JLabel titleLabel = new JLabel("Guest Registration & Customer Profiles");
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel descLabel = new JLabel("Register local and international guests, manage identity details, and search customer records.");
        descLabel.setFont(UIConstants.FONT_BODY);
        descLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(descLabel);

        return header;
    }

    private JPanel createFormCard() {
        JPanel formCard = new JPanel(new BorderLayout(0, 14));
        formCard.setPreferredSize(new Dimension(380, 0));
        formCard.setBackground(UIConstants.COLOR_CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JLabel formTitle = new JLabel("Customer Information");
        formTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        formTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 2, 6, 2);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        fieldsPanel.add(createFieldLabel("Guest ID:"), gbc);
        txtGuestId = new JTextField(IdGenerator.peekNextGuestId());
        txtGuestId.setFont(UIConstants.FONT_BODY);
        txtGuestId.setEditable(false);
        txtGuestId.setBackground(new Color(241, 245, 249));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.65;
        fieldsPanel.add(txtGuestId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        fieldsPanel.add(createFieldLabel("NIC / Passport:"), gbc);
        txtNicPassport = new JTextField();
        txtNicPassport.setFont(UIConstants.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.65;
        fieldsPanel.add(txtNicPassport, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        fieldsPanel.add(createFieldLabel("Full Name:"), gbc);
        txtFullName = new JTextField();
        txtFullName.setFont(UIConstants.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.65;
        fieldsPanel.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.35;
        fieldsPanel.add(createFieldLabel("Contact Number:"), gbc);
        txtContactNumber = new JTextField();
        txtContactNumber.setFont(UIConstants.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.65;
        fieldsPanel.add(txtContactNumber, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.35;
        fieldsPanel.add(createFieldLabel("Email Address:"), gbc);
        txtEmail = new JTextField();
        txtEmail.setFont(UIConstants.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.65;
        fieldsPanel.add(txtEmail, gbc);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        formCard.add(createActionButtonsPanel(), BorderLayout.SOUTH);

        return formCard;
    }

    private JPanel createActionButtonsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.setOpaque(false);

        JButton btnRegister = createStyledButton("Register Guest", UIConstants.COLOR_PRIMARY, Color.WHITE);
        JButton btnUpdate = createStyledButton("Update Guest", UIConstants.COLOR_SIDEBAR_ACTIVE, Color.WHITE);
        JButton btnClear = createStyledButton("Clear Form", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);
        JButton btnRefresh = createStyledButton("Refresh Table", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);

        btnRegister.addActionListener(e -> handleRegisterGuest());
        btnUpdate.addActionListener(e -> handleUpdateGuest());
        btnClear.addActionListener(e -> handleClearForm());
        btnRefresh.addActionListener(e -> {
            refreshTableData();
            updateStatusFeedback("Table reloaded with latest guest records.", false);
        });

        panel.add(btnRegister);
        panel.add(btnUpdate);
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

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setOpaque(false);

        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchControls.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UIConstants.FONT_BODY_BOLD);
        lblSearch.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        txtSearch = new JTextField(12);
        txtSearch.setFont(UIConstants.FONT_BODY);

        cmbSearchCriteria = new JComboBox<>(new String[]{"Guest Name", "Guest ID", "NIC / Passport"});
        cmbSearchCriteria.setFont(UIConstants.FONT_BODY);

        JButton btnSearch = createStyledButton("Search", UIConstants.COLOR_PRIMARY, Color.WHITE);
        JButton btnShowAll = createStyledButton("Show All", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);

        btnSearch.addActionListener(e -> handleSearch());
        btnShowAll.addActionListener(e -> {
            txtSearch.setText("");
            refreshTableData();
            updateStatusFeedback("All guest records displayed.", false);
        });

        searchControls.add(lblSearch);
        searchControls.add(txtSearch);
        searchControls.add(cmbSearchCriteria);
        searchControls.add(btnSearch);
        searchControls.add(btnShowAll);

        lblTableSummary = new JLabel("Total: 0 guests");
        lblTableSummary.setFont(UIConstants.FONT_MUTED);
        lblTableSummary.setForeground(UIConstants.COLOR_TEXT_MUTED);

        topPanel.add(searchControls, BorderLayout.WEST);
        topPanel.add(lblTableSummary, BorderLayout.EAST);
        tableCard.add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"Guest ID", "NIC / Passport", "Full Name", "Contact Number", "Email Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        guestTable = new JTable(tableModel);
        guestTable.setFont(UIConstants.FONT_BODY);
        guestTable.setRowHeight(26);
        guestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        guestTable.getTableHeader().setFont(UIConstants.FONT_BODY_BOLD);
        guestTable.getTableHeader().setBackground(new Color(241, 245, 249));
        guestTable.getTableHeader().setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        guestTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        guestTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        guestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableRowSelection();
            }
        });

        JScrollPane scrollPane = new JScrollPane(guestTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        return tableCard;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footer.setOpaque(false);

        lblStatusFeedback = new JLabel("Ready. Enter guest details to register a new customer profile.");
        lblStatusFeedback.setFont(UIConstants.FONT_MUTED);
        lblStatusFeedback.setForeground(UIConstants.COLOR_TEXT_MUTED);

        footer.add(lblStatusFeedback);
        return footer;
    }

    private void handleRegisterGuest() {
        String guestId = txtGuestId.getText().trim();
        String nic = txtNicPassport.getText().trim();
        String name = txtFullName.getText().trim();
        String contact = txtContactNumber.getText().trim();
        String email = txtEmail.getText().trim();

        if (!validateInputForm(nic, name, contact, email)) {
            return;
        }

        if (guestService.findGuestByNicOrPassport(nic) != null) {
            showWarning("A guest with this NIC or Passport is already registered.");
            txtNicPassport.requestFocus();
            return;
        }

        if (guestId.equals(IdGenerator.peekNextGuestId())) {
            guestId = IdGenerator.generateGuestId();
        }

        Guest newGuest = new Guest(guestId, nic, name, contact, email);
        boolean success = guestService.registerGuest(newGuest);

        if (success) {
            refreshTableData();
            handleClearForm();
            updateStatusFeedback("Guest " + name + " (" + guestId + ") registered successfully.", false);
            if (isShowing()) {
                JOptionPane.showMessageDialog(this, "Guest registered successfully with ID: " + guestId, "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            showWarning("Failed to register guest. Please check whether the ID or NIC is already registered.");
        }
    }

    private void handleUpdateGuest() {
        String guestId = txtGuestId.getText().trim();
        if (guestId.isEmpty()) {
            showWarning("Please select an existing guest from the table to update.");
            return;
        }

        Guest existing = guestService.findGuestById(guestId);
        if (existing == null) {
            showWarning("Guest ID '" + guestId + "' does not exist. Cannot update.");
            return;
        }

        String nic = txtNicPassport.getText().trim();
        String name = txtFullName.getText().trim();
        String contact = txtContactNumber.getText().trim();
        String email = txtEmail.getText().trim();

        if (!validateInputForm(nic, name, contact, email)) {
            return;
        }

        Guest existingWithNic = guestService.findGuestByNicOrPassport(nic);
        if (existingWithNic != null && !existingWithNic.getGuestId().equalsIgnoreCase(guestId)) {
            showWarning("A guest with this NIC or Passport is already registered.");
            txtNicPassport.requestFocus();
            return;
        }

        Guest updatedGuest = new Guest(guestId, nic, name, contact, email);
        boolean success = guestService.updateGuest(updatedGuest);

        if (success) {
            refreshTableData();
            updateStatusFeedback("Guest profile for " + name + " (" + guestId + ") updated successfully.", false);
            if (isShowing()) {
                JOptionPane.showMessageDialog(this, "Guest details updated successfully.", "Update Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            showWarning("Failed to update guest details.");
        }
    }

    private void handleClearForm() {
        txtGuestId.setText(IdGenerator.peekNextGuestId());
        txtNicPassport.setText("");
        txtFullName.setText("");
        txtContactNumber.setText("");
        txtEmail.setText("");
        guestTable.clearSelection();
        updateStatusFeedback("Form cleared. Ready for new registration.", false);
    }

    private void handleTableRowSelection() {
        int selectedRow = guestTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
            String guestId = (String) tableModel.getValueAt(selectedRow, 0);
            Guest guest = guestService.findGuestById(guestId);
            if (guest != null) {
                txtGuestId.setText(guest.getGuestId());
                txtNicPassport.setText(guest.getNicOrPassport());
                txtFullName.setText(guest.getFullName());
                txtContactNumber.setText(guest.getContactNumber());
                txtEmail.setText(guest.getEmail());
                updateStatusFeedback("Selected Guest: " + guest.getFullName() + " (" + guestId + "). Edit and click 'Update Guest'.", false);
            }
        }
    }

    private void handleSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            refreshTableData();
            return;
        }

        String criteria = (String) cmbSearchCriteria.getSelectedItem();
        List<Guest> results = new ArrayList<>();

        if ("Guest ID".equals(criteria)) {
            Guest g = guestService.findGuestById(query);
            if (g != null) {
                results.add(g);
            }
        } else if ("NIC / Passport".equals(criteria)) {
            Guest g = guestService.findGuestByNicOrPassport(query);
            if (g != null) {
                results.add(g);
            }
        } else {

            results = guestService.searchGuestsByName(query);
        }

        renderTableRows(results);
        lblTableSummary.setText("Found: " + results.size() + " match" + (results.size() == 1 ? "" : "es"));
        updateStatusFeedback("Search completed with " + results.size() + " result(s).", false);

        if (results.isEmpty()) {
            if (isShowing()) {
                JOptionPane.showMessageDialog(this, "No guest records found matching: " + query, "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private boolean validateInputForm(String nic, String name, String contact, String email) {
        if (nic.isEmpty()) {
            showWarning("Please enter NIC or Passport number.");
            txtNicPassport.requestFocus();
            return false;
        }
        if (name.isEmpty()) {
            showWarning("Please enter the guest's full name.");
            txtFullName.requestFocus();
            return false;
        }
        if (contact.isEmpty()) {
            showWarning("Please enter a contact number.");
            txtContactNumber.requestFocus();
            return false;
        }
        if (!PHONE_PATTERN.matcher(contact).matches()) {
            showWarning("Please enter a valid contact number.");
            txtContactNumber.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            showWarning("Please enter an email address.");
            txtEmail.requestFocus();
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showWarning("Please enter a valid email address.");
            txtEmail.requestFocus();
            return false;
        }
        return true;
    }

    public void refreshTableData() {
        renderTableRows(guestService.getAllGuests());
        lblTableSummary.setText("Total: " + guestService.getGuestCount() + " guest" + (guestService.getGuestCount() == 1 ? "" : "s"));
    }

    private void renderTableRows(List<Guest> guests) {
        tableModel.setRowCount(0);
        for (Guest g : guests) {
            tableModel.addRow(new Object[]{
                    g.getGuestId(),
                    g.getNicOrPassport(),
                    g.getFullName(),
                    g.getContactNumber(),
                    g.getEmail()
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
            JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JLabel createFieldLabel(String text) {
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

    public JTextField getTxtGuestId() {
        return txtGuestId;
    }

    public JTextField getTxtNicPassport() {
        return txtNicPassport;
    }

    public JTextField getTxtFullName() {
        return txtFullName;
    }

    public JTextField getTxtContactNumber() {
        return txtContactNumber;
    }

    public JTextField getTxtEmail() {
        return txtEmail;
    }

    public JTextField getTxtSearch() {
        return txtSearch;
    }

    public JComboBox<String> getCmbSearchCriteria() {
        return cmbSearchCriteria;
    }

    public JTable getGuestTable() {
        return guestTable;
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

    public void executeRegisterGuest() {
        handleRegisterGuest();
    }

    public void executeUpdateGuest() {
        handleUpdateGuest();
    }

    public void executeClearForm() {
        handleClearForm();
    }

    public void executeSearch() {
        handleSearch();
    }
}
