package lk.smartstay.model;

import java.util.Objects;


public class Invoice {

    private final String invoiceId;
    private final Reservation reservation;
    private final double baseRoomCharge;
    private final double extraGuestCharge;
    private final double discountAmount;
    private final double taxAmount;
    private final double bookingAdvanceDeduction;
    private final double finalPayableAmount;

    /**
     * Constructs an itemized Invoice associated with a specific Reservation.
     *
     * @param invoiceId               unique invoice reference number
     * @param reservation             the associated reservation entity
     * @param baseRoomCharge          calculated total base charge for the room stay
     * @param extraGuestCharge        surcharge for guests exceeding standard room capacity
     * @param discountAmount          deducted discount (e.g. 12% for stays >= 5 nights)
     * @param taxAmount               applicable government tax according to RoomType
     * @param bookingAdvanceDeduction upfront deposit deduction (normally LKR 10,000)
     * @param finalPayableAmount      net balance payable by the guest
     * @throws IllegalArgumentException if mandatory fields are null or invalid
     */
    public Invoice(String invoiceId, Reservation reservation, double baseRoomCharge,
                   double extraGuestCharge, double discountAmount, double taxAmount,
                   double bookingAdvanceDeduction, double finalPayableAmount) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Invoice ID cannot be null or empty.");
        }
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null.");
        }
        if (baseRoomCharge < 0 || extraGuestCharge < 0 || discountAmount < 0 || taxAmount < 0
                || bookingAdvanceDeduction < 0 || finalPayableAmount < 0) {
            throw new IllegalArgumentException("Invoice monetary amounts cannot be negative.");
        }

        this.invoiceId = invoiceId.trim();
        this.reservation = reservation;
        this.baseRoomCharge = baseRoomCharge;
        this.extraGuestCharge = extraGuestCharge;
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.bookingAdvanceDeduction = bookingAdvanceDeduction;
        this.finalPayableAmount = finalPayableAmount;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public double getBaseRoomCharge() {
        return baseRoomCharge;
    }

    public double getExtraGuestCharge() {
        return extraGuestCharge;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getBookingAdvanceDeduction() {
        return bookingAdvanceDeduction;
    }

    public double getFinalPayableAmount() {
        return finalPayableAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice invoice)) return false;
        return Objects.equals(invoiceId, invoice.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId);
    }

    @Override
    public String toString() {
        return "Invoice " + invoiceId + " [Booking: " + reservation.getBookingId()
                + ", Net Payable: LKR " + String.format("%.2f", finalPayableAmount) + "]";
    }
}
