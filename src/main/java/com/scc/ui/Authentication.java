package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.DatabaseConnection;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n========================================");
            System.out.println("       STUDY SPACE BOOKING SYSTEM");
            System.out.println("========================================");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.println("[3] Exit");
            System.out.println("========================================");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    login();
                    break;
                case "2":
                    register();
                    break;
                case "3":
                    running = false;
                    System.out.println("\nThank you for using the system!");
                    break;
                default:
                    System.out.println("\nInvalid choice. Please try again.");
                    break;
            }
        }
    }

    private void login() {
        try {
            System.out.println("\n========================================");
            System.out.println("                  LOGIN");
            System.out.println("========================================");

            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Username and password are required.");
                return;
            }

            User user = userDao.login(username, password);

            if (user == null) {
                System.out.println("\nInvalid username or password.");
                return;
            }

            if (!"APPROVED".equalsIgnoreCase(user.status())) {
                System.out.println("\nYour account is not approved.");
                System.out.println("Please contact the administrator.");
                return;
            }

            System.out.println("\nLogin successful!");
            System.out.println("Welcome, " + user.fullName());

            recordActivity(user.user_id(), "LOGIN", user.username() + " logged in successfully");

            openDashboard(user);

            recordActivity(user.user_id(), "LOGOUT", user.username() + " logged out successfully");

            System.out.println("\nYou have successfully logged out.");

        } catch (SQLException e) {
            System.out.println("\nDatabase error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nLogin error: " + e.getMessage());
        }
    }

    private void openDashboard(User user) throws SQLException {
        String role = user.role().trim().toUpperCase();

        switch (role) {
            case "STUDENT":
                System.out.println("\nOpening Student Dashboard...");
                StudentMenu studentMenu = new StudentMenu(scanner);
                studentMenu.display(user);
                break;

            case "STAFF":
            case "LIBRARIAN":
            case "FRONT DESK STAFF":
                System.out.println("\nOpening Staff Dashboard...");
                StaffMenu staffMenu = new StaffMenu(scanner);
                staffMenu.display(user);
                break;

            case "ADMIN":
                System.out.println("\nOpening Admin Dashboard...");
                try (Connection connection = DatabaseConnection.getConnection()) {
                    ActivityLogDao logDao = new ActivityLogDao(connection);
                    AdminMenu adminMenu = new AdminMenu(userDao, logDao, scanner);
                    adminMenu.display(user);
                }
                break;

            default:
                System.out.println("\nUnknown user role: " + role);
                System.out.println("Please contact the administrator.");
                break;
        }
    }

    private void recordActivity(int userId, String action, String description) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            ActivityLogDao logDao = new ActivityLogDao(connection);
            logDao.logAction(userId, action, description);
        }
    }

    private void register() {
        System.out.println("\n========================================");
        System.out.println("               REGISTRATION");
        System.out.println("========================================");

        // 1. full_name
        System.out.print("Enter Full Name: ");
        String fullName = scanner.nextLine().trim();

        // 2. email
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();

        // 3. username
        System.out.print("Enter new username: ");
        String username = scanner.nextLine().trim();

        // 4. password
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        // 5. role
        System.out.print("Select role [1] ADMIN [2] STUDENT [3] STAFF: ");
        String roleChoice = scanner.nextLine().trim();

        String role = "STUDENT"; // Default
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
                System.out.println("Invalid choice. Defaulting role to STUDENT.");
        }

        // 6. student_id
        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine().trim();

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || studentId.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        try {
            // Status ay "PENDING" at ang date ay ise-set ng database o SQL query
            boolean success = userDao.registerUser(fullName, email, username, password, role, studentId, "PENDING");

            if (success) {
                System.out.println("-> Successful Registration!, Waiting for Approval");
            } else {
                System.out.println("\nRegistration failed. Username or student ID may already exist.");
            }

        } catch (Exception e) {
            System.out.println("\nRegistration error: " + e.getMessage());
        }
    }
}