package lk.smartstay.ui.guest;

import lk.smartstay.util.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Placeholder panel for generating and viewing detailed reservation invoices,
 * including room charges, extra guest fees, discounts, taxes, advance deductions,
 * and final payable amounts.
 */
public class InvoicePanel extends JPanel {

    public InvoicePanel() {
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

        JLabel titleLabel = new JLabel("Invoices & Billing");
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel moduleLabel = new JLabel("Financial & Billing Module");
        moduleLabel.setFont(UIConstants.FONT_BODY_BOLD);
        moduleLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        JLabel descLabel = new JLabel("<html>This module handles billing calculations and invoice generation: "
                + "computing total nights, base room charges, extra guest fees, percentage discounts, "
                + "applicable taxes, booking advance deductions, and final payable amounts.<br><br>"
                + "<em>Note: Financial calculations and itemized invoice rendering will be implemented in subsequent phases.</em></html>");
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
