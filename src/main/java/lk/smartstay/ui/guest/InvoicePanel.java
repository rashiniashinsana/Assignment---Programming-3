package lk.smartstay.ui.guest;

import lk.smartstay.enums.ReservationStatus;
import lk.smartstay.model.Guest;
import lk.smartstay.model.Invoice;
import lk.smartstay.model.Reservation;
import lk.smartstay.model.Room;
import lk.smartstay.service.InvoiceService;
import lk.smartstay.service.ReservationService;
import lk.smartstay.util.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;

public class InvoicePanel extends JPanel {

    private final InvoiceService invoiceService;
    private final ReservationService reservationService;

    private JTextField txtSearchBookingId;
    private JButton btnSearchBooking;
    private JComboBox<String> cmbEligibleBookings;
    private JButton btnRefreshBookings;

    private JLabel lblSummaryBookingId;
    private JLabel lblSummaryGuest;
    private JLabel lblSummaryRoom;
    private JLabel lblSummaryDates;
    private JLabel lblSummaryNights;
    private JLabel lblSummaryGuests;
    private JLabel lblSummaryStatus;

    private JButton btnGenerateInvoice;
    private JButton btnPrintInvoice;
    private JButton btnSaveAsText;
    private JButton btnReset;

    private JEditorPane previewEditorPane;
    private JLabel lblPreviewStatus;
    private JLabel lblFooterFeedback;

    private Reservation currentSelectedReservation = null;
    private Invoice currentGeneratedInvoice = null;

    public InvoicePanel(InvoiceService invoiceService, ReservationService reservationService) {
        if (invoiceService == null) {
            throw new IllegalArgumentException("InvoiceService cannot be null.");
        }
        if (reservationService == null) {
            throw new IllegalArgumentException("ReservationService cannot be null.");
        }
        this.invoiceService = invoiceService;
        this.reservationService = reservationService;

        initComponents();
        populateEligibleBookingsDropdown();
    }

    public InvoicePanel() {
        this(new InvoiceService(), new ReservationService());
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel centerSplit = new JPanel(new BorderLayout(16, 0));
        centerSplit.setOpaque(false);

        centerSplit.add(createControlSidePanel(), BorderLayout.WEST);
        centerSplit.add(createInvoicePreviewPanel(), BorderLayout.CENTER);

        add(centerSplit, BorderLayout.CENTER);

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

        JLabel titleLabel = new JLabel("Invoices & Guest Billing");
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel descLabel = new JLabel("Financial & Billing Module — Generate itemized invoices, calculate stays, discounts, taxes, advance deductions, and print guest bills.");
        descLabel.setFont(UIConstants.FONT_BODY);
        descLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(3));
        header.add(descLabel);

