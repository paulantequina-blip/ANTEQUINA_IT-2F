package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

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
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role \\ status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Manage Study Spaces & Status");
            System.out.println("8. Approve / Reject Reservation");
            System.out.println("9. Logout");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    // View all users implementation
                    break;
                case "2":
                    handlePendingUsers();
                    break;
                case "3":
                    // Approve user registration implementation
                    break;
                case "4":
                    // Add new user implementation
                    break;
                case "5":
                    // Update user role / status implementation
                    break;
                case "6":
                    // Delete user account implementation
                    break;
                case "7":
                    // Manage study spaces & status implementation
                    break;
                case "8":
                    // Approve / reject reservation implementation
                    break;
                case "9":
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void handlePendingUsers() {
        List<User> pendingUsers = userDao.getPendingUsers();

        if (pendingUsers.isEmpty()) {
            System.out.println("\n-> No pending accounts found.");
            return;
        }

        System.out.println("\n--- PENDING ACCOUNTS ---");
        for (User u : pendingUsers) {
            System.out.println(u);
        }

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