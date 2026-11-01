package lk.smartstay.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;


// Maintains consistent styling, typography, spacing, and color schemes
// across all Swing panels in the application.

public final class UIConstants {

    private UIConstants() {
        // Prevent instantiation
    }
    public static final Color COLOR_PRIMARY = new Color(30, 58, 138);       
    public static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216); 
    public static final Color COLOR_SIDEBAR_BG = new Color(30, 41, 59);   
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(51, 65, 85); 
    public static final Color COLOR_SIDEBAR_HOVER = new Color(71, 85, 105); 
    public static final Color COLOR_BACKGROUND = new Color(248, 250, 252);  
    public static final Color COLOR_CARD_BG = Color.WHITE;                  
    public static final Color COLOR_CARD_BORDER = new Color(226, 232, 240); 
    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);   
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139); 
    public static final Color COLOR_TEXT_LIGHT = new Color(241, 245, 249); 

    // Typography
    public static final String FONT_FAMILY = "Segoe UI";
    public static final Font FONT_APP_TITLE = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_PAGE_TITLE = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_SECTION_TITLE = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font FONT_CARD_VALUE = new Font(FONT_FAMILY, Font.BOLD, 28);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_MUTED = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_NAV_BUTTON = new Font(FONT_FAMILY, Font.BOLD, 13);

    // Dimensions
    public static final Dimension WINDOW_SIZE = new Dimension(1200, 760);
    public static final Dimension SIDEBAR_SIZE = new Dimension(240, 760);
    public static final Dimension NAV_BUTTON_SIZE = new Dimension(216, 42);
    public static final Dimension HEADER_SIZE = new Dimension(1200, 60);
}
