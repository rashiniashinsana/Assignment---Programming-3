package lk.smartstay.model;

import lk.smartstay.enums.AvailabilityStatus;
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

    public Room(String roomId, RoomType roomType, BedType bedType, int capacity) {
        this(roomId, roomType, bedType, capacity, RoomStatus.AVAILABLE);
    }

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

    public Room(String roomId, RoomType roomType, BedType bedType, int capacity, AvailabilityStatus availabilityStatus) {
        this(roomId, roomType, bedType, capacity, availabilityStatus != null ? availabilityStatus.toRoomStatus() : RoomStatus.AVAILABLE);
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

    public void updateStatus(RoomStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Room status cannot be null.");
        }
        this.status = newStatus;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return AvailabilityStatus.fromRoomStatus(this.status);
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        if (availabilityStatus != null) {
            updateStatus(availabilityStatus.toRoomStatus());
        }
    }

    public boolean isAvailable() {
        return this.status == RoomStatus.AVAILABLE;
    }

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
