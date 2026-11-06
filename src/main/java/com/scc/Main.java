
package com.scc;

import com.scc.dao.Databaseinitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("Starting Study Space Booking System...");

        // Initialize database
        Databaseinitializer.initializer();

        // Initialize UserDao
        UserDao userDao = new UserDao();

        // Scanner for user input
        Scanner scanner = new Scanner(System.in);

        // Start Authentication
        Authentication auth = new Authentication(userDao, scanner);
        auth.start();

        scanner.close();

        System.out.println("Study Space Booking System Closed.");
    }
}
