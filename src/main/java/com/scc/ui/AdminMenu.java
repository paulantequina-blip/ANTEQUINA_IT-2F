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
                    handleAddNewUser();
                    break;
                case "4":
                    handleUpdateUserRoleAndStatus(currentUser);
                    break;
                case "5":
                    handleDeleteUser(currentUser);
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
        int userIdToApprove = readIntInput("");
        if (userIdToApprove == 0) return;

        boolean success = userDao.approveUser(userIdToApprove);
        if (success) {
            System.out.println("-> Account successfully APPROVED!");
        } else {
            System.out.println("-> Failed to approve account or ID not found.");
        }
    }

    private void handleAddNewUser() {
        System.out.println("\n--- ADD NEW USER ---");
        System.out.print("Enter Username (or type 0 to cancel): ");
        String username = sc.nextLine().trim();
        if (username.equals("0")) return;

        System.out.print("Enter Full Name: ");
        String fullName = sc.nextLine().trim();

        System.out.print("Enter Email: ");
        String email = sc.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        String role = readValidRole();
        if (role.equalsIgnoreCase("CANCEL")) return;

        System.out.print("Enter Student ID (or N/A): ");
        String studentId = sc.nextLine().trim();

        String status = readValidStatus();
        if (status.equalsIgnoreCase("CANCEL")) return;

        User newUser = new User(0, fullName, email, username, password, role, studentId, status, String.valueOf(System.currentTimeMillis()));
        boolean success = userDao.addUser(newUser);

        if (success) {
            System.out.println("-> New user successfully added!");
        } else {
            System.out.println("-> Failed to add new user. Username or email might already exist.");
        }
    }

    private void handleUpdateUserRoleAndStatus(User currentUser) {
        System.out.println("\n--- UPDATE USER ROLE AND STATUS ---");

        List<User> allUsers = userDao.getAllUsers();
        if (allUsers.isEmpty()) {
            System.out.println("No users found in the system.");
            return;
        }

        // Display compact table matching the requested style
        System.out.println("+-----+-------------------+----------+------------+");
        System.out.printf("| %-3s | %-17s | %-8s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println("+-----+-------------------+----------+------------+");
        for (User u : allUsers) {
            System.out.printf("| %-3d | %-17s | %-8s | %-10s |\n",
                    u.user_id(),
                    u.username(),
                    u.role(),
                    u.status()
            );
        }
        System.out.println("+-----+-------------------+----------+------------+");

        int targetUserId = -1;
        User targetUser = null;

        while (true) {
            System.out.print("Enter User ID to update (0 to cancel): ");
            String input = sc.nextLine().trim();
            try {
                targetUserId = Integer.parseInt(input);
                if (targetUserId == 0) return;

                // Find user object
                targetUser = null;
                for (User u : allUsers) {
                    if (u.user_id() == targetUserId) {
                        targetUser = u;
                        break;
                    }
                }

                if (targetUser == null) {
                    System.out.println("User ID not found. Please try again.");
                    continue;
                }

                // Self-Edit Restriction
                if (targetUserId == currentUser.user_id()) {
                    System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        String newRole = readValidRole();
        if (newRole.equalsIgnoreCase("CANCEL")) return;

        String newStatus = readValidStatus();
        if (newStatus.equalsIgnoreCase("CANCEL")) return;

        boolean success = userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus);
        if (success) {
            System.out.println("User '" + targetUser.username() + "' (ID " + targetUserId + ") updated successfully: role = " + newRole.toUpperCase() + ", status = " + newStatus.toUpperCase() + ".");
        } else {
            System.out.println("-> Failed to update user.");
        }
    }

    private void handleDeleteUser(User currentUser) {
        handleViewAllUsers();

        System.out.print("\nEnter Target User ID to delete (or type 0 to cancel): ");
        int targetUserId = readIntInput("");
        if (targetUserId == 0) return;

        // Self-Deletion Restriction
        if (targetUserId == currentUser.user_id()) {
            System.out.println("-> WARNING: Deleting the currently active admin account is restricted.");
            return;
        }

        System.out.print("Are you sure you want to delete User ID " + targetUserId + "? (y/n): ");
        String confirmation = sc.nextLine().trim().toLowerCase();
        if (!confirmation.equals("y")) {
            System.out.println("-> Deletion cancelled.");
            return;
        }

        boolean success = userDao.deleteUser(targetUserId);
        if (success) {
            System.out.println("-> User account successfully deleted!");
        } else {
            System.out.println("-> Failed to delete user. User ID may not exist.");
        }
    }

    // --- Input Validation Wrappers ---

    private int readIntInput(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) {
                System.out.print(prompt);
            }
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.println("Valid roles: ADMIN, STAFF, STUDENT");
            System.out.print("Enter new role (0 to cancel): ");
            String role = sc.nextLine().trim().toUpperCase();
            if (role.equals("0")) {
                return "CANCEL";
            }
            if (role.equals("ADMIN") || role.equals("STAFF") || role.equals("STUDENT")) {
                return role;
            }
            System.out.println("Invalid role. Please try again.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.println("Valid statuses: APPROVED, PENDING");
            System.out.print("Enter new status (0 to cancel): ");
            String status = sc.nextLine().trim().toUpperCase();
            if (status.equals("0")) {
                return "CANCEL";
            }
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("Invalid status. Please try again.");
        }
    }
}