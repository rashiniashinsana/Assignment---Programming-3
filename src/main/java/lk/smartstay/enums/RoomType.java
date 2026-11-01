package lk.smartstay.enums;

public enum RoomType {

    STANDARD("Standard Room", 6000.0, false, 1000.0, 0.10),
    DELUXE("Deluxe Room", 9000.0, true, 1500.0, 0.12),
    SUITE("Suite", 14000.0, true, 2000.0, 0.15),
    FAMILY_SUITE("Family Suite", 18000.0, true, 2500.0, 0.15);

    private final String displayName;
    private final double dailyRate;
    private final boolean complimentaryBreakfast;
    private final double extraGuestCharge;
    private final double taxRate;

    RoomType(String displayName, double dailyRate, boolean complimentaryBreakfast, double extraGuestCharge, double taxRate) {
        this.displayName = displayName;
        this.dailyRate = dailyRate;
        this.complimentaryBreakfast = complimentaryBreakfast;
        this.extraGuestCharge = extraGuestCharge;
        this.taxRate = taxRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public boolean hasComplimentaryBreakfast() {
        return complimentaryBreakfast;
    }

    public boolean isComplimentaryBreakfast() {
        return complimentaryBreakfast;
    }

    public double getExtraGuestCharge() {
        return extraGuestCharge;
    }

    public double getTaxRate() {
        return taxRate;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
