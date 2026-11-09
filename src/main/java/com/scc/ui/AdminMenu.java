
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.ActivityLogDao;
import com.scc.dao.DatabaseConnection;
import com.scc.model.User;
import com.scc.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner sc;

    public AdminMenu(UserDao userDao,
                     ActivityLogDao activityLogDao,
                     Scanner scanner) {

        this.userDao = userDao;
        this.activityLogDao = activityLogDao;
        this.sc = scanner;
    }

    // =====================================
    // ADMIN DASHBOARD
    // =====================================
    public void display(User currentUser) {

        boolean running = true;

        while (running) {

            System.out.println("\n========================================");
            System.out.println("             ADMIN DASHBOARD");
            System.out.println("========================================");
            System.out.println("Welcome, " + currentUser.username());
            System.out.println("----------------------------------------");

            System.out.println("[1] View All Users");
            System.out.println("[2] Approve User Registration");
            System.out.println("[3] Add New User");
            System.out.println("[4] Update User Role / Status");
            System.out.println("[5] Delete User Account");
            System.out.println("[6] Manage Study Spaces");
            System.out.println("[7] Approve / Reject Reservation");
            System.out.println("[8] View System Audit Logs");
            System.out.println("[9] Logout");

            System.out.println("========================================");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            try {

                switch (choice) {

                    case "1":
                        viewAllUsers();
                        pause();
                        break;

                    case "2":
                        approveUserRegistration(currentUser);
                        pause();
                        break;

                    case "3":
                        addNewUser(currentUser);
                        pause();
                        break;

                    case "4":
                        updateUser(currentUser);
                        pause();
                        break;

                    case "5":
                        deleteUser(currentUser);
                        pause();
                        break;

                    case "6":
                        manageStudySpaces(currentUser);
                        break;

                    case "7":
                        manageReservations();
                        pause();
                        break;

                    case "8":
                        viewAuditLogs();
                        pause();
                        break;

                    case "9":
                        logout(currentUser);
                        running = false;
                        break;

                    default:
                        System.out.println("Invalid choice.");
                        pause();
                }

            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // =====================================
    // VIEW ALL USERS
    // =====================================
    private void viewAllUsers() throws SQLException {

        var users = userDao.getAllUsers();

        if (users == null || users.isEmpty()) {
            System.out.println("\nNo users found.");
            return;
        }

        System.out.println("\n================ ALL USERS ================");

        System.out.printf(
                "%-8s %-20s %-15s %-12s %-10s%n",
                "User ID", "Full Name", "Username", "Role", "Status"
        );

        System.out.println("-".repeat(75));

        for (User user : users) {

            System.out.printf(
                    "%-8d %-20s %-15s %-12s %-10s%n",
                    user.user_id(),
                    user.fullName(),
                    user.username(),
                    user.role(),
                    user.status()
            );
        }
    }

    // =====================================
    // APPROVE USER REGISTRATION
    // =====================================
    private void approveUserRegistration(User currentUser)
            throws SQLException {

        System.out.println("\n========== APPROVE USER ==========");

        viewAllUsers();

        System.out.print("\nEnter User ID to approve (0 to cancel): ");

        int userId = Integer.parseInt(sc.nextLine().trim());

        if (userId == 0) return;

        boolean updated = userDao.updateUserStatus(
                userId, "APPROVED");

        if (updated) {

            System.out.println("User approved successfully!");

            activityLogDao.logAction(
                    currentUser.user_id(),
                    "APPROVE_USER",
                    "Admin approved user ID: " + userId
            );

        } else {
            System.out.println("User not found.");
        }
    }

    // =====================================
    // ADD NEW USER
    // =====================================
    private void addNewUser(User currentUser) throws SQLException {

        System.out.println("\n========== ADD NEW USER ==========");

        System.out.print("Full Name: ");
        String fullName = sc.nextLine().trim();

        System.out.print("Email: ");
        String email = sc.nextLine().trim();

        System.out.print("Student/Staff ID: ");
        String studentId = sc.nextLine().trim();

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        System.out.println("\n[1] ADMIN");
        System.out.println("[2] STUDENT");
        System.out.println("[3] STAFF");
        System.out.print("Choose Role: ");

        String roleChoice = sc.nextLine().trim();

        String role;

        switch (roleChoice) {

            case "1":
                role = "ADMIN";
                break;

            case "2":
                role = "STUDENT";
                break;

            case "3":
                role = "STAFF";
                break;

            default:
                System.out.println("Invalid role.");
                return;
        }

        boolean success = userDao.registerUser(
                fullName,
                email,
                username,
                password,
                role,
                studentId,
                "APPROVED"
        );

        if (success) {

            System.out.println("New user created successfully!");

            activityLogDao.logAction(
                    currentUser.user_id(),
                    "ADD_USER",
                    "Admin created new user: " + username
            );

        } else {
            System.out.println("Creation failed.");
        }
    }

    // =====================================
    // UPDATE USER
    // =====================================
    private void updateUser(User currentUser) throws SQLException {

        System.out.println("\n========== UPDATE USER ==========");

        viewAllUsers();

        System.out.print("\nEnter User ID (0 to cancel): ");

        int userId = Integer.parseInt(sc.nextLine().trim());

        if (userId == 0) return;

        if (userId == currentUser.user_id()) {
            System.out.println("You cannot modify your own admin account here.");
            return;
        }

        System.out.print("New Role (ADMIN/STUDENT/STAFF) or Enter to skip: ");
        String role = sc.nextLine().trim();

        System.out.print("New Status (APPROVED/PENDING/REJECTED) or Enter to skip: ");
        String status = sc.nextLine().trim();

        boolean updated = userDao.updateUserRoleAndStatus(
                userId,
                role.isEmpty() ? null : role.toUpperCase(),
                status.isEmpty() ? null : status.toUpperCase()
        );

        if (updated) {

            System.out.println("User updated successfully!");

            activityLogDao.logAction(
                    currentUser.user_id(),
                    "UPDATE_USER",
                    "Admin updated user ID: " + userId
            );

        } else {
            System.out.println("Update failed.");
        }
    }

    // =====================================
    // DELETE USER
    // =====================================
    private void deleteUser(User currentUser) throws SQLException {

        System.out.println("\n========== DELETE USER ==========");

        viewAllUsers();

        System.out.print("\nEnter User ID to delete (0 to cancel): ");

        int userId = Integer.parseInt(sc.nextLine().trim());

        if (userId == 0) return;

        if (userId == currentUser.user_id()) {
            System.out.println("You cannot delete your own admin account.");
            return;
        }

        System.out.print(
                "Are you sure you want to delete user #" + userId + "? (Y/N): ");

        String answer = sc.nextLine().trim();

        if (answer.equalsIgnoreCase("Y")) {

            boolean deleted = userDao.deleteUser(userId);

            if (deleted) {

                System.out.println("User deleted successfully!");

                activityLogDao.logAction(
                        currentUser.user_id(),
                        "DELETE_USER",
                        "Admin deleted user ID: " + userId
                );

            } else {
                System.out.println("User not found.");
            }

        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // =====================================
    // MANAGE STUDY SPACES
    // ADMIN ONLY
    // =====================================
    private void manageStudySpaces(User admin) {

        boolean managing = true;

        while (managing) {

            System.out.println("\n========================================");
            System.out.println("          STUDY SPACE MANAGEMENT");
            System.out.println("========================================");

            System.out.println("[1] View All Study Spaces");
            System.out.println("[2] Add Study Space");
            System.out.println("[3] Update Study Space");
            System.out.println("[4] Change Availability Status");
            System.out.println("[5] Back to Admin Dashboard");

            System.out.println("========================================");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            try {

                switch (choice) {

                    case "1":
                        viewStudySpaces();
                        pause();
                        break;

                    case "2":
                        addStudySpace(admin);
                        break;

                    case "3":
                        updateStudySpace(admin);
                        break;

                    case "4":
                        changeSpaceStatus(admin);
                        break;

                    case "5":
                        managing = false;
                        break;

                    default:
                        System.out.println("Invalid choice.");
                        pause();
                }

            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                pause();
            }
        }
    }

    // =====================================
    // VIEW STUDY SPACES
    // =====================================
    private void viewStudySpaces() throws SQLException {

        String sql = "SELECT space_id, space_name, space_type, "
                + "capacity, location, has_outlet, status "
                + "FROM study_spaces ORDER BY space_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n================ STUDY SPACES ================");

            System.out.printf(
                    "%-5s %-20s %-18s %-9s %-22s %-9s %-14s%n",
                    "ID", "Name", "Type", "Capacity",
                    "Location", "Outlet", "Status"
            );

            System.out.println("-".repeat(105));

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.printf(
                        "%-5d %-20s %-18s %-9d %-22s %-9s %-14s%n",
                        rs.getInt("space_id"),
                        rs.getString("space_name"),
                        rs.getString("space_type"),
                        rs.getInt("capacity"),
                        rs.getString("location"),
                        rs.getInt("has_outlet") == 1 ? "Yes" : "No",
                        rs.getString("status")
                );
            }

            if (!found) {
                System.out.println("No study spaces found.");
            }

            System.out.println("-".repeat(105));
        }
    }

    // =====================================
    // ADD STUDY SPACE
    // INSERT DIRECTLY INTO SQLITE
    // =====================================
    private void addStudySpace(User admin) throws SQLException {

        System.out.println("\n========== ADD STUDY SPACE ==========");

        String name = readRequired("Space Name: ");
        String type = readSpaceType();

        int capacity = readPositiveInteger("Capacity: ");

        String location = readRequired("Location: ");

        boolean hasOutlet = confirm("Has power outlet? (Y/N): ");

        System.out.println("\n---------- STUDY SPACE DETAILS ----------");
        System.out.println("Name     : " + name);
        System.out.println("Type     : " + type);
        System.out.println("Capacity : " + capacity);
        System.out.println("Location : " + location);
        System.out.println("Outlet   : " + (hasOutlet ? "Yes" : "No"));
        System.out.println("Status   : AVAILABLE");

        if (!confirm("\nSave this study space? (Y/N): ")) {
            System.out.println("Addition cancelled.");
            return;
        }

        String sql = "INSERT INTO study_spaces "
                + "(space_name, space_type, capacity, location, has_outlet, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, type);
            ps.setInt(3, capacity);
            ps.setString(4, location);
            ps.setInt(5, hasOutlet ? 1 : 0);
            ps.setString(6, "AVAILABLE");

            int result = ps.executeUpdate();

            if (result > 0) {

                int newId = 0;

                try (PreparedStatement idPs =
                             conn.prepareStatement("SELECT last_insert_rowid()");
                     ResultSet rs = idPs.executeQuery()) {

                    if (rs.next()) {
                        newId = rs.getInt(1);
                    }
                }

                System.out.println("\n========================================");
                System.out.println(" STUDY SPACE ADDED SUCCESSFULLY!");
                System.out.println("========================================");
                System.out.println("Space ID: " + newId);

                activityLogDao.logAction(
                        admin.user_id(),
                        "ADD_SPACE",
                        "Admin added study space #" + newId + ": " + name
                );

            } else {
                System.out.println("Failed to add study space.");
            }

        }

        System.out.println("\nUpdated SQLite Study Spaces:");
        viewStudySpaces();

        pause();
    }

    // =====================================
    // UPDATE STUDY SPACE
    // =====================================
    private void updateStudySpace(User admin) throws SQLException {

        System.out.println("\n========== UPDATE STUDY SPACE ==========");

        viewStudySpaces();

        int id = readNonNegativeInteger(
                "\nEnter Space ID (0 to cancel): ");

        if (id == 0) return;

        String name = readRequired("New Space Name: ");
        String type = readSpaceType();

        int capacity = readPositiveInteger("New Capacity: ");

        String location = readRequired("New Location: ");

        if (!confirm("Save changes? (Y/N): ")) {
            System.out.println("Update cancelled.");
            return;
        }

        String sql = "UPDATE study_spaces SET "
                + "space_name = ?, "
                + "space_type = ?, "
                + "capacity = ?, "
                + "location = ? "
                + "WHERE space_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, type);
            ps.setInt(3, capacity);
            ps.setString(4, location);
            ps.setInt(5, id);

            if (ps.executeUpdate() > 0) {

                System.out.println("Study space updated successfully!");

                activityLogDao.logAction(
                        admin.user_id(),
                        "UPDATE_SPACE",
                        "Admin updated study space ID: " + id
                );

            } else {
                System.out.println("Study space ID not found.");
            }
        }

        viewStudySpaces();
        pause();
    }

    // =====================================
    // CHANGE SPACE AVAILABILITY
    // =====================================
    private void changeSpaceStatus(User admin) throws SQLException {

        System.out.println("\n========== CHANGE SPACE STATUS ==========");

        viewStudySpaces();

        int id = readNonNegativeInteger(
                "\nEnter Space ID (0 to cancel): ");

        if (id == 0) return;

        System.out.println("\n[1] AVAILABLE");
        System.out.println("[2] UNAVAILABLE");
        System.out.println("[3] MAINTENANCE");

        System.out.print("Choose new status: ");

        String choice = sc.nextLine().trim();

        String status;

        switch (choice) {

            case "1":
                status = "AVAILABLE";
                break;

            case "2":
                status = "UNAVAILABLE";
                break;

            case "3":
                status = "MAINTENANCE";
                break;

            default:
                System.out.println("Invalid status.");
                return;
        }

        String sql = "UPDATE study_spaces SET status = ? "
                + "WHERE space_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            if (ps.executeUpdate() > 0) {

                System.out.println(
                        "Study space status changed to " + status + "!");

                activityLogDao.logAction(
                        admin.user_id(),
                        "UPDATE_SPACE_STATUS",
                        "Admin changed study space ID "
                                + id + " status to " + status
                );

            } else {
                System.out.println("Study space ID not found.");
            }
        }

        viewStudySpaces();
        pause();
    }

    // =====================================
    // STUDY SPACE TYPE
    // =====================================
    private String readSpaceType() {

        while (true) {

            System.out.println("\nSpace Type:");
            System.out.println("[1] PRIVATE ROOM");
            System.out.println("[2] DISCUSSION ROOM");
            System.out.println("[3] COMPUTER SPACE");
            System.out.println("[4] OTHER");

            System.out.print("Choose type: ");

            switch (sc.nextLine().trim()) {

                case "1":
                    return "PRIVATE ROOM";

                case "2":
                    return "DISCUSSION ROOM";

                case "3":
                    return "COMPUTER SPACE";

                case "4":
                    return readRequired("Enter space type: ").toUpperCase();

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // =====================================
    // MANAGE RESERVATIONS
    // =====================================
    private void manageReservations() {

        System.out.println("\n========== RESERVATION MANAGEMENT ==========");

        System.out.println(
                "Reservation approval and booking updates "
                        + "are handled through the Staff Dashboard."
        );
    }

    // =====================================
    // VIEW AUDIT LOGS
    // =====================================
    private void viewAuditLogs() throws SQLException {

        System.out.println("\n========== SYSTEM AUDIT LOGS ==========");

        System.out.println("[1] View All Logs");
        System.out.println("[2] Search By User ID");
        System.out.println("[3] Back");

        System.out.print("Enter choice: ");

        String choice = sc.nextLine().trim();

        List<ActivityLog> logs;

        switch (choice) {

            case "1":
                logs = activityLogDao.getAllLogs();
                break;

            case "2":
                System.out.print("Enter User ID: ");

                int userId = Integer.parseInt(sc.nextLine().trim());

                logs = activityLogDao.getLogsByUserId(userId);
                break;

            case "3":
                return;

            default:
                System.out.println("Invalid choice.");
                return;
        }

        if (logs == null || logs.isEmpty()) {
            System.out.println("No audit records found.");
            return;
        }

        System.out.println("\n================ ALL SYSTEM LOGS ================");

        System.out.printf(
                "%-5s %-10s %-20s %-45s %-20s%n",
                "ID", "USER ID", "ACTION", "DESCRIPTION", "TIMESTAMP"
        );

        System.out.println("-".repeat(110));

        for (ActivityLog log : logs) {

            System.out.printf(
                    "%-5d %-10d %-20s %-45s %-20s%n",
                    log.getLog_id(),
                    log.getUser_id(),
                    log.getAction(),
                    log.getDescription(),
                    log.getLog_timestamp()
            );
        }
    }

    // =====================================
    // LOGOUT
    // =====================================
    private void logout(User currentUser) {

        try {

            activityLogDao.logAction(
                    currentUser.user_id(),
                    "LOGOUT",
                    currentUser.username() + " logged out successfully"
            );

            System.out.println("\nAdmin logged out successfully!");

        } catch (SQLException e) {

            System.out.println(
                    "Unable to save logout activity: " + e.getMessage());
        }
    }

    // =====================================
    // INPUT HELPERS
    // =====================================
    private String readRequired(String message) {

        while (true) {

            System.out.print(message);

            String input = sc.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("This field is required.");
        }
    }

    private int readPositiveInteger(String message) {

        while (true) {

            System.out.print(message);

            try {

                int number = Integer.parseInt(sc.nextLine().trim());

                if (number > 0) {
                    return number;
                }

                System.out.println("Enter a number greater than zero.");

            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        }
    }

    private int readNonNegativeInteger(String message) {

        while (true) {

            System.out.print(message);

            try {

                int number = Integer.parseInt(sc.nextLine().trim());

                if (number >= 0) {
                    return number;
                }

                System.out.println("Enter 0 or a positive number.");

            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        }
    }

    private boolean confirm(String message) {

        System.out.print(message);

        String answer = sc.nextLine().trim();

        return answer.equalsIgnoreCase("Y")
                || answer.equalsIgnoreCase("YES");
    }

    private void pause() {

        System.out.println("\nPress ENTER to continue...");
        sc.nextLine();
    }
}
