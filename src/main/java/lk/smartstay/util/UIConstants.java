package lk.smartstay.util;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class UIConstants {

    private UIConstants() {
    }

    // --- Modern Hospitality Color Palette ---
    public static final Color COLOR_PRIMARY = new Color(15, 41, 66);              // Deep Hospitality Navy #0F2942
    public static final Color COLOR_PRIMARY_HOVER = new Color(26, 60, 95);        // Navy Hover
    public static final Color COLOR_ACCENT_GOLD = new Color(197, 155, 39);        // Warm Hospitality Gold #C59B27
    public static final Color COLOR_ACCENT_GOLD_HOVER = new Color(180, 138, 31);  // Gold Hover
    public static final Color COLOR_ACCENT_GOLD_BG = new Color(254, 249, 195);    // Soft Gold Tint #FEF9C3

    public static final Color COLOR_SIDEBAR_BG = new Color(15, 23, 42);           // Slate Black #0F172A
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(30, 41, 59);       // Active Tab Slate #1E293B
    public static final Color COLOR_SIDEBAR_HOVER = new Color(51, 65, 85);        // Hover Slate #334155

    public static final Color COLOR_BACKGROUND = new Color(248, 250, 252);        // Off-White Canvas #F8FAFC
    public static final Color COLOR_CARD_BG = Color.WHITE;                        // Clean White Card #FFFFFF
    public static final Color COLOR_CARD_BORDER = new Color(226, 232, 240);       // Subtle Border #E2E8F0
    public static final Color COLOR_CARD_BORDER_HOVER = new Color(203, 213, 225); // Subtle Border Hover

    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);         // Primary Slate #0F172A
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);        // Muted Gray #64748B
    public static final Color COLOR_TEXT_LIGHT = new Color(248, 250, 252);        // Light Text #F8FAFC

    // --- Status & Feedback Semantics ---
    public static final Color COLOR_SUCCESS = new Color(22, 163, 74);             // Green #16A34A
    public static final Color COLOR_SUCCESS_BG = new Color(220, 252, 231);        // Soft Green #DCFCE7
    public static final Color COLOR_SUCCESS_TEXT = new Color(22, 101, 52);        // Dark Green #166534

    public static final Color COLOR_DANGER = new Color(220, 38, 38);              // Crimson Red #DC2626
    public static final Color COLOR_DANGER_BG = new Color(254, 226, 226);         // Soft Red #FEE2E2
    public static final Color COLOR_DANGER_TEXT = new Color(153, 27, 27);         // Dark Red #991B1B

    public static final Color COLOR_WARNING = new Color(217, 119, 6);             // Amber #D97706
    public static final Color COLOR_WARNING_BG = new Color(254, 243, 199);        // Soft Amber #FEF3C7
    public static final Color COLOR_WARNING_TEXT = new Color(146, 64, 14);        // Dark Amber #92400E

    public static final Color COLOR_INFO_BG = new Color(224, 231, 255);           // Soft Blue #E0E7FF
    public static final Color COLOR_INFO_TEXT = new Color(55, 48, 163);           // Dark Blue #3730A3

    // --- Table Colors ---
    public static final Color COLOR_TABLE_HEADER_BG = new Color(15, 41, 66);      // Deep Navy #0F2942
    public static final Color COLOR_TABLE_HEADER_FG = Color.WHITE;
    public static final Color COLOR_TABLE_ALT_ROW = new Color(248, 250, 252);     // Subtle zebra row
    public static final Color COLOR_TABLE_SELECTION_BG = new Color(224, 231, 255);// Soft Indigo selection
    public static final Color COLOR_TABLE_SELECTION_FG = new Color(15, 23, 42);

    // --- Typography (Clean Segoe UI / System SansSerif) ---
    public static final String FONT_FAMILY = "Segoe UI";
    public static final Font FONT_APP_TITLE = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_PAGE_TITLE = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_SECTION_TITLE = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font FONT_CARD_VALUE = new Font(FONT_FAMILY, Font.BOLD, 28);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_MUTED = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_NAV_BUTTON = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_BADGE = new Font(FONT_FAMILY, Font.BOLD, 11);

    // --- Dimensions ---
    public static final Dimension WINDOW_SIZE = new Dimension(1220, 780);
    public static final Dimension SIDEBAR_SIZE = new Dimension(245, 780);
    public static final Dimension NAV_BUTTON_SIZE = new Dimension(220, 42);
    public static final Dimension HEADER_SIZE = new Dimension(1220, 60);

    // --- Reusable UI Component Builders ---

  
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(COLOR_CARD_BORDER);
        table.setSelectionBackground(COLOR_TABLE_SELECTION_BG);
        table.setSelectionForeground(COLOR_TABLE_SELECTION_FG);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(COLOR_TABLE_HEADER_BG);
        header.setForeground(COLOR_TABLE_HEADER_FG);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    }

 
    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(blendColor(bg, Color.WHITE, 0.15f));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

 
    public static DefaultTableCellRenderer createStatusBadgeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(FONT_BADGE);

                String text = (value != null) ? value.toString().trim() : "";
                label.setText(text);

                if (!isSelected) {
                    if (text.equalsIgnoreCase("AVAILABLE") || text.equalsIgnoreCase("CONFIRMED") || text.equalsIgnoreCase("Included")) {
                        label.setBackground(COLOR_SUCCESS_BG);
                        label.setForeground(COLOR_SUCCESS_TEXT);
                    } else if (text.equalsIgnoreCase("RESERVED") || text.equalsIgnoreCase("OCCUPIED")) {
                        label.setBackground(COLOR_INFO_BG);
                        label.setForeground(COLOR_INFO_TEXT);
                    } else if (text.equalsIgnoreCase("UNDER_MAINTENANCE") || text.equalsIgnoreCase("MAINTENANCE") || text.equalsIgnoreCase("No")) {
                        label.setBackground(COLOR_WARNING_BG);
                        label.setForeground(COLOR_WARNING_TEXT);
                    } else if (text.equalsIgnoreCase("CANCELLED")) {
                        label.setBackground(COLOR_DANGER_BG);
                        label.setForeground(COLOR_DANGER_TEXT);
                    } else if (text.equalsIgnoreCase("COMPLETED")) {
                        label.setBackground(new Color(241, 245, 249));
                        label.setForeground(COLOR_TEXT_MUTED);
                    } else {
                        label.setBackground((row % 2 == 0) ? Color.WHITE : COLOR_TABLE_ALT_ROW);
                        label.setForeground(COLOR_TEXT_PRIMARY);
                    }
                }
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                return label;
            }
        };
    }


    public static DefaultTableCellRenderer createRightAlignRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                label.setFont(FONT_BODY);
                if (!isSelected) {
                    label.setBackground((row % 2 == 0) ? Color.WHITE : COLOR_TABLE_ALT_ROW);
                    label.setForeground(COLOR_TEXT_PRIMARY);
                }
                label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 10));
                return label;
            }
        };
    }


    public static DefaultTableCellRenderer createCenterAlignRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(FONT_BODY);
                if (!isSelected) {
                    label.setBackground((row % 2 == 0) ? Color.WHITE : COLOR_TABLE_ALT_ROW);
                    label.setForeground(COLOR_TEXT_PRIMARY);
                }
                return label;
            }
        };
    }

 
    public static DefaultTableCellRenderer createLeftAlignRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setFont(FONT_BODY);
                if (!isSelected) {
                    label.setBackground((row % 2 == 0) ? Color.WHITE : COLOR_TABLE_ALT_ROW);
                    label.setForeground(COLOR_TEXT_PRIMARY);
                }
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 4));
                return label;
            }
        };
    }

    private static Color blendColor(Color c1, Color c2, float ratio) {
        float iRatio = 1.0f - ratio;
        int r = (int) (c1.getRed() * iRatio + c2.getRed() * ratio);
        int g = (int) (c1.getGreen() * iRatio + c2.getGreen() * ratio);
        int b = (int) (c1.getBlue() * iRatio + c2.getBlue() * ratio);
        return new Color(Math.min(255, Math.max(0, r)),
                Math.min(255, Math.max(0, g)),
                Math.min(255, Math.max(0, b)));
    }
}
