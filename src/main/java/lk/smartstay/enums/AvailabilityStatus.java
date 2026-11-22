package lk.smartstay.enums;

public enum AvailabilityStatus {

    AVAILABLE("Available"),
    RESERVED("Reserved"),
    OCCUPIED("Occupied"),
    UNDER_MAINTENANCE("Under Maintenance");

    private final String displayName;

    AvailabilityStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AvailabilityStatus fromRoomStatus(RoomStatus status) {
        if (status == null) {
            return AVAILABLE;
        }
        return switch (status) {
            case AVAILABLE -> AVAILABLE;
            case RESERVED -> RESERVED;
            case UNDER_MAINTENANCE -> UNDER_MAINTENANCE;
        };
    }

    public RoomStatus toRoomStatus() {
        return switch (this) {
            case AVAILABLE -> RoomStatus.AVAILABLE;
            case RESERVED, OCCUPIED -> RoomStatus.RESERVED;
            case UNDER_MAINTENANCE -> RoomStatus.UNDER_MAINTENANCE;
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
