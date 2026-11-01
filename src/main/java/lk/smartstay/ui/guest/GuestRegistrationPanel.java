package lk.smartstay.ui.guest;

import lk.smartstay.util.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Placeholder panel for registering new hotel guests and managing customer profiles.
 */
public class GuestRegistrationPanel extends JPanel {

    public GuestRegistrationPanel() {
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

        JLabel titleLabel = new JLabel("Guest Registration");
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel moduleLabel = new JLabel("Guest Services Module");
        moduleLabel.setFont(UIConstants.FONT_BODY_BOLD);
        moduleLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        JLabel descLabel = new JLabel("<html>This module enables local and international guests to register in the SmartStay system: "
                + "capturing essential details such as full name, contact information, passport or national ID, "
                + "and guest preferences.<br><br>"
                + "<em>Note: Guest registration forms, validation rules, and customer record management will be implemented in subsequent phases.</em></html>");
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
