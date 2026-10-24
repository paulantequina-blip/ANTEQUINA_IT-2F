package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner sc;

    public AdminMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    public void display(User currentUser) {
        while (true) {
            System.out.println("\n===============================");
            System.out.println(" Admin Dashboard - " + currentUser.username());
            System.out.println("===============================");
            System.out.println("1. View All Users");
            System.out.println("2. Approve User Registration");
            System.out.println("3. Add New User");
            System.out.println("4. Update User Role / Status");
            System.out.println("5. Delete User Account");
            System.out.println("6. Manage Study Spaces & Status");
            System.out.println("7. Approve / Reject Reservation");
            System.out.println("8. Logout");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    handleViewAllUsers();
                    break;
                case "2":
                    handleApproveUser();
                    break;
                case "3":
                    System.out.println("-> Feature under development.");
                    break;
                case "4":
                    System.out.println("-> Feature under development.");
                    break;
                case "5":
                    System.out.println("-> Feature under development.");
                    break;
                case "6":
                    System.out.println("-> Feature under development.");
                    break;
                case "7":
                    System.out.println("-> Feature under development.");
                    break;
                case "8":
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("-> [ERROR] Invalid option. Please try again.");
            }
        }
    }

    private void handleViewAllUsers() {
        List<User> allUsers = userDao.getAllUsers();

        if (allUsers.isEmpty()) {
            System.out.println("\n-> No users found in the system.");
            return;
        }

        System.out.println("\n=================================================================================================================");
        System.out.println("                                               ALL REGISTERED USERS                                              ");
        System.out.println("=================================================================================================================");
        System.out.println("+-----+------------+-----------------+----------+------------+------------+----------+---------------------+");
        System.out.printf("| %-3s | %-10s | %-15s | %-8s | %-10s | %-10s | %-8s | %-19s |\n",
                "ID", "Username", "Full Name", "Role", "Email", "Student ID", "Status", "Date Reg.");
        System.out.println("+-----+------------+-----------------+----------+------------+------------+----------+---------------------+");

        for (User u : allUsers) {
            String readableDate = u.date();
            try {
                long millis = Long.parseLong(u.date());
                Instant instant = Instant.ofEpochMilli(millis);
                readableDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.systemDefault())
                        .format(instant);
            } catch (Exception e) {
                // Fallback if date is not a plain millisecond timestamp string
            }

            System.out.printf("| %-3d | %-10s | %-15s | %-8s | %-10s | %-10s | %-8s | %-19s |\n",
                    u.user_id(),
                    u.username(),
                    u.fullName(),
                    u.role(),
                    u.email(),
                    u.studentId(),
                    u.status(),
                    readableDate
            );
        }
        System.out.println("+-----+------------+-----------------+----------+------------+------------+----------+---------------------+");
    }

    private void handleApproveUser() {
        List<User> pendingUsers = userDao.getPendingUsers();

        if (pendingUsers.isEmpty()) {
            System.out.println("\n-> No pending accounts found to approve.");
            return;
        }

        System.out.println("\n=================================================================================================================");
        System.out.println("                                         APPROVE PENDING USER REGISTRATIONS                                      ");
        System.out.println("=================================================================================================================");
        System.out.println("+-----+------------+-----------------+----------+------------+------------+----------+---------------------+");
        System.out.printf("| %-3s | %-10s | %-15s | %-8s | %-10s | %-10s | %-8s | %-19s |\n",
                "ID", "Username", "Full Name", "Role", "Email", "Student ID", "Status", "Date Reg.");
        System.out.println("+-----+------------+-----------------+----------+------------+------------+----------+---------------------+");

        for (User u : pendingUsers) {
            String readableDate = u.date();
            try {
                long millis = Long.parseLong(u.date());
                Instant instant = Instant.ofEpochMilli(millis);
                readableDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.systemDefault())
                        .format(instant);
            } catch (Exception e) {
                // Fallback
            }

            System.out.printf("| %-3d | %-10s | %-15s | %-8s | %-10s | %-10s | %-8s | %-19s |\n",
                    u.user_id(),
                    u.username(),
                    u.fullName(),
                    u.role(),
                    u.email(),
                    u.studentId(),
                    u.status(),
                    readableDate
            );
        }
        System.out.println("+-----+------------+-----------------+----------+------------+------------+----------+---------------------+");

        System.out.print("\nEnter User ID to approve (or type 0 to go back): ");
        try {
            int userIdToApprove = Integer.parseInt(sc.nextLine());
            if (userIdToApprove == 0) return;

            boolean success = userDao.approveUser(userIdToApprove);
            if (success) {
                System.out.println("-> Account successfully APPROVED!");
            } else {
                System.out.println("-> Failed to approve account or ID not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("-> Invalid input. Please enter a valid number ID.");
        }
    }
}