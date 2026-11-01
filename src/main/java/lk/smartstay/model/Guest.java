package lk.smartstay.model;

import java.util.Objects;

public class Guest {

    private final String guestId;
    private String nicOrPassport;
    private String fullName;
    private String contactNumber;
    private String email;

    /**
     * Constructs a new Guest 
     *
     * @param guestId       unique identifier for the guest (immutable)
     * @param nicOrPassport National Identity Card (NIC) or Passport number
     * @param fullName      full name of the guest
     * @param contactNumber primary telephone contact number
     * @param email         email address for booking confirmations
     * @throws IllegalArgumentException if mandatory fields are null or blank
     */
    public Guest(String guestId, String nicOrPassport, String fullName, String contactNumber, String email) {
        if (guestId == null || guestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Guest ID cannot be null or empty.");
        }
        if (nicOrPassport == null || nicOrPassport.trim().isEmpty()) {
            throw new IllegalArgumentException("NIC or Passport number cannot be null or empty.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty.");
        }

        this.guestId = guestId.trim();
        this.nicOrPassport = nicOrPassport.trim();
        this.fullName = fullName.trim();
        this.contactNumber = contactNumber != null ? contactNumber.trim() : "";
        this.email = email != null ? email.trim() : "";
    }

    public String getGuestId() {
        return guestId;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty.");
        }
        this.fullName = fullName.trim();
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber != null ? contactNumber.trim() : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guest other)) return false;
        return Objects.equals(guestId, other.guestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guestId);
    }

    @Override
    public String toString() {
        return fullName + " (" + guestId + " - " + nicOrPassport + ")";
    }
}
