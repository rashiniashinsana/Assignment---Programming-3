package lk.smartstay.service;

import lk.smartstay.enums.ReservationStatus;
import lk.smartstay.enums.RoomType;
import lk.smartstay.model.Invoice;
import lk.smartstay.model.Reservation;
import lk.smartstay.repository.ReservationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceService {

    public static final double LONG_STAY_DISCOUNT_PERCENT = 0.12;
    public static final int LONG_STAY_MINIMUM_NIGHTS = 5;

    private final ReservationRepository reservationRepository;
    private final Map<String, Invoice> invoiceCache;

    public InvoiceService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
        this.invoiceCache = new HashMap<>();
    }

    public InvoiceService() {
        this(new ReservationRepository());
    }

    /**
     * Algorithm: 5-Stage Itemized Hotel Billing Calculation.
     * Computes the official guest billing invoice using high-precision BigDecimal
     * arithmetic
     * with HALF_UP rounding to 2 decimal places:
     *
     * Step 1: Base Room Charge = Daily Room Rate * Nights
     * Step 2: Extra Guest Surcharge = max(0, Actual Guests - Room Capacity) * Extra
     * Guest Rate * Nights
     * Step 3: Long-Stay Discount = If Nights >= 5, 12% discount applied exclusively
     * to Base Room Charge
     * Step 4: Government Tax = (Base Room Charge + Extra Guest Surcharge -
     * Discount) * Room Tax Rate
     * Step 5: Advance Deduction & Unused Credit = Total Bill - Advance Deposit (LKR
     * 10,000).
     * If Total Bill < Advance, Final Payable = 0.00 and remaining advance is
     * credited as refundable.
     *
     * @param reservation The confirmed reservation to bill
     * @return Fully populated immutable Invoice record
     */
    public Invoice generateInvoice(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null.");
        }
        if (reservation.isCancelled() || reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot generate invoice for a CANCELLED reservation (Booking ID: "
                    + reservation.getBookingId()
                    + "). Cancelled bookings are non-billable and subject to advance refund.");
        }

        if (invoiceCache.containsKey(reservation.getBookingId())) {
            return invoiceCache.get(reservation.getBookingId());
        }

        BigDecimal baseCharge = calculateBaseCharge(reservation);

        BigDecimal extraGuestCharge = calculateExtraGuestCharge(reservation);

        BigDecimal discount = calculateDiscount(reservation);

        BigDecimal taxableSubtotal = baseCharge.add(extraGuestCharge).subtract(discount).setScale(2,
                RoundingMode.HALF_UP);

        BigDecimal tax = calculateTax(reservation, taxableSubtotal);

        BigDecimal billTotal = taxableSubtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        BigDecimal advance = BigDecimal.valueOf(reservation.getBookingAdvance()).setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalPayable;
        BigDecimal unusedAdvanceCredit;

        if (billTotal.compareTo(advance) >= 0) {
            finalPayable = billTotal.subtract(advance).setScale(2, RoundingMode.HALF_UP);
            unusedAdvanceCredit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {

            finalPayable = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            unusedAdvanceCredit = advance.subtract(billTotal).setScale(2, RoundingMode.HALF_UP);
        }

        String invoiceId = "INV-" + reservation.getBookingId();

        Invoice invoice = new Invoice(
                invoiceId,
                reservation,
                baseCharge.doubleValue(),
                extraGuestCharge.doubleValue(),
                discount.doubleValue(),
                tax.doubleValue(),
                advance.doubleValue(),
                finalPayable.doubleValue(),
                unusedAdvanceCredit.doubleValue());

        invoiceCache.put(reservation.getBookingId(), invoice);
        return invoice;
    }

    public BigDecimal calculateBaseCharge(Reservation reservation) {
        if (reservation == null || reservation.getRoom() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        long nights = reservation.calculateNumberOfNights();
        double dailyRate = reservation.getRoom().getDailyRate();
        return BigDecimal.valueOf(dailyRate)
                .multiply(BigDecimal.valueOf(nights))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateExtraGuestCharge(Reservation reservation) {
        if (reservation == null || reservation.getRoom() == null || reservation.getRoom().getRoomType() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        int guests = reservation.getNumberOfGuests();
        int capacity = reservation.getRoom().getCapacity();
        int extraGuests = Math.max(0, guests - capacity);
        if (extraGuests == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        double extraGuestRate = reservation.getRoom().getRoomType().getExtraGuestCharge();
        long nights = reservation.calculateNumberOfNights();
        return BigDecimal.valueOf(extraGuests)
                .multiply(BigDecimal.valueOf(extraGuestRate))
                .multiply(BigDecimal.valueOf(nights))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateDiscount(Reservation reservation) {
        if (reservation == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        long nights = reservation.calculateNumberOfNights();
        if (nights < LONG_STAY_MINIMUM_NIGHTS) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal baseCharge = calculateBaseCharge(reservation);
        BigDecimal discountRate = BigDecimal.valueOf(LONG_STAY_DISCOUNT_PERCENT);
        return baseCharge.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTax(Reservation reservation, BigDecimal taxableSubtotal) {
        if (reservation == null || reservation.getRoom() == null || reservation.getRoom().getRoomType() == null
                || taxableSubtotal == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        double taxRate = reservation.getRoom().getRoomType().getTaxRate();
        return taxableSubtotal.multiply(BigDecimal.valueOf(taxRate)).setScale(2, RoundingMode.HALF_UP);
    }

    public Invoice generateInvoiceByBookingId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty() || reservationRepository == null) {
            return null;
        }
        Reservation reservation = reservationRepository.findByBookingId(bookingId.trim());
        if (reservation == null) {
            return null;
        }
        return generateInvoice(reservation);
    }

    public Invoice getInvoiceByBookingId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return null;
        }
        return invoiceCache.get(bookingId.trim());
    }

    public Invoice findInvoiceById(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            return null;
        }
        for (Invoice inv : invoiceCache.values()) {
            if (invoiceId.equalsIgnoreCase(inv.getInvoiceId())) {
                return inv;
            }
        }
        return null;
    }

    public List<Invoice> getAllInvoices() {
        return Collections.unmodifiableList(new ArrayList<>(invoiceCache.values()));
    }

    public void clearInvoices() {
        invoiceCache.clear();
    }

    public ReservationRepository getReservationRepository() {
        return reservationRepository;
    }
}
