package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.ActivityLogDao;
import com.scc.model.User;
import com.scc.model.ActivityLog;

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

    public void display(User currentUser) {

        boolean running = true;

        while (running) {

            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║            ADMIN DASHBOARD             ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println(" Welcome, " + currentUser.username());
            System.out.println("──────────────────────────────────────────");
            System.out.println(" [1] View All Users");
            System.out.println(" [2] Approve User Registration");
            System.out.println(" [3] Add New User");
            System.out.println(" [4] Update User Role / Status");
            System.out.println(" [5] Delete User Account");
            System.out.println(" [6] Manage Study Spaces & Status");
            System.out.println(" [7] Approve / Reject Reservation");
            System.out.println(" [8] View System Audit Logs");
            System.out.println(" [9] Logout");
            System.out.println("══════════════════════════════════════════");
            System.out.print(" Enter choice: ");

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
                        manageStudySpaces();
                        pause();
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
                        System.out.println(" [!] Invalid choice. Please try again.");
                }

            } catch (SQLException e) {
                System.out.println(" [X] Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println(" [X] Invalid input format. Please enter numbers where required.");
            }
        }
    }

    private void viewAllUsers() throws SQLException {
        var users = userDao.getAllUsers();

        if (users == null || users.isEmpty()) {
            System.out.println("\n [!] No users found in the database.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                           ALL REGISTERED USERS                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
        System.out.printf(" %-8s %-20s %-15s %-12s %-10s%n", "User ID", "Full Name", "Username", "Role", "Status");
        System.out.println("────────────────────────────────────────────────────────────────────────");

        for (User user : users) {
            System.out.printf(" %-8d %-20s %-15s %-12s %-10s%n",
                    user.user_id(),
                    user.fullName(),
                    user.username(),
                    user.role(),
                    user.status());
        }
        System.out.println("────────────────────────────────────────────────────────────────────────");
    }

    private void approveUserRegistration(User currentUser) throws SQLException {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      APPROVE USER REGISTRATION         ║");
        System.out.println("╚════════════════════════════════════════╝");

        viewAllUsers();

        System.out.print("\n Enter User ID to approve (0 to cancel): ");
        int userId = Integer.parseInt(sc.nextLine().trim());

        if (userId == 0) return;

        boolean updated = userDao.updateUserStatus(userId, "APPROVED");
        if (updated) {
            System.out.println(" [✔] User ID #" + userId + " has been successfully APPROVED.");
            activityLogDao.logAction(currentUser.user_id(), "APPROVE_USER", "Admin approved user ID: " + userId);
        } else {
            System.out.println(" [X] Failed to approve user. Verify if the User ID exists.");
        }
    }

    private void addNewUser(User currentUser) throws SQLException {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║             ADD NEW USER               ║");
        System.out.println("╚════════════════════════════════════════╝");

        System.out.print(" Enter Full Name: ");
        String fullName = sc.nextLine().trim();

        System.out.print(" Enter Email: ");
        String email = sc.nextLine().trim();

        System.out.print(" Enter Student/Staff ID: ");
        String studentId = sc.nextLine().trim();

        System.out.print(" Enter Username: ");
        String username = sc.nextLine().trim();

        System.out.print(" Enter Password: ");
        String password = sc.nextLine().trim();

        System.out.print(" Enter Role [1] ADMIN [2] STUDENT [3] STAFF: ");
        String roleChoice = sc.nextLine().trim();

        String role = "STUDENT";
        switch (roleChoice) {
            case "1": role = "ADMIN"; break;
            case "2": role = "STUDENT"; break;
            case "3": role = "STAFF"; break;
            default: System.out.println(" [!] Invalid role choice, defaulting to STUDENT.");
        }

        boolean success = userDao.registerUser(fullName, email, username, password, role, studentId, "APPROVED");

        if (success) {
            System.out.println(" [✔] New user account created successfully!");
            activityLogDao.logAction(currentUser.user_id(), "ADD_USER", "Admin created new user: " + username);
        } else {
            System.out.println(" [X] Creation failed. Username or student ID might already exist.");
        }
    }

    private void updateUser(User currentUser) throws SQLException {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      UPDATE USER ROLE / STATUS         ║");
        System.out.println("╚════════════════════════════════════════╝");

        viewAllUsers();

        System.out.print("\n Enter User ID to update (0 to cancel): ");
        int userId = Integer.parseInt(sc.nextLine().trim());

        if (userId == 0) return;

        System.out.print(" Enter new Role (ADMIN / STUDENT / STAFF) or press Enter to skip: ");
        String role = sc.nextLine().trim();

        System.out.print(" Enter new Status (APPROVED / PENDING / REJECTED) or press Enter to skip: ");
        String status = sc.nextLine().trim();

        boolean updated = userDao.updateUserRoleAndStatus(
                userId,
                role.isEmpty() ? null : role.toUpperCase(),
                status.isEmpty() ? null : status.toUpperCase()
        );

        if (updated) {
            System.out.println(" [✔] User ID #" + userId + " updated successfully.");
            activityLogDao.logAction(currentUser.user_id(), "UPDATE_USER", "Admin updated user ID: " + userId);
        } else {
            System.out.println(" [X] Update failed. Verify the User ID.");
        }
    }

    private void deleteUser(User currentUser) throws SQLException {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           DELETE USER ACCOUNT          ║");
        System.out.println("╚════════════════════════════════════════╝");

        viewAllUsers();

        System.out.print("\n Enter User ID to delete (0 to cancel): ");
        int userId = Integer.parseInt(sc.nextLine().trim());

        if (userId == 0) return;

        System.out.print(" Are you sure you want to delete user #" + userId + "? (Y/N): ");
        String confirm = sc.nextLine().trim();

        if (confirm.equalsIgnoreCase("Y")) {
            boolean deleted = userDao.deleteUser(userId);
            if (deleted) {
                System.out.println(" [✔] User account deleted successfully.");
                activityLogDao.logAction(currentUser.user_id(), "DELETE_USER", "Admin deleted user ID: " + userId);
            } else {
                System.out.println(" [X] Deletion failed. User ID not found.");
            }
        } else {
            System.out.println(" [!] Deletion cancelled.");
        }
    }

    private void manageStudySpaces() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         STUDY SPACE MANAGEMENT         ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println(" [!] Note: Study space addition and availability management");
        System.out.println("     are fully accessible through the Staff Dashboard ([6]).");
    }

    private void manageReservations() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         RESERVATION MANAGEMENT         ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println(" [!] Note: Approving, rejecting, and managing reservations");
        System.out.println("     are managed dynamically via the Staff Dashboard ([2] & [3]).");
    }

    private void viewAuditLogs() throws SQLException {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           SYSTEM AUDIT LOGS            ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println(" [1] View All Logs");
        System.out.println(" [2] Search By User ID");
        System.out.println(" [3] Back");
        System.out.print(" Enter choice: ");

        String choice = sc.nextLine().trim();
        List<ActivityLog> logs;

        switch (choice) {
            case "1":
                logs = activityLogDao.getAllLogs();
                break;
            case "2":
                System.out.print(" Enter User ID: ");
                int userId = Integer.parseInt(sc.nextLine().trim());
                logs = activityLogDao.getLogsByUserId(userId);
                break;
            case "3":
                return;
            default:
                System.out.println(" [!] Invalid choice.");
                return;
        }

        if (logs == null || logs.isEmpty()) {
            System.out.println(" [!] No audit records found.");
            return;
        }

        System.out.println("ALL SYSTEM LOGS");
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-9s | %-16s | %-55s | %-19s%n", "ID", "USER ID", "ACTION TYPE", "DESCRIPTION", "TIMESTAMP");
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------");

        for (ActivityLog log : logs) {
            System.out.printf("%-4d | %-9d | %-16s | %-55s | %-19s%n",
                    log.getLog_id(),
                    log.getUser_id(),
                    log.getAction(),
                    log.getDescription(),
                    log.getLog_timestamp()
            );
        }
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------");
    }

    private void logout(User currentUser) {
        try {
            activityLogDao.logAction(
                    currentUser.user_id(),
                    "LOGOUT",
                    currentUser.username() + " logged out successfully"
            );

            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║          LOGOUT SUCCESSFUL             ║");
            System.out.println("╚════════════════════════════════════════╝");

        } catch (SQLException e) {
            System.out.println(" [X] Unable to save logout activity: " + e.getMessage());
        }
    }

    private void pause() {
        System.out.println("\n Press ENTER to continue...");
        sc.nextLine();
    }
}