package com.scc.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String DB_FILE = "studyspacebooking.db";
    private static final String DB_URL  = "jdbc:sqlite:" + DB_FILE;

    private static boolean printed = false;

    public static Connection getConnection() throws SQLException {

        // Shows which file the program really uses.
        // Open THIS exact file in your SQL viewer.
        if (!printed) {
            System.out.println("[DB] Using: "
                    + new File(DB_FILE).getAbsolutePath()
                    + (new File(DB_FILE).exists() ? "" : "   <-- FILE NOT FOUND, an empty one will be created!"));
            printed = true;
        }

        Connection connection = DriverManager.getConnection(DB_URL);

        try (Statement statement = connection.createStatement()) {

            // Enable SQLite foreign keys
            statement.execute("PRAGMA foreign_keys = ON;");

            // Create activity logs table
            String createTable =
                    "CREATE TABLE IF NOT EXISTS activitylogs (" +
                            "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "user_id INTEGER NOT NULL, " +
                            "action TEXT NOT NULL, " +
                            "description TEXT, " +
                            "log_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                            ");";

            statement.execute(createTable);
        }

        return connection;
    }
}