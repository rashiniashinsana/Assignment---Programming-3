package lk.smartstay.model;

import java.util.Objects;

public class Guest extends Person {

    private String nicOrPassport;

    public Guest(String guestId, String nicOrPassport, String fullName, String contactNumber, String email) {
        super(guestId, fullName, contactNumber, email);
        if (nicOrPassport == null || nicOrPassport.trim().isEmpty()) {
            throw new IllegalArgumentException("NIC or Passport number cannot be null or empty.");
        }
        this.nicOrPassport = nicOrPassport.trim();
    }

    public String getGuestId() {
        return getId();
    }

    public String getNicOrPassport() {
        return nicOrPassport;
    }

    public void setNicOrPassport(String nicOrPassport) {
        if (nicOrPassport == null || nicOrPassport.trim().isEmpty()) {
            throw new IllegalArgumentException("NIC or Passport number cannot be null or empty.");
        }
        this.nicOrPassport = nicOrPassport.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guest other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return fullName + " (" + id + " - " + nicOrPassport + ")";
    }
}
