package lk.smartstay;

import lk.smartstay.enums.BedType;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Guest;
import lk.smartstay.model.Invoice;
import lk.smartstay.model.Reservation;
import lk.smartstay.model.Room;
import lk.smartstay.service.HotelBookingManager;
import lk.smartstay.ui.MainFrame;

import javax.swing.SwingUtilities;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class SmartStayApp {

    private final HotelBookingManager manager;
    private final Scanner scanner;

    public SmartStayApp() {
        this.manager = new HotelBookingManager();
        this.manager.initializeDefaultData();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        SmartStayApp app = new SmartStayApp();
        app.run();
    }

    public void run() {
        System.out.println("================================================================================");
        System.out.println("            WELCOME TO SMARTSTAY HOTEL BOOKING & MANAGEMENT SYSTEM              ");
        System.out.println("                                                                                ");
        System.out.println("================================================================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Enter your choice (0-8): ").trim();
            System.out.println();

            switch (choice) {
                case "1" -> handleRoomManagement();
                case "2" -> handleGuestManagement();
                case "3" -> handleSearchAvailableRooms();
                case "4" -> handleMakeReservation();
                case "5" -> handleSearchReservations();
                case "6" -> handleCancelReservation();
                case "7" -> handleGenerateInvoice();
                case "8" -> handleLaunchGUI();
                case "0" -> {
                    System.out.println("Thank you for using SmartStay Hotel Booking System. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please enter a valid number from 0 to 8.\n");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("                                MAIN MENU                                       ");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  [1] Room Management (View / Add / Update / Delete Rooms)");
        System.out.println("  [2] Guest Customer Management (Register / Search / View Guests)");
        System.out.println("  [3] Search Room Availability (by Room Type & Date Range)");
        System.out.println("  [4] Make a New Reservation (2-Day Advance Window, LKR 10,000 Advance)");
        System.out.println("  [5] Search & View Reservations (by ID, Guest Name, or Date)");
        System.out.println("  [6] Cancel Reservation (24-Hour Check-in Policy Enforcement)");
        System.out.println("  [7] Generate & View Itemized Invoice (5-Stage Billing Breakdown)");
        System.out.println("  [8] Launch Graphical User Interface (Java Swing Desktop GUI)");
        System.out.println("  [0] Exit Application");
        System.out.println("--------------------------------------------------------------------------------");
    }

    private void handleRoomManagement() {
        System.out.println(">>> ROOM MANAGEMENT");
        System.out.println("1. View All Rooms");
        System.out.println("2. Add New Room");
        System.out.println("3. Update Room Status (AVAILABLE / UNDER_MAINTENANCE)");
        System.out.println("4. Delete Room (Safeguarded by Booking History)");
        System.out.println("0. Back to Main Menu");

        String sub = prompt("Select an option: ").trim();
        switch (sub) {
            case "1" -> displayAllRooms();
            case "2" -> addNewRoom();
            case "3" -> updateRoomStatus();
            case "4" -> deleteRoom();
            case "0" -> {
            }
            default -> System.out.println("Invalid selection.");
        }
    }

    private void displayAllRooms() {
        List<Room> rooms = manager.getAllRooms();
        System.out.println("\n--- Current Hotel Room Inventory ---");
        System.out.printf("%-8s %-16s %-12s %-10s %-18s %-14s %-8s%n",
                "Room ID", "Room Type", "Bed Type", "Capacity", "Status", "Daily Rate", "Tax");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (Room r : rooms) {
            System.out.printf("%-8s %-16s %-12s %-10d %-18s LKR %-10.2f %-7.0f%%%n",
                    r.getRoomId(),
                    r.getRoomType().getDisplayName(),
                    r.getBedType().getDisplayName(),
                    r.getCapacity(),
                    r.getStatus().getDisplayName(),
                    r.getDailyRate(),
                    r.getTaxRate() * 100);
        }
        System.out.println("Total configured rooms: " + rooms.size() + "\n");
    }

    private void addNewRoom() {
        System.out.println("\n--- Add New Room ---");
        String roomId = prompt("Enter Room ID (e.g. R501): ").trim();
        if (roomId.isEmpty()) {
            System.out.println("Room ID cannot be blank.");
            return;
        }

        RoomType type = selectRoomType();
        if (type == null)
            return;

        BedType bed = selectBedType();
        if (bed == null)
            return;

        int capacity;
        try {
            capacity = Integer.parseInt(prompt("Enter Room Capacity (positive integer): ").trim());
            if (capacity <= 0) {
                System.out.println("Capacity must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for capacity.");
            return;
        }

        boolean added = manager.addRoom(roomId, type, bed, capacity);
        if (added) {
            System.out.println("Success: Room " + roomId + " added successfully.");
        } else {
            System.out.println("Failed: A room with ID " + roomId + " already exists.");
        }
    }

    private void updateRoomStatus() {
        System.out.println("\n--- Update Room Status ---");
        String roomId = prompt("Enter Room ID: ").trim();
        Room room = manager.findRoomById(roomId);
        if (room == null) {
            System.out.println("Room ID '" + roomId + "' not found.");
            return;
        }

        System.out.println("Current status of " + roomId + ": " + room.getStatus().getDisplayName());
        System.out.println("1. AVAILABLE");
        System.out.println("2. UNDER_MAINTENANCE");
        String s = prompt("Select new status (1 or 2): ").trim();
        RoomStatus newStatus = switch (s) {
            case "1" -> RoomStatus.AVAILABLE;
            case "2" -> RoomStatus.UNDER_MAINTENANCE;
            default -> null;
        };

        if (newStatus == null) {
            System.out.println("Invalid selection.");
            return;
        }

        manager.updateRoomStatus(roomId, newStatus);
        System.out.println("Success: Room " + roomId + " status updated to " + newStatus.getDisplayName() + ".");
    }

    private void deleteRoom() {
        System.out.println("\n--- Delete Room ---");
        String roomId = prompt("Enter Room ID to delete: ").trim();
        boolean removed = manager.removeRoom(roomId);
        if (removed) {
            System.out.println("Success: Room " + roomId + " deleted from inventory.");
        } else {
            System.out.println("Failed: Room not found or deletion blocked by existing reservation history safeguard.");
        }
    }

    private void handleGuestManagement() {
        System.out.println(">>> GUEST MANAGEMENT");
        System.out.println("1. View All Registered Guests");
        System.out.println("2. Register New Guest");
        System.out.println("3. Search Guest (by Name or NIC)");
        System.out.println("0. Back to Main Menu");

        String sub = prompt("Select an option: ").trim();
        switch (sub) {
            case "1" -> displayAllGuests();
            case "2" -> registerNewGuest();
            case "3" -> searchGuests();
            case "0" -> {
            }
            default -> System.out.println("Invalid selection.");
        }
    }

    private void displayAllGuests() {
        List<Guest> guests = manager.getAllGuests();
        System.out.println("\n--- Registered Guests Directory ---");
        System.out.printf("%-10s %-16s %-22s %-16s %-24s%n", "Guest ID", "NIC / Passport", "Full Name", "Contact",
                "Email");
        System.out.println(
                "-------------------------------------------------------------------------------------------------");
        for (Guest g : guests) {
            System.out.printf("%-10s %-16s %-22s %-16s %-24s%n",
                    g.getGuestId(), g.getNicOrPassport(), g.getFullName(), g.getContactNumber(), g.getEmail());
        }
        System.out.println("Total registered guests: " + guests.size() + "\n");
    }

    private void registerNewGuest() {
        System.out.println("\n--- Register New Guest ---");
        String nic = prompt("Enter NIC or Passport number: ").trim();
        String name = prompt("Enter Guest Full Name: ").trim();
        String phone = prompt("Enter Contact Number: ").trim();
        String email = prompt("Enter Email Address: ").trim();

        try {
            boolean registered = manager.registerGuest(nic, name, phone, email);
            if (registered) {
                Guest g = manager.findGuestByNic(nic);
                System.out.println("Success: Guest registered with assigned Guest ID: " + g.getGuestId() + ".");
            } else {
                System.out.println("Failed: A guest with this NIC/Passport already exists or input is invalid.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Validation Error: " + e.getMessage());
        }
    }

    private void searchGuests() {
        System.out.println("\n--- Search Guests ---");
        String query = prompt("Enter Guest Name or NIC: ").trim();
        List<Guest> byName = manager.searchGuestsByName(query);
        Guest byNic = manager.findGuestByNic(query);

        System.out.println("\nSearch Results:");
        if (byNic != null) {
            System.out.println("Exact NIC Match: " + byNic);
        }
        if (!byName.isEmpty()) {
            System.out.println("Name Matches (" + byName.size() + "):");
            for (Guest g : byName) {
                System.out.println(" - " + g);
            }
        } else if (byNic == null) {
            System.out.println("No registered guests matched query '" + query + "'.");
        }
    }

    private void handleSearchAvailableRooms() {
        System.out.println("\n>>> SEARCH AVAILABLE ROOMS");
        RoomType type = selectRoomType();
        if (type == null)
            return;

        LocalDate checkIn = promptDate("Enter Check-in Date (YYYY-MM-DD): ");
        if (checkIn == null)
            return;

        LocalDate checkOut = promptDate("Enter Check-out Date (YYYY-MM-DD): ");
        if (checkOut == null)
            return;

        try {
            List<Room> available = manager.searchAvailableRooms(type, checkIn, checkOut);
            System.out.println(
                    "\nAvailable " + type.getDisplayName() + " rooms for " + checkIn + " to " + checkOut + ":");
            if (available.isEmpty()) {
                System.out.println("No rooms available for the requested type and dates.");
            } else {
                for (Room r : available) {
                    System.out.printf(" - Room %s: %s, Capacity %d, Daily Rate LKR %.2f, Breakfast: %s%n",
                            r.getRoomId(), r.getBedType().getDisplayName(), r.getCapacity(), r.getDailyRate(),
                            r.hasComplimentaryBreakfast() ? "Included" : "Excluded");
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Search Error: " + e.getMessage());
        }
    }

    private void handleMakeReservation() {
        System.out.println("\n>>> MAKE A NEW RESERVATION");
        System.out.println("Note: Mandatory 2-day advance booking window and LKR 10,000.00 advance deposit required.");

        String guestId = prompt("Enter Registered Guest ID (e.g. GST001): ").trim();
        Guest guest = manager.findGuestById(guestId);
        if (guest == null) {
            System.out.println("Guest ID not found. Please register the guest first.");
            return;
        }
        System.out.println("Selected Guest: " + guest.getFullName());

        String roomId = prompt("Enter Room ID to book: ").trim();
        Room room = manager.findRoomById(roomId);
        if (room == null) {
            System.out.println("Room ID not found.");
            return;
        }

        int guestsCount;
        try {
            guestsCount = Integer.parseInt(prompt("Enter Number of Guests: ").trim());
            if (guestsCount <= 0) {
                System.out.println("Number of guests must be at least 1.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid guest count number.");
            return;
        }

        LocalDate checkIn = promptDate("Enter Check-in Date (YYYY-MM-DD): ");
        if (checkIn == null)
            return;

        LocalDate checkOut = promptDate("Enter Check-out Date (YYYY-MM-DD): ");
        if (checkOut == null)
            return;

        try {
            Reservation res = manager.createReservation(guest, room, guestsCount, checkIn, checkOut);
            System.out.println("\n================================================================================");
            System.out.println("                      BOOKING CONFIRMATION SUCCESSFUL                          ");
            System.out.println("================================================================================");
            System.out.println("  Booking ID:       " + res.getBookingId());
            System.out.println(
                    "  Guest:            " + res.getGuest().getFullName() + " (" + res.getGuest().getGuestId() + ")");
            System.out.println("  Room:             " + res.getRoom().getRoomId() + " ("
                    + res.getRoom().getRoomType().getDisplayName() + ")");
            System.out.println("  Dates:            " + res.getCheckInDate() + " to " + res.getCheckOutDate() + " ("
                    + res.calculateNumberOfNights() + " nights)");
            System.out.println("  Guests Occupying: " + res.getNumberOfGuests() + " (Room Capacity: "
                    + res.getRoom().getCapacity() + ")");
            System.out.println("  Advance Recorded: LKR " + String.format("%.2f", res.getBookingAdvance())
                    + " (Refundable deposit)");
            System.out.println("  Status:           " + res.getStatus().getDisplayName());
            System.out.println("================================================================================\n");
        } catch (Exception e) {
            System.out.println("Reservation Failed: " + e.getMessage());
        }
    }

    private void handleSearchReservations() {
        System.out.println("\n>>> SEARCH RESERVATIONS");
        System.out.println("1. View All Reservations");
        System.out.println("2. Search by Booking ID (O(1) Direct Lookup)");
        System.out.println("3. Search by Guest Name");
        System.out.println("4. Search by Check-in Date");
        System.out.println("0. Back to Main Menu");

        String sub = prompt("Select option: ").trim();
        switch (sub) {
            case "1" -> {
                List<Reservation> all = manager.getAllReservations();
                System.out.println("\n--- All Recorded Reservations (" + all.size() + ") ---");
                for (Reservation r : all) {
                    System.out.println(r);
                }
            }
            case "2" -> {
                String bId = prompt("Enter Booking ID (e.g. BKG001): ").trim();
                Reservation r = manager.findReservationById(bId);
                if (r != null) {
                    System.out.println("Found: " + r);
                } else {
                    System.out.println("No reservation found with ID '" + bId + "'.");
                }
            }
            case "3" -> {
                String name = prompt("Enter Guest Name query: ").trim();
                List<Reservation> matches = manager.searchReservationsByGuestName(name);
                System.out.println("Matches found: " + matches.size());
                for (Reservation r : matches) {
                    System.out.println(" - " + r);
                }
            }
            case "4" -> {
                LocalDate date = promptDate("Enter Check-in Date (YYYY-MM-DD): ");
                if (date != null) {
                    List<Reservation> matches = manager.getReservationsByCheckInDate(date);
                    System.out.println("Reservations arriving on " + date + " (" + matches.size() + "):");
                    for (Reservation r : matches) {
                        System.out.println(" - " + r);
                    }
                }
            }
            case "0" -> {
            }
            default -> System.out.println("Invalid selection.");
        }
    }

    private void handleCancelReservation() {
        System.out.println("\n>>> CANCEL RESERVATION (24-Hour Policy)");
        String bId = prompt("Enter Booking ID to cancel: ").trim();
        Reservation res = manager.findReservationById(bId);
        if (res == null) {
            System.out.println("Reservation '" + bId + "' not found.");
            return;
        }

        System.out.println("Booking Details: " + res);
        boolean canCancel = manager.canCancelReservation(bId);
        if (!canCancel) {
            String reason = manager.getCancellationIneligibilityReason(bId);
            System.out.println("\nCancellation REJECTED by Policy Engine:");
            System.out.println("Reason: " + reason);
            return;
        }

        double refund = manager.getRefundAmount(bId);
        System.out.println("Cancellation Eligible: You are cancelling at least 24 hours before 14:00 check-in.");
        System.out.printf("Refundable Deposit Amount: LKR %.2f%n", refund);
        String confirm = prompt("Are you sure you want to cancel this booking? (yes/no): ").trim().toLowerCase();
        if ("yes".equals(confirm) || "y".equals(confirm)) {
            boolean success = manager.cancelReservation(bId);
            if (success) {
                System.out.println("Success: Reservation " + bId + " has been cancelled. Dates have been freed.");
            } else {
                System.out.println("Failed to cancel reservation.");
            }
        } else {
            System.out.println("Cancellation aborted.");
        }
    }

    private void handleGenerateInvoice() {
        System.out.println("\n>>> GENERATE & VIEW ITEMIZED INVOICE");
        String bId = prompt("Enter Confirmed Booking ID: ").trim();

        try {
            Invoice inv = manager.generateInvoice(bId);
            if (inv == null) {
                System.out.println("Reservation not found for Booking ID: " + bId);
                return;
            }

            Reservation r = inv.getReservation();
            System.out.println("\n================================================================================");
            System.out.println("                           SMARTSTAY HOTEL INVOICE                              ");
            System.out.println("================================================================================");
            System.out.printf("  Invoice ID:               %-30s%n", inv.getInvoiceId());
            System.out.printf("  Booking Reference:        %-30s%n", r.getBookingId());
            System.out.printf("  Guest Name:               %-30s%n", r.getGuest().getFullName());
            System.out.printf("  NIC / Passport:           %-30s%n", r.getGuest().getNicOrPassport());
            System.out.printf("  Contact / Email:          %s | %s%n", r.getGuest().getContactNumber(),
                    r.getGuest().getEmail());
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("  Room Number:              %-30s%n", r.getRoom().getRoomId());
            System.out.printf("  Room Package:             %-30s%n", r.getRoom().getRoomType().getDisplayName());
            System.out.printf("  Bedding Configuration:    %-30s%n", r.getRoom().getBedType().getDisplayName());
            System.out.printf("  Daily Room Rate:          LKR %,.2f per night%n", r.getRoom().getDailyRate());
            System.out.printf("  Stay Duration:            %s to %s (%d nights)%n", r.getCheckInDate(),
                    r.getCheckOutDate(), r.calculateNumberOfNights());
            System.out.printf("  Total Occupants:          %d guests (Standard Capacity: %d)%n", r.getNumberOfGuests(),
                    r.getRoom().getCapacity());
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("  1. Base Room Charge:      LKR %,12.2f%n", inv.getBaseRoomCharge());
            System.out.printf("  2. Extra Guest Surcharge: LKR %,12.2f%n", inv.getExtraGuestCharge());
            System.out.printf("  3. Long-Stay Discount:   -LKR %,12.2f (%s)%n", inv.getDiscountAmount(),
                    r.qualifiesForLongStayDiscount() ? "12% Applied for >=5 nights" : "0.00 - Stays < 5 nights");
            System.out.printf("     Taxable Subtotal:      LKR %,12.2f%n", inv.getTaxableSubtotal());
            System.out.printf("  4. Government Tax (%2.0f%%):  LKR %,12.2f%n", r.getRoom().getTaxRate() * 100,
                    inv.getTaxAmount());
            System.out.printf("     Gross Bill Total:      LKR %,12.2f%n", inv.getBillTotal());
            System.out.printf("  5. Advance Deducted:     -LKR %,12.2f (Booking Deposit)%n",
                    inv.getBookingAdvanceDeduction());
            System.out.println("================================================================================");
            System.out.printf("  FINAL NET PAYABLE:        LKR %,12.2f%n", inv.getFinalPayableAmount());
            if (inv.hasUnusedAdvanceCredit()) {
                System.out.printf("  UNUSED ADVANCE CREDIT:    LKR %,12.2f (Refund Due to Guest)%n",
                        inv.getUnusedAdvanceCredit());
            }
            System.out.println("================================================================================\n");

        } catch (IllegalStateException e) {
            System.out.println("Billing Notice: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Invoice Error: " + e.getMessage());
        }
    }

    private void handleLaunchGUI() {
        System.out.println("Launching SmartStay Swing Graphical User Interface (GUI)...");
        try {
            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            });
            System.out.println("GUI launched successfully in desktop window.");
        } catch (Exception e) {
            System.out.println("Notice: Could not open graphical display: " + e.getMessage());
        }
    }

    private RoomType selectRoomType() {
        System.out.println("Select Room Type:");
        System.out.println("1. STANDARD (LKR 6,000 / night, 10% Tax)");
        System.out.println("2. DELUXE (LKR 9,000 / night, Breakfast Incl, 12% Tax)");
        System.out.println("3. SUITE (LKR 14,000 / night, Breakfast Incl, 15% Tax)");
        System.out.println("4. FAMILY_SUITE (LKR 18,000 / night, Breakfast Incl, 15% Tax)");
        String choice = prompt("Enter type (1-4): ").trim();
        return switch (choice) {
            case "1" -> RoomType.STANDARD;
            case "2" -> RoomType.DELUXE;
            case "3" -> RoomType.SUITE;
            case "4" -> RoomType.FAMILY_SUITE;
            default -> {
                System.out.println("Invalid Room Type.");
                yield null;
            }
        };
    }

    private BedType selectBedType() {
        System.out.println("Select Bed Type:");
        System.out.println("1. SINGLE");
        System.out.println("2. DOUBLE");
        System.out.println("3. QUEEN");
        System.out.println("4. KING");
        String choice = prompt("Enter bed type (1-4): ").trim();
        return switch (choice) {
            case "1" -> BedType.SINGLE;
            case "2" -> BedType.DOUBLE;
            case "3" -> BedType.QUEEN;
            case "4" -> BedType.KING;
            default -> {
                System.out.println("Invalid Bed Type.");
                yield null;
            }
        };
    }

    private LocalDate promptDate(String message) {
        String input = prompt(message).trim();
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            return null;
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return "";
    }
}
