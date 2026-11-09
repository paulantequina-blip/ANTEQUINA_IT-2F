
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
            System.out.println("[2] Manage User Accounts");
            System.out.println("[3] Add New User");
            System.out.println("[4] Delete User Account");
            System.out.println("[5] Manage Study Spaces");
            System.out.println("[6] View System Audit Logs");
            System.out.println("[7] Daily Booking Report");
            System.out.println("[8] Logout");

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
                        manageUserAccounts(currentUser);
                        break;

                    case "3":
                        addNewUser(currentUser);
                        pause();
                        break;

                    case "4":
                        deleteUser(currentUser);
                        pause();
                        break;

                    case "5":
                        manageStudySpaces(currentUser);
                        break;

                    case "6":
                        viewAuditLogs();
                        pause();
                        break;

                    case "7":
                        dailyBookingReport(currentUser);
                        pause();
                        break;

                    case "8":
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
                "%-8s %-25s %-20s %-12s %-12s%n",
                "User ID", "Full Name", "Username", "Role", "Status"
        );

        System.out.println("-".repeat(85));

        for (User user : users) {

            System.out.printf(
                    "%-8d %-25s %-20s %-12s %-12s%n",
                    user.user_id(),
                    user.fullName(),
                    user.username(),
                    user.role(),
                    user.status()
            );
        }

        System.out.println("-".repeat(85));
    }

    // =====================================
    // MANAGE USER ACCOUNTS
    // =====================================
    private void manageUserAccounts(User currentUser)
            throws SQLException {

        boolean managing = true;

        while (managing) {

            System.out.println("\n========================================");
            System.out.println("          MANAGE USER ACCOUNTS");
            System.out.println("========================================");

            System.out.println("[1] View All Users");
            System.out.println("[2] Approve User Registration");
            System.out.println("[3] Update User Role");
            System.out.println("[4] Update User Status");
            System.out.println("[5] Back to Admin Dashboard");

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
                        updateUserRole(currentUser);
                        pause();
                        break;

                    case "4":
                        updateUserStatus(currentUser);
                        pause();
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
    // APPROVE USER REGISTRATION
    // =====================================
    private void approveUserRegistration(User currentUser)
            throws SQLException {

        System.out.println("\n========== APPROVE USER REGISTRATION ==========");

        viewAllUsers();

        int userId = readNonNegativeInteger(
                "\nEnter User ID to approve (0 to cancel): "
        );

        if (userId == 0) return;

        if (userId == currentUser.user_id()) {
            System.out.println("You cannot change your own admin account.");
            return;
        }

        boolean updated = userDao.updateUserStatus(
                userId, "APPROVED"
        );

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
    // UPDATE USER ROLE
    // =====================================
    private void updateUserRole(User currentUser)
            throws SQLException {

        System.out.println("\n========== UPDATE USER ROLE ==========");

        viewAllUsers();

        int userId = readNonNegativeInteger(
                "\nEnter User ID (0 to cancel): "
        );

        if (userId == 0) return;

        if (userId == currentUser.user_id()) {
            System.out.println(
                    "You cannot modify your own admin account."
            );
            return;
        }

        System.out.println("\n[1] ADMIN");
        System.out.println("[2] STUDENT");
        System.out.println("[3] STAFF");

        System.out.print("Choose New Role: ");

        String role;

        switch (sc.nextLine().trim()) {

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

        boolean updated = userDao.updateUserRoleAndStatus(
                userId,
                role,
                null
        );

        if (updated) {

            System.out.println("User role updated successfully!");

            activityLogDao.logAction(
                    currentUser.user_id(),
                    "UPDATE_USER_ROLE",
                    "Admin changed user ID " + userId
                            + " role to " + role
            );

        } else {
            System.out.println("User not found or update failed.");
        }
    }

    // =====================================
    // UPDATE USER STATUS
    // =====================================
    private void updateUserStatus(User currentUser)
            throws SQLException {

        System.out.println("\n========== UPDATE USER STATUS ==========");

        viewAllUsers();

        int userId = readNonNegativeInteger(
                "\nEnter User ID (0 to cancel): "
        );

        if (userId == 0) return;

        if (userId == currentUser.user_id()) {
            System.out.println(
                    "You cannot modify your own admin account."
            );
            return;
        }

        System.out.println("\n[1] APPROVED");
        System.out.println("[2] PENDING");
        System.out.println("[3] REJECTED");

        System.out.print("Choose New Status: ");

        String status;

        switch (sc.nextLine().trim()) {

            case "1":
                status = "APPROVED";
                break;

            case "2":
                status = "PENDING";
                break;

            case "3":
                status = "REJECTED";
                break;

            default:
                System.out.println("Invalid status.");
                return;
        }

        boolean updated = userDao.updateUserRoleAndStatus(
                userId,
                null,
                status
        );

        if (updated) {

            System.out.println("User status updated successfully!");

            activityLogDao.logAction(
                    currentUser.user_id(),
                    "UPDATE_USER_STATUS",
                    "Admin changed user ID " + userId
                            + " status to " + status
            );

        } else {
            System.out.println("User not found or update failed.");
        }
    }

    // =====================================
    // ADD NEW USER
    // =====================================
    private void addNewUser(User currentUser) throws SQLException {

        System.out.println("\n========== ADD NEW USER ==========");

        String fullName = readRequired("Full Name: ");
        String email = readRequired("Email: ");
        String studentId = readRequired("Student/Staff ID: ");
        String username = readRequired("Username: ");
        String password = readRequired("Password: ");

        System.out.println("\n[1] ADMIN");
        System.out.println("[2] STUDENT");
        System.out.println("[3] STAFF");

        System.out.print("Choose Role: ");

        String role;

        switch (sc.nextLine().trim()) {

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
    // DELETE USER
    // =====================================
    private void deleteUser(User currentUser) throws SQLException {

        System.out.println("\n========== DELETE USER ==========");

        viewAllUsers();

        int userId = readNonNegativeInteger(
                "\nEnter User ID to delete (0 to cancel): "
        );

        if (userId == 0) return;

        if (userId == currentUser.user_id()) {
            System.out.println(
                    "You cannot delete your own admin account."
            );
            return;
        }

        if (confirm(
                "Are you sure you want to delete user #"
                        + userId + "? (Y/N): "
        )) {

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
                    "%-7s %-25s %-22s %-10s %-25s %-10s %-15s%n",
                    "ID", "Name", "Type", "Capacity",
                    "Location", "Outlet", "Status"
            );

            System.out.println("-".repeat(125));

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.printf(
                        "%-7d %-25s %-22s %-10d %-25s %-10s %-15s%n",
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

            System.out.println("-".repeat(125));
        }
    }

    // =====================================
    // ADD STUDY SPACE
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
                "\nEnter Space ID (0 to cancel): "
        );

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
                "\nEnter Space ID (0 to cancel): "
        );

        if (id == 0) return;

        System.out.println("\n[1] AVAILABLE");
        System.out.println("[2] UNAVAILABLE");
        System.out.println("[3] MAINTENANCE");

        System.out.print("Choose new status: ");

        String status;

        switch (sc.nextLine().trim()) {

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
                        "Study space status changed to " + status + "!"
                );

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
                    return readRequired(
                            "Enter space type: "
                    ).toUpperCase();

                default:
                    System.out.println("Invalid choice.");
            }
        }
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
                int userId = readPositiveInteger("Enter User ID: ");
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
                "%-7s %-10s %-22s %-50s %-22s%n",
                "ID", "USER ID", "ACTION", "DESCRIPTION", "TIMESTAMP"
        );

        System.out.println("-".repeat(120));

        for (ActivityLog log : logs) {

            System.out.printf(
                    "%-7d %-10d %-22s %-50s %-22s%n",
                    log.getLog_id(),
                    log.getUser_id(),
                    log.getAction(),
                    log.getDescription(),
                    log.getLog_timestamp()
            );
        }
    }

    // =====================================
    // DAILY BOOKING REPORT
    // =====================================
    private void dailyBookingReport(User admin) throws SQLException {

        System.out.println("\n==============================================================");
        System.out.println("                    DAILY BOOKING REPORT");
        System.out.println("==============================================================");

        System.out.print("Enter Date (YYYY-MM-DD): ");

        String date = sc.nextLine().trim();

        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date. Use YYYY-MM-DD.");
            return;
        }

        String sql = """
                SELECT
                    b.booking_id,
                    b.booking_date,
                    s.space_name,
                    u.full_name,
                    b.start_time,
                    b.end_time,
                    b.status
                FROM bookings b
                INNER JOIN users u
                    ON b.user_id = u.user_id
                INNER JOIN study_spaces s
                    ON b.space_id = s.space_id
                WHERE b.booking_date = ?
                ORDER BY b.start_time
                """;

        int totalBookings = 0;
        int approved = 0;
        int pending = 0;
        int cancelled = 0;
        int noShow = 0;
        int other = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date);

            try (ResultSet rs = ps.executeQuery()) {

                System.out.println("\nREPORT DATE: " + date);
                System.out.println("=".repeat(145));

                System.out.printf(
                        "%-9s %-13s %-25s %-23s %-15s %-15s %-15s%n",
                        "Book ID",
                        "Date",
                        "Study Space",
                        "Student Name",
                        "Start Time",
                        "End Time",
                        "Status"
                );

                System.out.println("-".repeat(145));

                while (rs.next()) {

                    totalBookings++;

                    String status = rs.getString("status");

                    if (status == null) {
                        status = "UNKNOWN";
                    }

                    switch (status.toUpperCase()) {

                        case "APPROVED":
                            approved++;
                            break;

                        case "PENDING":
                            pending++;
                            break;

                        case "CANCELLED":
                            cancelled++;
                            break;

                        case "NO_SHOW":
                        case "NO-SHOW":
                            noShow++;
                            break;

                        default:
                            other++;
                            break;
                    }

                    System.out.printf(
                            "%-9d %-13s %-25s %-23s %-15s %-15s %-15s%n",
                            rs.getInt("booking_id"),
                            rs.getString("booking_date"),
                            rs.getString("space_name"),
                            rs.getString("full_name"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            status
                    );
                }

                System.out.println("-".repeat(145));

                if (totalBookings == 0) {
                    System.out.println(
                            "No bookings found for this date."
                    );
                }
            }
        }

        System.out.println("\n================================================");
        System.out.println("                 REPORT SUMMARY");
        System.out.println("================================================");

        System.out.println("Report Date        : " + date);
        System.out.println("Total Bookings     : " + totalBookings);
        System.out.println("Approved Bookings  : " + approved);
        System.out.println("Pending Bookings   : " + pending);
        System.out.println("Cancelled Bookings : " + cancelled);
        System.out.println("No-Show Bookings   : " + noShow);
        System.out.println("Other Statuses     : " + other);

        System.out.println("================================================");

        activityLogDao.logAction(
                admin.user_id(),
                "VIEW_DAILY_REPORT",
                "Admin generated daily booking report for " + date
        );
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
                    "Unable to save logout activity: " + e.getMessage()
            );
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

                System.out.println(
                        "Enter a number greater than zero."
                );

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

                System.out.println(
                        "Enter 0 or a positive number."
                );

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
