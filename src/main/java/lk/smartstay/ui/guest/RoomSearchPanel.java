package lk.smartstay.ui.guest;

import lk.smartstay.util.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Placeholder panel for searching and filtering available rooms by room type and date range.
 */
public class RoomSearchPanel extends JPanel {

    public RoomSearchPanel() {
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

        JLabel titleLabel = new JLabel("Room Search & Availability");
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel moduleLabel = new JLabel("Guest Services Module");
        moduleLabel.setFont(UIConstants.FONT_BODY_BOLD);
        moduleLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        JLabel descLabel = new JLabel("<html>This module enables guests to query available rooms according to preferred criteria: "
                + "room type (Single, Double, Suite, Deluxe), bed specifications, desired check-in/check-out dates, "
                + "and guest capacity.<br><br>"
                + "<em>Note: Search algorithms, date-overlap filtering, and availability verification will be implemented in subsequent phases.</em></html>");
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
