
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

            System.out.println("\n==================================");
            System.out.println("       ADMIN DASHBOARD");
            System.out.println("==================================");
            System.out.println("Welcome, " + currentUser.username());
            System.out.println("[1] View All Users");
            System.out.println("[2] Approve User Registration");
            System.out.println("[3] Add New User");
            System.out.println("[4] Update User Role / Status");
            System.out.println("[5] Delete User Account");
            System.out.println("[6] Manage Study Spaces & Status");
            System.out.println("[7] Approve / Reject Reservation");
            System.out.println("[8] View System Audit Logs");
            System.out.println("[9] Logout");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            try {
                switch (choice) {

                    case "1":
                        viewAllUsers();
                        break;

                    case "2":
                        System.out.println("Connect your approval feature here.");
                        break;

                    case "3":
                        System.out.println("Connect your add-user feature here.");
                        break;

                    case "4":
                        System.out.println("Connect your update-user feature here.");
                        break;

                    case "5":
                        System.out.println("Connect your delete-user feature here.");
                        break;

                    case "6":
                        System.out.println("Study Space Management.");
                        break;

                    case "7":
                        System.out.println("Reservation Management.");
                        break;

                    case "8":
                        viewAuditLogs();
                        break;

                    case "9":
                        logout(currentUser);
                        running = false;
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }

            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void viewAllUsers() throws SQLException {

        var users = userDao.getAllUsers();

        if (users == null || users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        for (User user : users) {
            System.out.println("--------------------------------");
            System.out.println("User ID: " + user.user_id());
            System.out.println("Name: " + user.fullName());
            System.out.println("Username: " + user.username());
            System.out.println("Role: " + user.role());
            System.out.println("Status: " + user.status());
        }
    }

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

        if (logs.isEmpty()) {
            System.out.println("No record found.");
            return;
        }

        System.out.printf(
                "%-8s %-10s %-15s %-45s %-22s%n",
                "LOG ID", "USER ID", "ACTION",
                "DESCRIPTION", "TIMESTAMP"
        );

        for (ActivityLog log : logs) {
            System.out.printf(
                    "%-8d %-10d %-15s %-45s %-22s%n",
                    log.getLog_id(),
                    log.getUser_id(),
                    log.getAction(),
                    log.getDescription(),
                    log.getLog_timestamp()
            );
        }
    }

    private void logout(User currentUser) {

        try {
            activityLogDao.logAction(
                    currentUser.user_id(),
                    "LOGOUT",
                    currentUser.username() + " logged out successfully"
            );

            System.out.println("\nLogout successful!");
            System.out.println("Logout activity saved to database.");

        } catch (SQLException e) {
            System.out.println("Unable to save logout activity: "
                    + e.getMessage());
        }
    }
}
