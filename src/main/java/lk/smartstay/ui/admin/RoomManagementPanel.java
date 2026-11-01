package lk.smartstay.ui.admin;

import lk.smartstay.util.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Placeholder panel for administrator room management operations:
 * adding, updating, removing, and toggling room availability.
 */
public class RoomManagementPanel extends JPanel {

    public RoomManagementPanel() {
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

        JLabel titleLabel = new JLabel("Room Management");
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel moduleLabel = new JLabel("Administrator Module");
        moduleLabel.setFont(UIConstants.FONT_BODY_BOLD);
        moduleLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        JLabel descLabel = new JLabel("<html>This module will allow hotel administrators to manage the complete room inventory: "
                + "add new room records, update room details (room type, bed configuration, base price), "
                + "remove room records, and toggle room availability status.<br><br>"
                + "<em>Note: Full room management operations and data persistence will be implemented in subsequent phases.</em></html>");
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
