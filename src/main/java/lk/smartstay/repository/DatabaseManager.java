package lk.smartstay.repository;

import lk.smartstay.enums.BedType;
import lk.smartstay.enums.ReservationStatus;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Guest;
import lk.smartstay.model.Reservation;
import lk.smartstay.model.Room;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;

public class DatabaseManager {

    private static final String DEFAULT_DATA_DIR = "data";
    private static final String DB_FILE_NAME = "smartstay.db";
    private static final String SQL_DUMP_FILE_NAME = "smartstay_dump.sql";

    private static volatile DatabaseManager instance;

    private final Path dataDir;
    private final Path dbFile;
    private final Path sqlDumpFile;
    private boolean persistenceEnabled = true;

    private RoomRepository registeredRoomRepo;
    private GuestRepository registeredGuestRepo;
    private ReservationRepository registeredResRepo;

    public DatabaseManager() {
        this(Paths.get(DEFAULT_DATA_DIR));
    }

    public DatabaseManager(Path dataDirectory) {
        this.dataDir = dataDirectory;
        this.dbFile = dataDirectory.resolve(DB_FILE_NAME);
        this.sqlDumpFile = dataDirectory.resolve(SQL_DUMP_FILE_NAME);
        ensureDataDirectoryExists();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    public synchronized void registerRepositories(RoomRepository roomRepo, GuestRepository guestRepo,
            ReservationRepository resRepo) {
        this.registeredRoomRepo = roomRepo;
        this.registeredGuestRepo = guestRepo;
        this.registeredResRepo = resRepo;
    }

    public synchronized void autoSave() {
        if (!persistenceEnabled || registeredRoomRepo == null || registeredGuestRepo == null
                || registeredResRepo == null) {
            return;
        }
        save(registeredRoomRepo, registeredGuestRepo, registeredResRepo);
    }

    public boolean isPersistenceEnabled() {
        return persistenceEnabled;
    }

    public void setPersistenceEnabled(boolean persistenceEnabled) {
        this.persistenceEnabled = persistenceEnabled;
    }

    public Path getDbFile() {
        return dbFile;
    }

    public Path getSqlDumpFile() {
        return sqlDumpFile;
    }

    private void ensureDataDirectoryExists() {
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create database directory: " + e.getMessage());
        }
    }

    public synchronized void initializeAndLoad(RoomRepository roomRepo, GuestRepository guestRepo,
            ReservationRepository resRepo) {
        registerRepositories(roomRepo, guestRepo, resRepo);

        if (!persistenceEnabled) {
            return;
        }

        ensureDataDirectoryExists();

        if (!Files.exists(dbFile) || isFileEmpty(dbFile)) {
            seedDefaultData(roomRepo, guestRepo, resRepo);
            save(roomRepo, guestRepo, resRepo);
        } else {
            load(roomRepo, guestRepo, resRepo);
        }
    }

    private boolean isFileEmpty(Path path) {
        try {
            return Files.size(path) == 0;
        } catch (IOException e) {
            return true;
        }
    }

    public synchronized void seedDefaultData(RoomRepository roomRepo, GuestRepository guestRepo,
            ReservationRepository resRepo) {
        if (roomRepo.count() == 0) {
            roomRepo.addRoom(new Room("R101", RoomType.STANDARD, BedType.SINGLE, 1, RoomStatus.AVAILABLE));
            roomRepo.addRoom(new Room("R102", RoomType.STANDARD, BedType.DOUBLE, 2, RoomStatus.AVAILABLE));
            roomRepo.addRoom(new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE));
            roomRepo.addRoom(new Room("R202", RoomType.DELUXE, BedType.QUEEN, 2, RoomStatus.AVAILABLE));
            roomRepo.addRoom(new Room("R301", RoomType.SUITE, BedType.KING, 3, RoomStatus.AVAILABLE));
            roomRepo.addRoom(new Room("R302", RoomType.SUITE, BedType.QUEEN, 3, RoomStatus.UNDER_MAINTENANCE));
            roomRepo.addRoom(new Room("R401", RoomType.FAMILY_SUITE, BedType.KING, 4, RoomStatus.AVAILABLE));
            roomRepo.addRoom(new Room("R402", RoomType.FAMILY_SUITE, BedType.KING, 4, RoomStatus.AVAILABLE));
        }

