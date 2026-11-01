package lk.smartstay.ui.guest;

import lk.smartstay.util.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Placeholder panel for making room reservations, tracking booking statuses,
 * looking up bookings by ID or guest name, and canceling eligible bookings.
 */
public class ReservationPanel extends JPanel {

    public ReservationPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JPanel contentCard = new JPanel();
        contentCard.setLayout(new BoxLayout(contentCard, BoxLayout.Y_AXIS));
        contentCard.setBackground(UIConstants.COLOR_CARD_BG);
        contentCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        JLabel titleLabel = new JLabel("Reservations Management");
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel moduleLabel = new JLabel("Guest & Booking Operations");
        moduleLabel.setFont(UIConstants.FONT_BODY_BOLD);
        moduleLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        JLabel descLabel = new JLabel("<html>This module handles complete reservation lifecycles: "
                + "creating bookings for registered guests, searching reservations by Booking ID, Guest Name, "
                + "or Check-in date, and processing reservation cancellations with status updates.<br><br>"
                + "<em>Note: Booking workflows, search algorithms, and cancellation rules will be implemented in subsequent phases.</em></html>");
        descLabel.setFont(UIConstants.FONT_BODY);
        descLabel.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        contentCard.add(titleLabel);
        contentCard.add(Box.createVerticalStrut(4));
        contentCard.add(moduleLabel);
        contentCard.add(Box.createVerticalStrut(20));
        contentCard.add(descLabel);

        add(contentCard, BorderLayout.NORTH);
    }
}
