
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

            System.out.println("\n================================");
            System.out.println("   STUDY SPACE BOOKING SYSTEM");
            System.out.println("================================");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.println("[3] Exit");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    login();
                    break;

                case "2":
                    System.out.println(
                            "Connect your existing registration method here."
                    );
                    break;

                case "3":
                    running = false;
                    System.out.println("Thank you for using the system!");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void login() {

        try {
            System.out.println("\n========== LOGIN ==========");

            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            User user = userDao.login(username, password);

            if (user == null) {
                System.out.println("Invalid username or password.");
                return;
            }

            if (!"APPROVED".equalsIgnoreCase(user.status())) {
                System.out.println("Your account is not approved.");
                return;
            }

            // SAVE SUCCESSFUL LOGIN
            try (Connection connection =
                         DatabaseConnection.getConnection()) {

                ActivityLogDao logDao =
                        new ActivityLogDao(connection);

                logDao.logAction(
                        user.user_id(),
                        "LOGIN",
                        user.username() + " logged in successfully"
                );
            }

            System.out.println("Login successful!");

            // OPEN ADMIN MENU
            if ("ADMIN".equalsIgnoreCase(user.role())) {

                try (Connection connection =
                             DatabaseConnection.getConnection()) {

                    ActivityLogDao logDao =
                            new ActivityLogDao(connection);

                    AdminMenu adminMenu = new AdminMenu(
                            userDao,
                            logDao,
                            scanner
                    );

                    adminMenu.display(user);
                }

            } else if ("STUDENT".equalsIgnoreCase(user.role())) {

                System.out.println("Student dashboard is not connected yet.");

                // Connect your existing StudentMenu here.

            } else if ("STAFF".equalsIgnoreCase(user.role())
                    || "LIBRARIAN".equalsIgnoreCase(user.role())) {

                System.out.println("Staff dashboard is not connected yet.");

                // Connect your existing StaffMenu here.

            } else {
                System.out.println("Unknown user role.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }
    }
}
