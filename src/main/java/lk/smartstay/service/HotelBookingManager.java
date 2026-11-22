package lk.smartstay.service;

import lk.smartstay.enums.BedType;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Guest;
import lk.smartstay.model.Invoice;
import lk.smartstay.model.Reservation;
import lk.smartstay.model.Room;
import lk.smartstay.repository.GuestRepository;
import lk.smartstay.repository.ReservationRepository;
import lk.smartstay.repository.RoomRepository;
import lk.smartstay.util.IdGenerator;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class HotelBookingManager {

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;
    private final RoomService roomService;
    private final GuestService guestService;
    private final ReservationService reservationService;
    private final InvoiceService invoiceService;

    public HotelBookingManager() {
        this(new RoomRepository(), new GuestRepository(), new ReservationRepository(),
                Clock.system(ReservationService.COLOMBO_ZONE));
    }

    public HotelBookingManager(RoomRepository roomRepository, GuestRepository guestRepository,
            ReservationRepository reservationRepository, Clock clock) {
        this.roomRepository = roomRepository != null ? roomRepository : new RoomRepository();
        this.guestRepository = guestRepository != null ? guestRepository : new GuestRepository();
        this.reservationRepository = reservationRepository != null ? reservationRepository
                : new ReservationRepository();
        this.roomService = new RoomService(this.roomRepository, this.reservationRepository);
        this.guestService = new GuestService(this.guestRepository);
        this.reservationService = new ReservationService(this.roomRepository, this.reservationRepository,
                this.guestRepository, clock);
        this.invoiceService = new InvoiceService(this.reservationRepository);
    }

    public void initializeDefaultData() {
        if (roomRepository.count() == 0) {
            roomRepository.addRoom(new Room("R101", RoomType.STANDARD, BedType.SINGLE, 1, RoomStatus.AVAILABLE));
            roomRepository.addRoom(new Room("R102", RoomType.STANDARD, BedType.DOUBLE, 2, RoomStatus.AVAILABLE));
            roomRepository.addRoom(new Room("R201", RoomType.DELUXE, BedType.KING, 2, RoomStatus.AVAILABLE));
            roomRepository.addRoom(new Room("R202", RoomType.DELUXE, BedType.QUEEN, 2, RoomStatus.AVAILABLE));
            roomRepository.addRoom(new Room("R301", RoomType.SUITE, BedType.KING, 3, RoomStatus.AVAILABLE));
            roomRepository.addRoom(new Room("R302", RoomType.SUITE, BedType.QUEEN, 3, RoomStatus.UNDER_MAINTENANCE));
            roomRepository.addRoom(new Room("R401", RoomType.FAMILY_SUITE, BedType.KING, 4, RoomStatus.AVAILABLE));
        }

        if (guestRepository.count() == 0) {
            guestRepository.addGuest(new Guest(IdGenerator.generateGuestId(), "901234567V", "Sunil Fernando",
                    "0771234567", "sunil@example.com"));
            guestRepository.addGuest(new Guest(IdGenerator.generateGuestId(), "959876543V", "Nimal Perera",
                    "0719876543", "nimal@example.com"));
            guestRepository.addGuest(new Guest(IdGenerator.generateGuestId(), "PA1234567", "Alice Smith",
                    "+447911123456", "alice@example.com"));
        }
    }

    public boolean addRoom(Room room) {
        return roomService.addRoom(room);
    }

    public boolean addRoom(String roomId, RoomType type, BedType bedType, int capacity) {
        return roomService.addRoom(new Room(roomId, type, bedType, capacity, RoomStatus.AVAILABLE));
    }

    public boolean updateRoom(Room room) {
        return roomService.updateRoom(room);
    }

    public boolean removeRoom(String roomId) {
        return roomService.removeRoom(roomId);
    }

    public boolean updateRoomStatus(String roomId, RoomStatus status) {
        return roomService.updateRoomStatus(roomId, status);
    }

    public Room findRoomById(String roomId) {
        return roomService.findRoomById(roomId);
    }

    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    public boolean registerGuest(Guest guest) {
        return guestService.registerGuest(guest);
    }

    public boolean registerGuest(String nic, String name, String phone, String email) {
        String guestId = IdGenerator.generateGuestId();
        return guestService.registerGuest(new Guest(guestId, nic, name, phone, email));
    }

    public Guest findGuestById(String guestId) {
        return guestService.findGuestById(guestId);
    }

    public Guest findGuestByNic(String nicOrPassport) {
        return guestService.findGuestByNicOrPassport(nicOrPassport);
    }

    public List<Guest> searchGuestsByName(String name) {
        return guestService.searchGuestsByName(name);
    }

    public List<Guest> getAllGuests() {
        return guestService.getAllGuests();
    }

    public List<Room> searchAvailableRooms(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        return reservationService.searchAvailableRooms(type, checkIn, checkOut);
    }

    public Reservation createReservation(Guest guest, Room room, int numberOfGuests, LocalDate checkIn,
            LocalDate checkOut) {
        return reservationService.createReservation(guest, room, numberOfGuests, checkIn, checkOut);
    }

    public Reservation createReservation(String guestId, String roomId, int numberOfGuests, LocalDate checkIn,
            LocalDate checkOut) {
        Guest guest = guestService.findGuestById(guestId);
        if (guest == null) {
            throw new IllegalArgumentException("Guest ID '" + guestId + "' does not exist.");
        }
        Room room = roomService.findRoomById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room ID '" + roomId + "' does not exist.");
        }
        return reservationService.createReservation(guest, room, numberOfGuests, checkIn, checkOut);
    }

    public Reservation findReservationById(String bookingId) {
        return reservationService.searchByBookingId(bookingId);
    }

    public List<Reservation> searchReservationsByGuestName(String guestName) {
        return reservationService.searchByGuestName(guestName);
    }

    public List<Reservation> getReservationsByCheckInDate(LocalDate date) {
        return reservationService.getReservationsByCheckInDate(date);
    }

    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    public boolean canCancelReservation(String bookingId) {
        Reservation res = findReservationById(bookingId);
        return reservationService.canCancel(res);
    }

    public String getCancellationIneligibilityReason(String bookingId) {
        Reservation res = findReservationById(bookingId);
        return reservationService.getCancellationIneligibilityReason(res);
    }

    public boolean cancelReservation(String bookingId) {
        return reservationService.cancelReservation(bookingId);
    }

    public double getRefundAmount(String bookingId) {
        Reservation res = findReservationById(bookingId);
        return reservationService.getRefundAmount(res);
    }

    public Invoice generateInvoice(String bookingId) {
        return invoiceService.generateInvoiceByBookingId(bookingId);
    }

    public Invoice generateInvoice(Reservation reservation) {
        return invoiceService.generateInvoice(reservation);
    }

    public Invoice getInvoiceByBookingId(String bookingId) {
        return invoiceService.getInvoiceByBookingId(bookingId);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    public RoomRepository getRoomRepository() {
        return roomRepository;
    }

    public GuestRepository getGuestRepository() {
        return guestRepository;
    }

    public ReservationRepository getReservationRepository() {
        return reservationRepository;
    }

    public RoomService getRoomService() {
        return roomService;
    }

    public GuestService getGuestService() {
        return guestService;
    }

    public ReservationService getReservationService() {
        return reservationService;
    }

    public InvoiceService getInvoiceService() {
        return invoiceService;
    }
}
