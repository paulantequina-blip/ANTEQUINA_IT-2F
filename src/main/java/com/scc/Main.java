
package com.scc;

import com.scc.dao.Databaseinitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // Tinanggal na ang mga lumang print statements sa itaas

        // Initialize the database tables
        Databaseinitializer.initializer();

        // Create UserDao
        UserDao userDao = new UserDao();

        // ... sunod na ang iba pang codes

        // Create Scanner
        Scanner scanner = new Scanner(System.in);

        // Start Authentication
        Authentication authentication =
                new Authentication(userDao, scanner);

        authentication.start();

        // Close Scanner after logout/exit
        scanner.close();

        System.out.println("\n==========================================================");
        System.out.println(               "STUDY SPACE BOOKING SYSTEM CLOSED");
        System.out.println("=======-----------------------===============================");
    }
}