        return header;
    }

    private JPanel createControlSidePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(440, 0));
        panel.setBackground(UIConstants.COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel sideTitle = new JLabel("Reservation Billing Selector");
        sideTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        sideTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        panel.add(sideTitle, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(createSearchSection());
        content.add(Box.createVerticalStrut(10));
        content.add(createDropdownSection());
        content.add(Box.createVerticalStrut(12));
        content.add(createBookingSummarySection());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);

        panel.add(createActionsPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createSearchSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);

        JLabel lblTitle = new JLabel("1. Search by Booking ID");
        lblTitle.setFont(UIConstants.FONT_BODY_BOLD);
        lblTitle.setForeground(UIConstants.COLOR_PRIMARY);
        section.add(lblTitle);
        section.add(Box.createVerticalStrut(4));

        JPanel searchRow = new JPanel(new BorderLayout(6, 0));
        searchRow.setOpaque(false);

        txtSearchBookingId = new JTextField();
        txtSearchBookingId.setFont(UIConstants.FONT_BODY);
        txtSearchBookingId.setToolTipText("Enter Booking ID (e.g. BKG001)");
        txtSearchBookingId.addActionListener(e -> handleSearchBooking());

        btnSearchBooking = createStyledButton("Find Booking", UIConstants.COLOR_PRIMARY, Color.WHITE);
        btnSearchBooking.addActionListener(e -> handleSearchBooking());

        searchRow.add(txtSearchBookingId, BorderLayout.CENTER);
        searchRow.add(btnSearchBooking, BorderLayout.EAST);

        section.add(searchRow);
        return section;
    }

    private JPanel createDropdownSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);

        JLabel lblTitle = new JLabel("2. Or Select from Billable Bookings");
        lblTitle.setFont(UIConstants.FONT_BODY_BOLD);
        lblTitle.setForeground(UIConstants.COLOR_PRIMARY);
        section.add(lblTitle);
        section.add(Box.createVerticalStrut(4));

        JPanel dropdownRow = new JPanel(new BorderLayout(6, 0));
        dropdownRow.setOpaque(false);

        cmbEligibleBookings = new JComboBox<>();
        cmbEligibleBookings.setFont(UIConstants.FONT_BODY);
        cmbEligibleBookings.addActionListener(e -> handleDropdownSelection());

        btnRefreshBookings = createStyledButton("↻", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);
        btnRefreshBookings.setToolTipText("Refresh billable bookings list");
        btnRefreshBookings.addActionListener(e -> {
            populateEligibleBookingsDropdown();
            updateFooterFeedback("Billable bookings list refreshed.", false);
        });

        dropdownRow.add(cmbEligibleBookings, BorderLayout.CENTER);
        dropdownRow.add(btnRefreshBookings, BorderLayout.EAST);

        section.add(dropdownRow);
        return section;
    }

    private JPanel createBookingSummarySection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);

        JLabel lblTitle = new JLabel("3. Selected Booking Inspection");
        lblTitle.setFont(UIConstants.FONT_BODY_BOLD);
        lblTitle.setForeground(UIConstants.COLOR_PRIMARY);
        section.add(lblTitle);
        section.add(Box.createVerticalStrut(6));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIConstants.COLOR_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 3, 2, 3);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        card.add(createFieldLabel("Booking ID:"), gbc);
        lblSummaryBookingId = createValueLabel("None (Select a booking)");
        lblSummaryBookingId.setForeground(UIConstants.COLOR_PRIMARY);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.65;
        card.add(lblSummaryBookingId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        card.add(createFieldLabel("Guest:"), gbc);
        lblSummaryGuest = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.65;
        card.add(lblSummaryGuest, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        card.add(createFieldLabel("Room:"), gbc);
        lblSummaryRoom = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.65;
        card.add(lblSummaryRoom, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.35;
        card.add(createFieldLabel("Stay Dates:"), gbc);
        lblSummaryDates = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.65;
        card.add(lblSummaryDates, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.35;
        card.add(createFieldLabel("Duration:"), gbc);
        lblSummaryNights = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.65;
        card.add(lblSummaryNights, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.35;
        card.add(createFieldLabel("Guests:"), gbc);
        lblSummaryGuests = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 0.65;
        card.add(lblSummaryGuests, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.35;
        card.add(createFieldLabel("Status:"), gbc);
        lblSummaryStatus = createValueLabel("-");
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 0.65;
        card.add(lblSummaryStatus, gbc);

        section.add(card);
        return section;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 6, 6));
        panel.setOpaque(false);

        btnGenerateInvoice = createStyledButton("Generate Invoice", UIConstants.COLOR_ACCENT_GOLD, Color.WHITE);
        btnGenerateInvoice.setEnabled(false);
        btnGenerateInvoice.addActionListener(e -> handleGenerateInvoice());

        btnPrintInvoice = createStyledButton("Print Invoice", new Color(15, 118, 110), Color.WHITE);
        btnPrintInvoice.setEnabled(false);
        btnPrintInvoice.setToolTipText("Print invoice via Java Swing printing system");
        btnPrintInvoice.addActionListener(e -> handlePrintInvoice());

        btnSaveAsText = createStyledButton("Save as Text", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);
        btnSaveAsText.setEnabled(false);
        btnSaveAsText.setToolTipText("Export itemized invoice to a text file (.txt)");
        btnSaveAsText.addActionListener(e -> handleSaveAsText());

        btnReset = createStyledButton("Reset", new Color(241, 245, 249), UIConstants.COLOR_TEXT_PRIMARY);
        btnReset.addActionListener(e -> handleReset());

        panel.add(btnGenerateInvoice);
        panel.add(btnPrintInvoice);
        panel.add(btnSaveAsText);
        panel.add(btnReset);

        return panel;
    }

    private JPanel createInvoicePreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UIConstants.COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JLabel previewTitle = new JLabel("Official Guest Tax Invoice");
        previewTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        previewTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        lblPreviewStatus = new JLabel("Awaiting invoice generation");
        lblPreviewStatus.setFont(UIConstants.FONT_MUTED);
        lblPreviewStatus.setForeground(UIConstants.COLOR_TEXT_MUTED);

        titleBar.add(previewTitle, BorderLayout.WEST);
        titleBar.add(lblPreviewStatus, BorderLayout.EAST);
        panel.add(titleBar, BorderLayout.NORTH);

        previewEditorPane = new JEditorPane();
        previewEditorPane.setEditable(false);
        previewEditorPane.setContentType("text/html");
        previewEditorPane.setBackground(Color.WHITE);
        previewEditorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        renderInitialPlaceholder();

        JScrollPane scrollPane = new JScrollPane(previewEditorPane);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footer.setOpaque(false);

        lblFooterFeedback = new JLabel("Ready. Search or select a confirmed booking to generate an itemized invoice.");
        lblFooterFeedback.setFont(UIConstants.FONT_MUTED);
        lblFooterFeedback.setForeground(UIConstants.COLOR_TEXT_MUTED);

        footer.add(lblFooterFeedback);
        return footer;
    }

    public void populateEligibleBookingsDropdown() {
        cmbEligibleBookings.removeAllItems();
        cmbEligibleBookings.addItem("-- Select an Eligible Booking --");

        List<Reservation> all = reservationService.getAllReservations();
        int count = 0;
        for (Reservation r : all) {
            if (!r.isCancelled() && r.getStatus() != ReservationStatus.CANCELLED) {
                String guestName = (r.getGuest() != null) ? r.getGuest().getFullName() : "Guest";
                String roomId = (r.getRoom() != null) ? r.getRoom().getRoomId() : "Room";
                cmbEligibleBookings.addItem(r.getBookingId() + " - " + guestName + " (" + roomId + ", " + r.getStatus().getDisplayName() + ")");
                count++;
            }
        }

        if (count == 0) {
            updateFooterFeedback("No active billable reservations found. Create a booking in Reservations tab.", false);
        }
    }

    private void handleSearchBooking() {
        String query = txtSearchBookingId.getText().trim();
        if (query.isEmpty()) {
            showWarning("Please enter a Booking ID to search.");
            txtSearchBookingId.requestFocus();
            return;
        }

        Reservation r = reservationService.searchByBookingId(query);
        if (r == null) {
            showWarning("No reservation record found for Booking ID: '" + query + "'.");
            return;
        }

        if (r.isCancelled() || r.getStatus() == ReservationStatus.CANCELLED) {
            showWarning("Reservation '" + query + "' is CANCELLED.\nCancelled reservations are non-billable and cannot be invoiced.");
            return;
        }

        for (int i = 0; i < cmbEligibleBookings.getItemCount(); i++) {
            String item = cmbEligibleBookings.getItemAt(i);
            if (item != null && item.startsWith(r.getBookingId() + " -")) {
                cmbEligibleBookings.setSelectedIndex(i);
                return;
            }
        }

        selectReservation(r);
    }

    private void handleDropdownSelection() {
        int index = cmbEligibleBookings.getSelectedIndex();
        if (index <= 0) {
            clearSelectedReservation();
            return;
        }

        String item = (String) cmbEligibleBookings.getSelectedItem();
        if (item != null && item.contains(" - ")) {
            String bookingId = item.substring(0, item.indexOf(" - ")).trim();
            Reservation r = reservationService.searchByBookingId(bookingId);
            if (r != null) {
                selectReservation(r);
            }
        }
    }

    private void selectReservation(Reservation r) {
        this.currentSelectedReservation = r;
        this.currentGeneratedInvoice = null;

        lblSummaryBookingId.setText(r.getBookingId());
        Guest g = r.getGuest();
        lblSummaryGuest.setText(g != null ? g.getFullName() + " (" + g.getGuestId() + ")" : "-");

        Room rm = r.getRoom();
        if (rm != null) {
            lblSummaryRoom.setText("Room " + rm.getRoomId() + " (" + rm.getRoomType().getDisplayName() + ", Rate: LKR " + String.format("%,.2f", rm.getDailyRate()) + ")");
        } else {
            lblSummaryRoom.setText("-");
        }

        lblSummaryDates.setText(r.getCheckInDate() + " to " + r.getCheckOutDate());
        lblSummaryNights.setText(r.calculateNumberOfNights() + " night" + (r.calculateNumberOfNights() == 1 ? "" : "s")
                + (r.qualifiesForLongStayDiscount() ? " (12% Discount Eligible)" : ""));
        lblSummaryGuests.setText(r.getNumberOfGuests() + " guest" + (r.getNumberOfGuests() == 1 ? "" : "s")
                + (rm != null && r.getNumberOfGuests() > rm.getCapacity() ? " (" + (r.getNumberOfGuests() - rm.getCapacity()) + " Extra)" : ""));
        lblSummaryStatus.setText(r.getStatus().getDisplayName());

        btnGenerateInvoice.setEnabled(true);
        btnPrintInvoice.setEnabled(false);
        btnSaveAsText.setEnabled(false);

        Invoice existing = invoiceService.getInvoiceByBookingId(r.getBookingId());
        if (existing != null) {
            this.currentGeneratedInvoice = existing;
            renderInvoicePreview(existing);
            btnPrintInvoice.setEnabled(true);
            btnSaveAsText.setEnabled(true);
            lblPreviewStatus.setText("Invoice " + existing.getInvoiceId() + " (Loaded from record)");
            updateFooterFeedback("Loaded existing invoice " + existing.getInvoiceId() + " for Booking " + r.getBookingId() + ".", false);
        } else {
            renderReadyToGeneratePlaceholder(r);
            lblPreviewStatus.setText("Ready to generate");
            updateFooterFeedback("Selected Booking " + r.getBookingId() + ". Click 'Generate Invoice' to calculate bill.", false);
        }
    }

    private void clearSelectedReservation() {
        this.currentSelectedReservation = null;
        this.currentGeneratedInvoice = null;

        lblSummaryBookingId.setText("None (Select a booking)");
        lblSummaryGuest.setText("-");
        lblSummaryRoom.setText("-");
        lblSummaryDates.setText("-");
        lblSummaryNights.setText("-");
        lblSummaryGuests.setText("-");
        lblSummaryStatus.setText("-");

        btnGenerateInvoice.setEnabled(false);
        btnPrintInvoice.setEnabled(false);
        btnSaveAsText.setEnabled(false);

        renderInitialPlaceholder();
        lblPreviewStatus.setText("Awaiting invoice generation");
    }

    public Invoice generateInvoiceDirectly() {
        if (currentSelectedReservation == null) {
            return null;
        }
        Invoice invoice = invoiceService.generateInvoice(currentSelectedReservation);
        this.currentGeneratedInvoice = invoice;

        renderInvoicePreview(invoice);

        btnPrintInvoice.setEnabled(true);
        btnSaveAsText.setEnabled(true);
        lblPreviewStatus.setText("Invoice " + invoice.getInvoiceId() + " (Generated)");

        String payableSummary = String.format("LKR %,.2f", invoice.getFinalPayableAmount());
        if (invoice.hasUnusedAdvanceCredit()) {
            payableSummary += " (Unused Refundable Advance: LKR " + String.format("%,.2f", invoice.getUnusedAdvanceCredit()) + ")";
        }

        updateFooterFeedback("Invoice " + invoice.getInvoiceId() + " generated successfully! Net Payable: " + payableSummary, false);
        return invoice;
    }

    public void handleGenerateInvoice() {
        if (currentSelectedReservation == null) {
            showWarning("Please select an eligible booking first.");
            return;
        }

        try {
            Invoice invoice = generateInvoiceDirectly();
            if (invoice != null && isShowing()) {
                JOptionPane.showMessageDialog(this,
                        "Invoice " + invoice.getInvoiceId() + " generated successfully!\n\n"
                                + "Net Payable Amount: LKR " + String.format("%,.2f", invoice.getFinalPayableAmount()) + "\n"
                                + (invoice.hasUnusedAdvanceCredit() ? "Unused Advance Credit: LKR " + String.format("%,.2f", invoice.getUnusedAdvanceCredit()) + " (Refundable)\n\n" : "\n")
                                + "Click 'Print Invoice' to print or 'Save as Text' to export.",
                        "Invoice Generated", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            showWarning("Failed to generate invoice: " + ex.getMessage());
        }
    }

    private void handlePrintInvoice() {
        if (currentGeneratedInvoice == null) {
            showWarning("No invoice has been generated to print.");
            return;
        }

        try {
            MessageFormat header = new MessageFormat("SmartStay Hotel — Tax Invoice " + currentGeneratedInvoice.getInvoiceId());
            MessageFormat footer = new MessageFormat("Page - {0}");
            boolean printed = previewEditorPane.print(header, footer, true, null, null, true);
            if (printed) {
                updateFooterFeedback("Invoice " + currentGeneratedInvoice.getInvoiceId() + " sent to printer successfully.", false);
            } else {
                updateFooterFeedback("Printing cancelled by user.", false);
            }
        } catch (PrinterException ex) {
            showWarning("Printing error: " + ex.getMessage());
        }
    }

    private void handleSaveAsText() {
        if (currentGeneratedInvoice == null) {
            showWarning("No invoice has been generated to export.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Invoice as Text");
        fileChooser.setSelectedFile(new File(currentGeneratedInvoice.getInvoiceId() + ".txt"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

        int choice = fileChooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
            }

            try (FileWriter writer = new FileWriter(selectedFile)) {
                writer.write(buildInvoicePlainText(currentGeneratedInvoice));
                JOptionPane.showMessageDialog(this,
                        "Invoice saved successfully to:\n" + selectedFile.getAbsolutePath(),
                        "Save Successful", JOptionPane.INFORMATION_MESSAGE);
                updateFooterFeedback("Invoice saved to: " + selectedFile.getName(), false);
            } catch (IOException ex) {
                showWarning("Error saving file: " + ex.getMessage());
            }
        }
    }

    private void handleReset() {
        txtSearchBookingId.setText("");
        if (cmbEligibleBookings.getItemCount() > 0) {
            cmbEligibleBookings.setSelectedIndex(0);
        }
        clearSelectedReservation();
        updateFooterFeedback("Billing form reset. Ready for new query.", false);
    }

    private void renderInitialPlaceholder() {
        previewEditorPane.setText(
                "<html><body style='font-family: Segoe UI, sans-serif; background-color: #f8fafc; padding: 40px; text-align: center; color: #64748b;'>"
                        + "<div style='border: 2px dashed #cbd5e1; border-radius: 8px; padding: 50px 20px; background-color: #ffffff;'>"
                        + "<h2 style='color: #1e3a8a; margin-top: 0;'>Official Hotel Tax Invoice</h2>"
                        + "<p style='font-size: 14px;'>No reservation currently selected for billing.</p>"
                        + "<p style='font-size: 12px; color: #94a3b8;'>Search by Booking ID or pick from the eligible billable bookings list on the left to review stay charges, discounts, taxes, and generate the final bill.</p>"
                        + "</div>"
                        + "</body></html>"
        );
        previewEditorPane.setCaretPosition(0);
    }

    private void renderReadyToGeneratePlaceholder(Reservation r) {
        previewEditorPane.setText(
                "<html><body style='font-family: Segoe UI, sans-serif; background-color: #f8fafc; padding: 30px; text-align: center; color: #475569;'>"
                        + "<div style='border: 1px solid #cbd5e1; border-radius: 8px; padding: 35px 25px; background-color: #ffffff;'>"
                        + "<h3 style='color: #1e3a8a; margin-top: 0;'>Booking " + r.getBookingId() + " Selected</h3>"
                        + "<p>Guest: <b>" + (r.getGuest() != null ? r.getGuest().getFullName() : "-") + "</b></p>"
                        + "<p>Stay Duration: <b>" + r.calculateNumberOfNights() + " nights</b> (" + r.getCheckInDate() + " to " + r.getCheckOutDate() + ")</p>"
                        + "<p style='color: #15803d;'><b>Recorded Advance Deposit: LKR " + String.format("%,.2f", r.getBookingAdvance()) + "</b></p>"
                        + "<br>"
                        + "<p style='font-size: 13px; color: #1e3a8a;'><b>Click the 'Generate Invoice' button on the left to compute all charges, discounts, taxes, and final payable amounts.</b></p>"
                        + "</div>"
                        + "</body></html>"
        );
        previewEditorPane.setCaretPosition(0);
    }

    private void renderInvoicePreview(Invoice invoice) {
        Reservation r = invoice.getReservation();
        Guest g = r.getGuest();
        Room rm = r.getRoom();
        long nights = r.calculateNumberOfNights();
        int capacity = rm != null ? rm.getCapacity() : 2;
        int guests = r.getNumberOfGuests();
        int extraGuests = Math.max(0, guests - capacity);
        double extraRate = rm != null ? rm.getRoomType().getExtraGuestCharge() : 0.0;
        double taxRate = rm != null ? rm.getRoomType().getTaxRate() : 0.0;

        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
                .append("body { font-family: 'Segoe UI', Arial, sans-serif; font-size: 12px; color: #1e293b; margin: 15px; }")
                .append(".header-table { width: 100%; border-bottom: 2px solid #1e3a8a; margin-bottom: 12px; padding-bottom: 8px; }")
                .append(".hotel-title { font-size: 20px; font-weight: bold; color: #1e3a8a; }")
                .append(".hotel-sub { font-size: 11px; color: #64748b; }")
                .append(".invoice-ref { font-size: 14px; font-weight: bold; color: #1e3a8a; text-align: right; }")
                .append(".section-title { font-size: 12px; font-weight: bold; color: #1e3a8a; background-color: #f1f5f9; padding: 4px 8px; border-left: 3px solid #1e3a8a; margin-top: 10px; margin-bottom: 6px; }")
                .append(".grid-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }")
                .append(".grid-table td { padding: 4px 8px; font-size: 11px; }")
                .append(".label { font-weight: bold; color: #475569; width: 22%; }")
                .append(".val { color: #0f172a; width: 28%; }")
                .append(".billing-table { width: 100%; border-collapse: collapse; margin-top: 8px; margin-bottom: 12px; }")
                .append(".billing-table th { background-color: #1e3a8a; color: #ffffff; padding: 6px 10px; font-size: 11px; text-align: left; }")
                .append(".billing-table td { padding: 6px 10px; border-bottom: 1px solid #e2e8f0; font-size: 11px; }")
                .append(".amount-col { text-align: right; font-family: 'Courier New', monospace; font-size: 12px; }")
                .append(".total-box { background-color: #f8fafc; border: 2px solid #1e3a8a; border-radius: 6px; padding: 10px 14px; margin-top: 8px; }")
                .append(".payable-highlight { font-size: 18px; font-weight: bold; color: #1e3a8a; }")
                .append(".credit-highlight { font-size: 14px; font-weight: bold; color: #d97706; }")
                .append(".footer-note { font-size: 10px; color: #94a3b8; text-align: center; margin-top: 16px; border-top: 1px solid #e2e8f0; padding-top: 8px; }")
                .append("</style></head><body>");

        html.append("<table class='header-table'><tr>")
                .append("<td>")
                .append("<span class='hotel-title'>SmartStay Hotel &amp; Suites</span><br>")
                .append("<span class='hotel-sub'>Official Guest Tax Invoice &bull; CI6115 Programming III Hotel Management</span><br>")
                .append("<span class='hotel-sub'>Colombo, Sri Lanka &bull; Tel: +94 11 234 5678 &bull; info@smartstay.lk</span>")
                .append("</td>")
                .append("<td style='text-align: right; vertical-align: top;'>")
                .append("<span class='invoice-ref'>INVOICE: ").append(invoice.getInvoiceId()).append("</span><br>")
                .append("<span class='hotel-sub'><b>Issue Date:</b> ").append(LocalDate.now()).append("</span><br>")
                .append("<span class='hotel-sub'><b>Booking Ref:</b> ").append(r.getBookingId()).append("</span><br>")
                .append("<span class='hotel-sub'><b>Status:</b> ").append(r.getStatus().getDisplayName()).append("</span>")
                .append("</td></tr></table>");

        html.append("<div class='section-title'>1. Guest Information</div>")
                .append("<table class='grid-table'>")
                .append("<tr><td class='label'>Guest Name:</td><td class='val'><b>").append(g != null ? g.getFullName() : "-").append("</b></td>")
                .append("<td class='label'>Guest ID:</td><td class='val'>").append(g != null ? g.getGuestId() : "-").append("</td></tr>")
                .append("<tr><td class='label'>NIC / Passport:</td><td class='val'>").append(g != null ? g.getNicOrPassport() : "-").append("</td>")
                .append("<td class='label'>Contact Phone:</td><td class='val'>").append(g != null ? g.getContactNumber() : "-").append("</td></tr>")
                .append("<tr><td class='label'>Email Address:</td><td class='val' colspan='3'>").append(g != null ? g.getEmail() : "-").append("</td></tr>")
                .append("</table>");

        html.append("<div class='section-title'>2. Accommodation &amp; Stay Breakdown</div>")
                .append("<table class='grid-table'>")
                .append("<tr><td class='label'>Room ID:</td><td class='val'><b>").append(rm != null ? rm.getRoomId() : "-").append("</b></td>")
                .append("<td class='label'>Room Type:</td><td class='val'>").append(rm != null ? rm.getRoomType().getDisplayName() : "-").append("</td></tr>")
                .append("<tr><td class='label'>Bed Configuration:</td><td class='val'>").append(rm != null ? rm.getBedType().getDisplayName() : "-").append("</td>")
                .append("<td class='label'>Daily Room Rate:</td><td class='val'><b>LKR ").append(rm != null ? String.format("%,.2f", rm.getDailyRate()) : "-").append("</b></td></tr>")
                .append("<tr><td class='label'>Check-in Date:</td><td class='val'>").append(r.getCheckInDate()).append(" (14:00)</td>")
                .append("<td class='label'>Check-out Date:</td><td class='val'>").append(r.getCheckOutDate()).append(" (12:00)</td></tr>")
                .append("<tr><td class='label'>Stay Duration:</td><td class='val'><b>").append(nights).append(" Night").append(nights == 1 ? "" : "s").append("</b></td>")
                .append("<td class='label'>Number of Guests:</td><td class='val'>").append(guests).append(" Guest").append(guests == 1 ? "" : "s")
                .append(" (Max Capacity: ").append(capacity).append(")</td></tr>")
                .append("</table>");

        html.append("<div class='section-title'>3. Itemized Financial Breakdown</div>")
                .append("<table class='billing-table'>")
                .append("<tr><th style='width: 70%;'>Item Description</th><th style='width: 30%; text-align: right;'>Amount (LKR)</th></tr>");

        html.append("<tr><td><b>Base Room Charge</b><br>")
                .append("<span style='color: #64748b; font-size: 10px;'>")
                .append(rm != null ? String.format("LKR %,.2f", rm.getDailyRate()) : "-")
                .append(" &times; ").append(nights).append(" night").append(nights == 1 ? "" : "s").append("</span></td>")
                .append("<td class='amount-col'>").append(String.format("%,.2f", invoice.getBaseRoomCharge())).append("</td></tr>");

        html.append("<tr><td><b>Extra Guest Surcharge</b><br>")
                .append("<span style='color: #64748b; font-size: 10px;'>")
                .append(extraGuests > 0 ? extraGuests + " extra guest(s) &times; LKR " + String.format("%,.2f", extraRate) + " &times; " + nights + " nights" : "No extra guests (Within room capacity of " + capacity + ")")
                .append("</span></td>")
                .append("<td class='amount-col'>").append(String.format("%,.2f", invoice.getExtraGuestCharge())).append("</td></tr>");

        if (invoice.getDiscountAmount() > 0) {
            html.append("<tr style='background-color: #f0fdf4;'><td><b style='color: #15803d;'>Long Stay Discount (12%)</b><br>")
                    .append("<span style='color: #166534; font-size: 10px;'>12% deducted from base room charge for stays of 5 nights or more</span></td>")
                    .append("<td class='amount-col' style='color: #15803d;'>-").append(String.format("%,.2f", invoice.getDiscountAmount())).append("</td></tr>");
        } else {
            html.append("<tr><td><b>Long Stay Discount</b><br>")
                    .append("<span style='color: #64748b; font-size: 10px;'>Eligible on stays of 5+ nights (Current stay: ").append(nights).append(" nights)</span></td>")
                    .append("<td class='amount-col'>0.00</td></tr>");
        }

        html.append("<tr style='background-color: #f8fafc;'><td><b>Taxable Subtotal</b><br>")
                .append("<span style='color: #64748b; font-size: 10px;'>Base Charge + Extra Guest Surcharge &minus; Discount</span></td>")
                .append("<td class='amount-col'><b>").append(String.format("%,.2f", invoice.getTaxableSubtotal())).append("</b></td></tr>");

        html.append("<tr><td><b>Government Tax (").append(String.format("%.0f%%", taxRate * 100)).append(")</b><br>")
                .append("<span style='color: #64748b; font-size: 10px;'>Tax rate for ").append(rm != null ? rm.getRoomType().getDisplayName() : "").append(" applied on taxable subtotal</span></td>")
                .append("<td class='amount-col'>").append(String.format("%,.2f", invoice.getTaxAmount())).append("</td></tr>");

        html.append("<tr style='background-color: #f1f5f9; font-weight: bold;'><td>Total Stay Bill</td>")
                .append("<td class='amount-col'>LKR ").append(String.format("%,.2f", invoice.getBillTotal())).append("</td></tr>");

        html.append("<tr style='background-color: #f0fdf4;'><td><b style='color: #15803d;'>Booking Advance Deducted</b><br>")
                .append("<span style='color: #166534; font-size: 10px;'>Recorded booking deposit credited towards bill</span></td>")
                .append("<td class='amount-col' style='color: #15803d;'>-").append(String.format("%,.2f", invoice.getBookingAdvanceDeduction())).append("</td></tr>");

        html.append("</table>");

        html.append("<div class='total-box'><table style='width: 100%; border-collapse: collapse;'>")
                .append("<tr><td><span style='font-size: 13px; font-weight: bold; color: #475569;'>FINAL NET PAYABLE:</span></td>")
                .append("<td style='text-align: right;'><span class='payable-highlight'>LKR ").append(String.format("%,.2f", invoice.getFinalPayableAmount())).append("</span></td></tr>");

        if (invoice.hasUnusedAdvanceCredit()) {
            html.append("<tr><td colspan='2' style='padding-top: 8px; border-top: 1px dashed #cbd5e1;'>")
                    .append("<div style='background-color: #fef3c7; border: 1px solid #f59e0b; border-radius: 4px; padding: 6px 10px; margin-top: 4px;'>")
                    .append("<span class='credit-highlight'>Refundable Unused Advance Credit: LKR ").append(String.format("%,.2f", invoice.getUnusedAdvanceCredit())).append("</span><br>")
                    .append("<span style='font-size: 11px; color: #92400e;'>The booking advance of LKR 10,000 exceeds the total stay charges (LKR ").append(String.format("%,.2f", invoice.getBillTotal())).append("). Balance is refundable to the guest.</span>")
                    .append("</div></td></tr>");
        }

        html.append("</table></div>");

        html.append("<div class='footer-note'>")
                .append("Thank you for choosing SmartStay Hotel &amp; Suites. We hope you enjoyed your stay!<br>")
                .append("<i>This document is an official computer-generated tax invoice for CI6115 coursework simulation.</i>")
                .append("</div>");

        html.append("</body></html>");

        previewEditorPane.setText(html.toString());
        previewEditorPane.setCaretPosition(0);
    }

    private String buildInvoicePlainText(Invoice invoice) {
        Reservation r = invoice.getReservation();
        Guest g = r.getGuest();
        Room rm = r.getRoom();
        long nights = r.calculateNumberOfNights();
        int capacity = rm != null ? rm.getCapacity() : 2;
        int guests = r.getNumberOfGuests();
        int extraGuests = Math.max(0, guests - capacity);
        double taxRate = rm != null ? rm.getRoomType().getTaxRate() : 0.0;

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("                       SMARTSTAY HOTEL & SUITES                         \n");
        sb.append("                      OFFICIAL GUEST TAX INVOICE                        \n");
        sb.append("========================================================================\n");
        sb.append(String.format("Invoice ID    : %s\n", invoice.getInvoiceId()));
        sb.append(String.format("Booking ID    : %s\n", r.getBookingId()));
        sb.append(String.format("Issue Date    : %s\n", LocalDate.now()));
        sb.append(String.format("Booking Status: %s\n", r.getStatus().getDisplayName()));
        sb.append("------------------------------------------------------------------------\n");
        sb.append("GUEST DETAILS:\n");
        sb.append(String.format("  Full Name   : %s (ID: %s)\n", g != null ? g.getFullName() : "-", g != null ? g.getGuestId() : "-"));
        sb.append(String.format("  NIC/Passport: %s\n", g != null ? g.getNicOrPassport() : "-"));
        sb.append(String.format("  Contact No  : %s\n", g != null ? g.getContactNumber() : "-"));
        sb.append(String.format("  Email       : %s\n", g != null ? g.getEmail() : "-"));
        sb.append("------------------------------------------------------------------------\n");
        sb.append("ROOM & STAY DETAILS:\n");
        sb.append(String.format("  Room ID     : %s\n", rm != null ? rm.getRoomId() : "-"));
        sb.append(String.format("  Room Type   : %s\n", rm != null ? rm.getRoomType().getDisplayName() : "-"));
        sb.append(String.format("  Bed Type    : %s\n", rm != null ? rm.getBedType().getDisplayName() : "-"));
        sb.append(String.format("  Daily Rate  : LKR %,.2f\n", rm != null ? rm.getDailyRate() : 0.0));
        sb.append(String.format("  Check-in    : %s\n", r.getCheckInDate()));
        sb.append(String.format("  Check-out   : %s\n", r.getCheckOutDate()));
        sb.append(String.format("  Stay Nights : %d night%s\n", nights, nights == 1 ? "" : "s"));
        sb.append(String.format("  Guests      : %d guest%s (Room Capacity: %d)\n", guests, guests == 1 ? "" : "s", capacity));
        sb.append("------------------------------------------------------------------------\n");
        sb.append("FINANCIAL BREAKDOWN (LKR):\n");
        sb.append(String.format("  Base Room Charge (%d nights @ LKR %,.2f) : LKR %12s\n",
                nights, rm != null ? rm.getDailyRate() : 0.0, String.format("%,.2f", invoice.getBaseRoomCharge())));
        sb.append(String.format("  Extra Guest Charges (%d extra @ LKR %,.2f/night) : LKR %12s\n",
                extraGuests, rm != null ? rm.getRoomType().getExtraGuestCharge() : 0.0, String.format("%,.2f", invoice.getExtraGuestCharge())));
        sb.append(String.format("  Long Stay Discount (12%% for >=5 nights)        : -LKR %11s\n",
                String.format("%,.2f", invoice.getDiscountAmount())));
        sb.append(String.format("  Taxable Subtotal                               : LKR %12s\n",
                String.format("%,.2f", invoice.getTaxableSubtotal())));
        sb.append(String.format("  Government Tax (%.0f%%)                           : LKR %12s\n",
                taxRate * 100, String.format("%,.2f", invoice.getTaxAmount())));
        sb.append(String.format("  Total Bill Amount                              : LKR %12s\n",
                String.format("%,.2f", invoice.getBillTotal())));
        sb.append(String.format("  Booking Advance Deducted                       : -LKR %11s\n",
                String.format("%,.2f", invoice.getBookingAdvanceDeduction())));
        sb.append("========================================================================\n");
        sb.append(String.format("  FINAL NET PAYABLE AMOUNT                       : LKR %12s\n",
                String.format("%,.2f", invoice.getFinalPayableAmount())));
        if (invoice.hasUnusedAdvanceCredit()) {
            sb.append(String.format("  REFUNDABLE UNUSED ADVANCE CREDIT               : LKR %12s\n",
                    String.format("%,.2f", invoice.getUnusedAdvanceCredit())));
            sb.append("  (Advance deposit exceeded total stay charges; excess is refundable)\n");
        }
        sb.append("========================================================================\n");
        sb.append("Thank you for staying at SmartStay Hotel & Suites!\n");
        return sb.toString();
    }

    private void updateFooterFeedback(String message, boolean isError) {
        lblFooterFeedback.setText(message);
        lblFooterFeedback.setForeground(isError ? new Color(185, 28, 28) : UIConstants.COLOR_TEXT_MUTED);
    }

    private void showWarning(String message) {
        updateFooterFeedback(message, true);
        if (isShowing()) {
            JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BODY_BOLD);
        label.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BODY);
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

    public JTextField getTxtSearchBookingId() {
        return txtSearchBookingId;
    }

    public JButton getBtnSearchBooking() {
        return btnSearchBooking;
    }

    public JComboBox<String> getCmbEligibleBookings() {
        return cmbEligibleBookings;
    }

    public JButton getBtnGenerateInvoice() {
        return btnGenerateInvoice;
    }

    public JButton getBtnPrintInvoice() {
        return btnPrintInvoice;
    }

    public JButton getBtnSaveAsText() {
        return btnSaveAsText;
    }

    public JEditorPane getPreviewEditorPane() {
        return previewEditorPane;
    }

    public Reservation getCurrentSelectedReservation() {
        return currentSelectedReservation;
    }

    public Invoice getCurrentGeneratedInvoice() {
        return currentGeneratedInvoice;
    }

    public void selectBookingByIdForTesting(String bookingId) {
        Reservation r = reservationService.searchByBookingId(bookingId);
        if (r != null) {
            selectReservation(r);
        }
    }
}
