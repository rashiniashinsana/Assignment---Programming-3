package lk.smartstay;

import lk.smartstay.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Main application entry point for the SmartStay Hotel Booking System.
 * Initializes the Swing look-and-feel and launches the MainFrame on the Event Dispatch Thread (EDT).
 * Contains no business logic in accordance with single-responsibility principles.
 */
public class Main {

    public static void main(String[] args) {
        // Configure native system look and feel for consistent desktop integration
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            // Log fallback notice to standard error without crashing UI initialization
            System.err.println("Notice: Could not load system Look and Feel, defaulting to standard Swing theme: " + ex.getMessage());
        }

        // Launch UI safely on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
