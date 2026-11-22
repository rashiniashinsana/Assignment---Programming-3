-- =============================================================================
-- SmartStay Hotel Booking System - SQL Schema & Data Export
-- Kingston University London - CI6115 Programming III
-- Generated at: 2026-11-22T16:48:12.373131300Z
-- =============================================================================

CREATE TABLE IF NOT EXISTS rooms (
    room_id VARCHAR(10) PRIMARY KEY,
    room_type VARCHAR(20) NOT NULL,
    bed_type VARCHAR(20) NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS guests (
    guest_id VARCHAR(10) PRIMARY KEY,
    nic_or_passport VARCHAR(30) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(30) NOT NULL,
    email VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS reservations (
    booking_id VARCHAR(15) PRIMARY KEY,
    room_id VARCHAR(10) REFERENCES rooms(room_id),
    guest_id VARCHAR(10) REFERENCES guests(guest_id),
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_guests INT NOT NULL,
    booking_advance DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL
);

INSERT INTO rooms (room_id, room_type, bed_type, capacity, status) VALUES ('001', 'FAMILY_SUITE', 'KING', 5, 'AVAILABLE') ON CONFLICT (room_id) DO UPDATE SET status = EXCLUDED.status;

INSERT INTO guests (guest_id, nic_or_passport, full_name, contact_number, email) VALUES ('GST001', '200272601014', 'AVR Ashinsana', '0778874455', 'rashi@gmail.com') ON CONFLICT (guest_id) DO NOTHING;

INSERT INTO reservations (booking_id, room_id, guest_id, check_in_date, check_out_date, number_of_guests, booking_advance, status) VALUES ('BKG001', '001', 'GST001', '2026-11-24', '2026-11-27', 5, 10000.00, 'CONFIRMED') ON CONFLICT (booking_id) DO UPDATE SET status = EXCLUDED.status;
