package lk.smartstay.util;

import java.util.concurrent.atomic.AtomicInteger;


public final class IdGenerator {

    private static final AtomicInteger guestCounter = new AtomicInteger(1);
    private static final AtomicInteger bookingCounter = new AtomicInteger(1);
    private static final AtomicInteger invoiceCounter = new AtomicInteger(1);

    private IdGenerator() {
    }

    /**
     * Generates a sequential guest identifier (e.g. GST001).
     *
     * @return formatted guest ID string
     */
    public static String generateGuestId() {
        return String.format("GST%03d", guestCounter.getAndIncrement());
    }

    /**
     * Generates a sequential booking identifier (e.g. BKG001).
     *
     * @return formatted booking ID string
     */
    public static String generateBookingId() {
        return String.format("BKG%03d", bookingCounter.getAndIncrement());
    }

    /**
     * Generates a sequential invoice identifier (e.g. INV001).
     *
     * @return formatted invoice ID string
     */
    public static String generateInvoiceId() {
        return String.format("INV%03d", invoiceCounter.getAndIncrement());
    }

    /**
     * Resets counters to initial state (useful for test suites or demo resets).
     */
    public static void reset() {
        guestCounter.set(1);
        bookingCounter.set(1);
        invoiceCounter.set(1);
    }
}
