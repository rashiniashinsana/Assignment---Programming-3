package lk.smartstay;

import lk.smartstay.repository.DatabaseManager;
import lk.smartstay.repository.GuestRepository;
import lk.smartstay.repository.ReservationRepository;
import lk.smartstay.repository.RoomRepository;
import lk.smartstay.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            System.err.println("Notice: Could not load system Look and Feel, defaulting to standard Swing theme: " + ex.getMessage());
        }

        RoomRepository roomRepository = new RoomRepository();
        GuestRepository guestRepository = new GuestRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        DatabaseManager.getInstance().initializeAndLoad(roomRepository, guestRepository, reservationRepository);

        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(roomRepository, guestRepository, reservationRepository);
            mainFrame.setVisible(true);
        });
    }
}
