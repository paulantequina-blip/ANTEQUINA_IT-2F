package com.scc;

import com.scc.dao.UserDao;

import java.util.Scanner;

public class Main {
    public static final Scanner sc = new Scanner(System.in);
    public static final UserDao userDao = new UserDao();

    public static void main(String[] args){

        System.out.println("\n===============================");
        System.out.println(" EQUIPMENT AND PENALTY RECORD ");
        System.out.println("===============================");
        System.out.println("1. Log in");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        String choice = sc.nextLine();

        switch (choice){
            case "1":
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
                String role;
                switch (roleChoice){
                    case 1:
                        role = "ADMIN";
                        break;
                    case 2:
                        role = "STUDENT";
                        break;
                    case 3:
                        role = "STAFF";
                        break;
                    default:
                        role = null;
                }

                if (role != null){
                    userDao.registerUser(fullName, email, user, pass, role, studentId);
                } else {
                    System.out.println("Invalid role selection");
                }
                break;
        }
    }
}
