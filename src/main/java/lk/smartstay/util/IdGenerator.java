package lk.smartstay.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe identifier generation algorithm.
 * Uses AtomicInteger counters to generate sequential, zero-padded identifiers
 * formatted according to domain prefixes:
 * - GST%03d for Guests (e.g. GST001)
 * - BKG%03d for Reservations (e.g. BKG001)
 * - INV%03d for Invoices (e.g. INV001)
 */
public final class IdGenerator {

    private static final AtomicInteger guestCounter = new AtomicInteger(1);
    private static final AtomicInteger bookingCounter = new AtomicInteger(1);
    private static final AtomicInteger invoiceCounter = new AtomicInteger(1);

    private IdGenerator() {
    }

    public static String generateGuestId() {
        return String.format("GST%03d", guestCounter.getAndIncrement());
    }

    public static String peekNextGuestId() {
        return String.format("GST%03d", guestCounter.get());
    }

    public static String generateBookingId() {
        return String.format("BKG%03d", bookingCounter.getAndIncrement());
    }

    public static String generateInvoiceId() {
        return String.format("INV%03d", invoiceCounter.getAndIncrement());
    }

    public static void reset() {
        guestCounter.set(1);
        bookingCounter.set(1);
        invoiceCounter.set(1);
    }
}
