package lk.smartstay.model;

public class Admin extends Person {

    private String role;

    public Admin(String adminId, String fullName, String contactNumber, String email, String role) {
        super(adminId, fullName, contactNumber, email);
        this.role = (role != null && !role.trim().isEmpty()) ? role.trim() : "ADMINISTRATOR";
    }

    public Admin(String adminId, String fullName) {
        this(adminId, fullName, "", "", "ADMINISTRATOR");
    }

    public String getAdminId() {
        return getId();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty.");
        }
        this.role = role.trim();
    }

    @Override
    public String toString() {
        return fullName + " [Admin ID: " + id + ", Role: " + role + "]";
    }
}
