package lk.smartstay.repository;

import lk.smartstay.model.Guest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GuestRepository {

    private final Map<String, Guest> guests;

    public GuestRepository() {
        this.guests = new HashMap<>();
    }

    public boolean addGuest(Guest guest) {
        if (guest == null || guest.getGuestId() == null || guest.getGuestId().trim().isEmpty()) {
            return false;
        }
        String id = guest.getGuestId().trim();
        if (guests.containsKey(id)) {
            return false;
        }
        if (findByNicOrPassport(guest.getNicOrPassport()) != null) {
            return false;
        }
        guests.put(id, guest);
        return true;
    }

    public boolean updateGuest(Guest guest) {
        if (guest == null || guest.getGuestId() == null || guest.getGuestId().trim().isEmpty()) {
            return false;
        }
        String id = guest.getGuestId().trim();
        if (!guests.containsKey(id)) {
            return false;
        }

        Guest existingWithNic = findByNicOrPassport(guest.getNicOrPassport());
        if (existingWithNic != null && !existingWithNic.getGuestId().equalsIgnoreCase(id)) {
            return false;
        }

        guests.put(id, guest);
        return true;
    }

    public Guest findById(String guestId) {
        if (guestId == null || guestId.trim().isEmpty()) {
            return null;
        }
        return guests.get(guestId.trim());
    }

    public Guest findByNicOrPassport(String nicOrPassport) {
        if (nicOrPassport == null || nicOrPassport.trim().isEmpty()) {
            return null;
        }
        String target = nicOrPassport.trim();
        for (Guest g : guests.values()) {
            if (g.getNicOrPassport() != null && g.getNicOrPassport().equalsIgnoreCase(target)) {
                return g;
            }
        }
        return null;
    }

    public List<Guest> findByName(String name) {
        List<Guest> matches = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) {
            return matches;
        }
        String query = name.trim().toLowerCase(Locale.ROOT);
        for (Guest g : guests.values()) {
            if (g.getFullName() != null && g.getFullName().toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(g);
            }
        }
        return matches;
    }

    public List<Guest> findAll() {
        return new ArrayList<>(guests.values());
    }

    public int count() {
        return guests.size();
    }
}
