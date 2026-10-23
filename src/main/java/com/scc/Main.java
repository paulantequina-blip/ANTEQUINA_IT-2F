package com.scc;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.AdminMenu;

import java.util.Scanner;

public class Main {
    public static final Scanner sc = new Scanner(System.in);
    public static final UserDao userDao = new UserDao();

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n===============================");
            System.out.println(" Study Space Booking System ");
            System.out.println("===============================");
            System.out.println("1. Log in");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;

                case "2":
                    System.out.print("Enter Full Name: ");
                    String fullName = sc.nextLine();
                    System.out.print("Enter Email: ");
                    String email = sc.nextLine();
                    System.out.print("Enter Student ID: ");
                    String studentId = sc.nextLine();
                    System.out.print("Enter new username: ");
                    String user = sc.nextLine();
                    System.out.print("Enter Password: ");
                    String pass = sc.nextLine();

                    System.out.print("Select role [1] ADMIN [2] STUDENT [3] STAFF: ");
                    int roleChoice = sc.nextInt();
                    sc.nextLine(); // consume newline

                    String role = switch (roleChoice) {
                        case 1 -> "ADMIN";
                        case 2 -> "STUDENT";
                        case 3 -> "STAFF";
                        default -> null;
                    };

                    if (role != null) {
                        userDao.registerUser(fullName, email, user, pass, role, studentId);
                    } else {
                        System.out.println("Invalid role selection!");
                    }
                    break;

                case "3":
                    System.out.println("Exiting system. Goodbye!");
                    return;

                default:
                    System.out.println("Invalid choice. Please select 1, 2, or 3.");
            }
        }
    }

    private static void handleLogin() {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        User user = userDao.login(username, password);

        if (user != null) {
            System.out.println("\n-> Welcome " + user.username() + " | ROLE: " + user.role());
            switch (user.role().toUpperCase()) {
                case "ADMIN" -> new AdminMenu(userDao, sc).display(user);
                case "STUDENT" -> {
                    System.out.println("-> Student Dashboard (Feature under development)");
                }
                case "STAFF" -> {
                    System.out.println("-> Staff Dashboard (Feature under development)");
                }
                default -> System.out.println("-> Account role not recognized!");
            }
        }
    }
}