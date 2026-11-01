package lk.smartstay.model;

import lk.smartstay.enums.BedType;
import lk.smartstay.enums.RoomStatus;
import lk.smartstay.enums.RoomType;

import java.util.Objects;


public class Room {

    private final String roomId;
    private RoomType roomType;
    private BedType bedType;
    private int capacity;
    private RoomStatus status;

    /**
     * Constructs a new Room with an initial availability status of AVAILABLE.
     *
     * @param roomId   unique room number or identifier (immutable)
     * @param roomType category and package tier of the room
     * @param bedType  bed configuration
     * @param capacity maximum guest capacity (must be greater than 0)
     */
    public Room(String roomId, RoomType roomType, BedType bedType, int capacity) {
        this(roomId, roomType, bedType, capacity, RoomStatus.AVAILABLE);
    }

    /**
     * Constructs a new Room with all required domain attributes.
     *
     * @param roomId   unique room number or identifier (immutable)
     * @param roomType category and package tier of the room
     * @param bedType  bed configuration
     * @param capacity maximum guest capacity (must be greater than 0)
     * @param status   current operational status
     * @throws IllegalArgumentException if inputs are null or capacity <= 0
     */
    public Room(String roomId, RoomType roomType, BedType bedType, int capacity, RoomStatus status) {
        if (roomId == null || roomId.trim().isEmpty()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty.");
        }
        if (roomType == null) {
            throw new IllegalArgumentException("Room type cannot be null.");
        }
        if (bedType == null) {
            throw new IllegalArgumentException("Bed type cannot be null.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Room capacity must be greater than zero.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Room status cannot be null.");
        }

        this.roomId = roomId.trim();
        this.roomType = roomType;
        this.bedType = bedType;
        this.capacity = capacity;
        this.status = status;
    }

    public String getRoomId() {
        return roomId;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        if (roomType == null) {
            throw new IllegalArgumentException("Room type cannot be null.");
        }
        this.roomType = roomType;
    }

    public BedType getBedType() {
        return bedType;
    }

    public void setBedType(BedType bedType) {
        if (bedType == null) {
            throw new IllegalArgumentException("Bed type cannot be null.");
        }
        this.bedType = bedType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Room capacity must be greater than zero.");
        }
        this.capacity = capacity;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        updateStatus(status);
    }

    /**
     *  availability status of the room.
     *
     * @param newStatus the new status to set
     */
    public void updateStatus(RoomStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Room status cannot be null.");
        }
        this.status = newStatus;
    }

    /**
     * Checks currently available.
     *
     * @return true if status is AVAILABLE, false otherwise
     */
    public boolean isAvailable() {
        return this.status == RoomStatus.AVAILABLE;
    }

    // --- Derived Domain Data  ---

    public double getDailyRate() {
        return roomType.getDailyRate();
    }

    public double getTaxRate() {
        return roomType.getTaxRate();
    }

    public double getExtraGuestCharge() {
        return roomType.getExtraGuestCharge();
    }

    public boolean hasComplimentaryBreakfast() {
        return roomType.hasComplimentaryBreakfast();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room other)) return false;
        return Objects.equals(roomId, other.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId);
    }

    @Override
    public String toString() {
        return "Room " + roomId + " [" + roomType.getDisplayName() + ", " + bedType.getDisplayName() + ", Status: " + status.getDisplayName() + "]";
    }
}
