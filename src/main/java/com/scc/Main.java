package com.scc;

import com.scc.dao.Databaseinitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Study Space Booking System...");

        Databaseinitializer.initializer();


        UserDao userDao = new UserDao();
        Scanner scanner = new Scanner(System.in);


        Authentication auth = new Authentication(userDao, scanner);
        auth.start();
        scanner.close();
    }
}