        if (guestRepo.count() == 0) {
            guestRepo.addGuest(new Guest("G001", "901234567V", "Sunil Fernando", "0771234567", "sunil@example.com"));
            guestRepo.addGuest(new Guest("G002", "959876543V", "Nimal Perera", "0719876543", "nimal@example.com"));
            guestRepo.addGuest(new Guest("G003", "PA1234567", "Alice Smith", "+447911123456", "alice@example.com"));
        }

        if (resRepo.count() == 0 && roomRepo.count() > 0 && guestRepo.count() > 0) {
            Room r201 = roomRepo.findById("R201");
            Guest g001 = guestRepo.findById("G001");
            if (r201 != null && g001 != null) {
                LocalDate checkIn = LocalDate.now().plusDays(5);
                LocalDate checkOut = checkIn.plusDays(4);
                Reservation sample = new Reservation("BKG001", g001, r201, 2, checkIn, checkOut,
                        10000.00, ReservationStatus.CONFIRMED);
                resRepo.addReservation(sample);
            }
        }
    }

    public synchronized void load(RoomRepository roomRepo, GuestRepository guestRepo, ReservationRepository resRepo) {
        if (!persistenceEnabled || !Files.exists(dbFile)) {
            return;
        }

        String currentSection = "";
        try (BufferedReader reader = Files.newBufferedReader(dbFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1).toUpperCase();
                    continue;
                }

                String[] parts = line.split("\\|", -1);

                switch (currentSection) {
                    case "SCHEMA:ROOMS":
                    case "ROOMS":
                        parseAndLoadRoom(parts, roomRepo);
                        break;
                    case "SCHEMA:GUESTS":
                    case "GUESTS":
                        parseAndLoadGuest(parts, guestRepo);
                        break;
                    case "SCHEMA:RESERVATIONS":
                    case "RESERVATIONS":
                        parseAndLoadReservation(parts, resRepo, roomRepo, guestRepo);
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not read database file: " + e.getMessage());
        }
    }

    private void parseAndLoadRoom(String[] parts, RoomRepository roomRepo) {
        if (parts.length < 5) {
            return;
        }
        try {
            String roomId = parts[0].trim();
            RoomType type = RoomType.valueOf(parts[1].trim());
            BedType bedType = BedType.valueOf(parts[2].trim());
            int capacity = Integer.parseInt(parts[3].trim());
            RoomStatus status = RoomStatus.valueOf(parts[4].trim());

            Room room = new Room(roomId, type, bedType, capacity, status);
            if (roomRepo.existsById(roomId)) {
                roomRepo.updateRoom(room);
            } else {
                roomRepo.addRoom(room);
            }
        } catch (Exception e) {
            System.err.println("Notice: Skipping malformed room record: " + String.join("|", parts));
        }
    }

    private void parseAndLoadGuest(String[] parts, GuestRepository guestRepo) {
        if (parts.length < 5) {
            return;
        }
        try {
            String guestId = parts[0].trim();
            String nic = parts[1].trim();
            String name = parts[2].trim();
            String phone = parts[3].trim();
            String email = parts[4].trim();

            Guest guest = new Guest(guestId, nic, name, phone, email);
            if (guestRepo.findById(guestId) != null) {
                guestRepo.updateGuest(guest);
            } else {
                guestRepo.addGuest(guest);
            }
        } catch (Exception e) {
            System.err.println("Notice: Skipping malformed guest record: " + String.join("|", parts));
        }
    }

    private void parseAndLoadReservation(String[] parts, ReservationRepository resRepo, RoomRepository roomRepo,
            GuestRepository guestRepo) {
        if (parts.length < 8) {
            return;
        }
        try {
            String bookingId = parts[0].trim();
            String roomId = parts[1].trim();
            String guestId = parts[2].trim();
            LocalDate checkIn = LocalDate.parse(parts[3].trim());
            LocalDate checkOut = LocalDate.parse(parts[4].trim());
            int numGuests = Integer.parseInt(parts[5].trim());
            double advance = Double.parseDouble(parts[6].trim());
            ReservationStatus status = ReservationStatus.valueOf(parts[7].trim());

            Room room = roomRepo.findById(roomId);
            Guest guest = guestRepo.findById(guestId);

            if (room != null && guest != null) {
                Reservation res = new Reservation(bookingId, guest, room, numGuests, checkIn, checkOut, advance,
                        status);
                if (resRepo.findByBookingId(bookingId) != null) {
                    resRepo.updateReservation(res);
                } else {
                    resRepo.addReservation(res);
                }
            }
        } catch (Exception e) {
            System.err.println("Notice: Skipping malformed reservation record: " + String.join("|", parts));
        }
    }

    public synchronized void save(RoomRepository roomRepo, GuestRepository guestRepo, ReservationRepository resRepo) {
        if (!persistenceEnabled) {
            return;
        }

        ensureDataDirectoryExists();
        Path tempFile = dataDir.resolve(DB_FILE_NAME + ".tmp");

        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
            writer.write("# ==============================================================================");
            writer.newLine();
            writer.write("# SMARTSTAY HOTEL BOOKING SYSTEM - RELATIONAL DATABASE STORAGE");
            writer.newLine();
            writer.write("# Auto-generated and synchronized on " + Instant.now());
            writer.newLine();
            writer.write("# ==============================================================================");
            writer.newLine();
            writer.newLine();

            writer.write("[SCHEMA:ROOMS]");
            writer.newLine();
            writer.write("# room_id|room_type|bed_type|capacity|status");
            writer.newLine();
            for (Room room : roomRepo.findAll()) {
                writer.write(String.format("%s|%s|%s|%d|%s",
                        room.getRoomId(),
                        room.getRoomType().name(),
                        room.getBedType().name(),
                        room.getCapacity(),
                        room.getStatus().name()));
                writer.newLine();
            }
            writer.newLine();

            writer.write("[SCHEMA:GUESTS]");
            writer.newLine();
            writer.write("# guest_id|nic_or_passport|full_name|contact_number|email");
            writer.newLine();
            for (Guest guest : guestRepo.findAll()) {
                writer.write(String.format("%s|%s|%s|%s|%s",
                        guest.getGuestId(),
                        guest.getNicOrPassport(),
                        guest.getFullName(),
                        guest.getContactNumber(),
                        guest.getEmail()));
                writer.newLine();
            }
            writer.newLine();

            writer.write("[SCHEMA:RESERVATIONS]");
            writer.newLine();
            writer.write("# booking_id|room_id|guest_id|check_in|check_out|num_guests|advance|status");
            writer.newLine();
            for (Reservation res : resRepo.findAll()) {
                writer.write(String.format(Locale.ROOT, "%s|%s|%s|%s|%s|%d|%.2f|%s",
                        res.getBookingId(),
                        res.getRoom().getRoomId(),
                        res.getGuest().getGuestId(),
                        res.getCheckInDate(),
                        res.getCheckOutDate(),
                        res.getNumberOfGuests(),
                        res.getBookingAdvance(),
                        res.getStatus().name()));
                writer.newLine();
            }

            writer.flush();
        } catch (IOException e) {
            System.err.println("Warning: Failed writing to temporary database file: " + e.getMessage());
            return;
        }

        try {
            Files.move(tempFile, dbFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.move(tempFile, dbFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException fallbackError) {
                System.err.println("Warning: Could not commit database file: " + fallbackError.getMessage());
            }
        }

        exportSqlDump(roomRepo, guestRepo, resRepo);
    }

    public synchronized void exportSqlDump(RoomRepository roomRepo, GuestRepository guestRepo,
            ReservationRepository resRepo) {
        try (BufferedWriter writer = Files.newBufferedWriter(sqlDumpFile, StandardCharsets.UTF_8)) {
            writer.write("-- =============================================================================");
            writer.newLine();
            writer.write("-- SmartStay Hotel Booking System - SQL Schema & Data Export");
            writer.newLine();
            writer.write("-- Kingston University London - CI6115 Programming III");
            writer.newLine();
            writer.write("-- Generated at: " + Instant.now());
            writer.newLine();
            writer.write("-- =============================================================================");
            writer.newLine();
            writer.newLine();

            writer.write("CREATE TABLE IF NOT EXISTS rooms (");
            writer.newLine();
            writer.write("    room_id VARCHAR(10) PRIMARY KEY,");
            writer.newLine();
            writer.write("    room_type VARCHAR(20) NOT NULL,");
            writer.newLine();
            writer.write("    bed_type VARCHAR(20) NOT NULL,");
            writer.newLine();
            writer.write("    capacity INT NOT NULL,");
            writer.newLine();
            writer.write("    status VARCHAR(20) NOT NULL");
            writer.newLine();
            writer.write(");");
            writer.newLine();
            writer.newLine();

            writer.write("CREATE TABLE IF NOT EXISTS guests (");
            writer.newLine();
            writer.write("    guest_id VARCHAR(10) PRIMARY KEY,");
            writer.newLine();
            writer.write("    nic_or_passport VARCHAR(30) UNIQUE NOT NULL,");
            writer.newLine();
            writer.write("    full_name VARCHAR(100) NOT NULL,");
            writer.newLine();
            writer.write("    contact_number VARCHAR(30) NOT NULL,");
            writer.newLine();
            writer.write("    email VARCHAR(100) NOT NULL");
            writer.newLine();
            writer.write(");");
            writer.newLine();
            writer.newLine();

            writer.write("CREATE TABLE IF NOT EXISTS reservations (");
            writer.newLine();
            writer.write("    booking_id VARCHAR(15) PRIMARY KEY,");
            writer.newLine();
            writer.write("    room_id VARCHAR(10) REFERENCES rooms(room_id),");
            writer.newLine();
            writer.write("    guest_id VARCHAR(10) REFERENCES guests(guest_id),");
            writer.newLine();
            writer.write("    check_in_date DATE NOT NULL,");
            writer.newLine();
            writer.write("    check_out_date DATE NOT NULL,");
            writer.newLine();
            writer.write("    number_of_guests INT NOT NULL,");
            writer.newLine();
            writer.write("    booking_advance DECIMAL(10,2) NOT NULL,");
            writer.newLine();
            writer.write("    status VARCHAR(20) NOT NULL");
            writer.newLine();
            writer.write(");");
            writer.newLine();
            writer.newLine();

            for (Room room : roomRepo.findAll()) {
                writer.write(String.format(
                        "INSERT INTO rooms (room_id, room_type, bed_type, capacity, status) VALUES ('%s', '%s', '%s', %d, '%s') ON CONFLICT (room_id) DO UPDATE SET status = EXCLUDED.status;",
                        room.getRoomId(), room.getRoomType().name(), room.getBedType().name(), room.getCapacity(),
                        room.getStatus().name()));
                writer.newLine();
            }
            writer.newLine();

            for (Guest guest : guestRepo.findAll()) {
                writer.write(String.format(
                        "INSERT INTO guests (guest_id, nic_or_passport, full_name, contact_number, email) VALUES ('%s', '%s', '%s', '%s', '%s') ON CONFLICT (guest_id) DO NOTHING;",
                        guest.getGuestId(), escapeSql(guest.getNicOrPassport()), escapeSql(guest.getFullName()),
                        escapeSql(guest.getContactNumber()), escapeSql(guest.getEmail())));
                writer.newLine();
            }
            writer.newLine();

            for (Reservation res : resRepo.findAll()) {
                writer.write(String.format(Locale.ROOT,
                        "INSERT INTO reservations (booking_id, room_id, guest_id, check_in_date, check_out_date, number_of_guests, booking_advance, status) VALUES ('%s', '%s', '%s', '%s', '%s', %d, %.2f, '%s') ON CONFLICT (booking_id) DO UPDATE SET status = EXCLUDED.status;",
                        res.getBookingId(), res.getRoom().getRoomId(), res.getGuest().getGuestId(),
                        res.getCheckInDate(), res.getCheckOutDate(), res.getNumberOfGuests(),
                        res.getBookingAdvance(), res.getStatus().name()));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Warning: Failed writing SQL dump: " + e.getMessage());
        }
    }

    private String escapeSql(String val) {
        if (val == null) {
            return "";
        }
        return val.replace("'", "''");
    }
}

