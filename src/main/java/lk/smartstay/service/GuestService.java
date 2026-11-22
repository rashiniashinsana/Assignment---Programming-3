package lk.smartstay.service;

import lk.smartstay.model.Guest;
import lk.smartstay.repository.DatabaseManager;
import lk.smartstay.repository.GuestRepository;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class GuestService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        if (guestRepository == null) {
            throw new IllegalArgumentException("GuestRepository cannot be null.");
        }
        this.guestRepository = guestRepository;
    }

    public boolean registerGuest(Guest guest) {
        if (!validateGuestData(guest)) {
            return false;
        }

        String id = guest.getGuestId().trim();
        if (guestRepository.findById(id) != null) {
            return false;
        }

        if (guestRepository.findByNicOrPassport(guest.getNicOrPassport().trim()) != null) {
            return false;
        }

        boolean ok = guestRepository.addGuest(guest);
        if (ok) {
            DatabaseManager.getInstance().autoSave();
        }
        return ok;
    }

    public boolean updateGuest(Guest guest) {
        if (!validateGuestData(guest)) {
            return false;
        }

        String id = guest.getGuestId().trim();
        if (guestRepository.findById(id) == null) {
            return false;
        }

        boolean ok = guestRepository.updateGuest(guest);
        if (ok) {
            DatabaseManager.getInstance().autoSave();
        }
        return ok;
    }

    public Guest findGuestById(String guestId) {
        if (guestId == null || guestId.trim().isEmpty()) {
            return null;
        }
        return guestRepository.findById(guestId.trim());
    }

    public Guest findGuestByNicOrPassport(String nicOrPassport) {
        if (nicOrPassport == null || nicOrPassport.trim().isEmpty()) {
            return null;
        }
        return guestRepository.findByNicOrPassport(nicOrPassport.trim());
    }

    public List<Guest> searchGuestsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return guestRepository.findByName(name.trim());
    }

    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    public int getGuestCount() {
        return guestRepository.count();
    }

    private boolean validateGuestData(Guest guest) {
        if (guest == null) {
            return false;
        }
        if (guest.getGuestId() == null || guest.getGuestId().trim().isEmpty()) {
            return false;
        }
        if (guest.getNicOrPassport() == null || guest.getNicOrPassport().trim().isEmpty()) {
            return false;
        }
        if (guest.getFullName() == null || guest.getFullName().trim().isEmpty()) {
            return false;
        }
        if (guest.getContactNumber() == null || guest.getContactNumber().trim().isEmpty()) {
            return false;
        }
        if (guest.getEmail() == null || guest.getEmail().trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(guest.getEmail().trim()).matches();
    }
}
