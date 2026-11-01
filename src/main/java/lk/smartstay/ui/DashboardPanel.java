package lk.smartstay.ui;

import lk.smartstay.util.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * Dashboard panel displaying the welcome banner, system summary metrics,
 * and high-level status for the SmartStay Hotel Booking System.
 */
public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UIConstants.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Top Section: Welcome Banner
        add(createWelcomeBanner(), BorderLayout.NORTH);

        // Center Section: Summary Cards & Status Info
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);

        centerContainer.add(createSummaryCardsPanel());
        centerContainer.add(Box.createVerticalStrut(25));
        centerContainer.add(createSystemOverviewPanel());

        add(centerContainer, BorderLayout.CENTER);
    }

    /**
     * Builds the welcome banner containing system name and description.
     */
    private JPanel createWelcomeBanner() {
        JPanel banner = new JPanel();
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBackground(UIConstants.COLOR_CARD_BG);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        JLabel titleLabel = new JLabel("SmartStay Hotel Booking System");
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel subtitleLabel = new JLabel("A medium-sized hotel chain in Sri Lanka serving local and international guests.");
        subtitleLabel.setFont(UIConstants.FONT_BODY);
        subtitleLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        banner.add(titleLabel);
        banner.add(Box.createVerticalStrut(6));
        banner.add(subtitleLabel);

        return banner;
    }

    /**
     * Builds the 4 summary metric cards showing placeholder counts.
     */
    private JPanel createSummaryCardsPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(1, 4, 18, 0));
        gridPanel.setOpaque(false);
        gridPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 120));

        gridPanel.add(createMetricCard("Total Rooms", "0", "Configured hotel rooms"));
        gridPanel.add(createMetricCard("Available Rooms", "0", "Rooms ready for booking"));
        gridPanel.add(createMetricCard("Reservations", "0", "Active guest bookings"));
        gridPanel.add(createMetricCard("Registered Guests", "0", "Customer database profiles"));

        return gridPanel;
    }

    /**
     * Helper to create an individual metric card.
     */
    private JPanel createMetricCard(String title, String value, String note) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIConstants.COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIConstants.FONT_BODY_BOLD);
        titleLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UIConstants.FONT_CARD_VALUE);
        valueLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel noteLabel = new JLabel(note);
        noteLabel.setFont(UIConstants.FONT_MUTED);
        noteLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(noteLabel);

        return card;
    }

    /**
     * Builds an informative system overview panel describing the coursework architecture.
     */
    private JPanel createSystemOverviewPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIConstants.COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        JLabel sectionTitle = new JLabel("System Overview & Navigation Guide");
        sectionTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        sectionTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        JLabel descLabel = new JLabel("<html>SmartStay provides dedicated interfaces for both Administrators and Guests.<br>"
                + "Use the left navigation sidebar to switch between modules: Room Management, Guest Registration, "
                + "Room Search, Reservations, and Invoices. Data persistence is managed in-memory as per coursework specifications.</html>");
        descLabel.setFont(UIConstants.FONT_BODY);
        descLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        panel.add(sectionTitle);
        panel.add(Box.createVerticalStrut(10));
        panel.add(descLabel);

        return panel;
    }
}
