
package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class Databaseinitializer {

    private static final String URL = "jdbc:sqlite:studyspacebooking.db";

    public static void initializer() {

        String createTableSQL = "CREATE TABLE IF NOT EXISTS activity_logs (" +
                "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "action TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "log_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection connection = DriverManager.getConnection(URL);
             Statement stmt = connection.createStatement()) {

            stmt.execute(createTableSQL);

            System.out.println("Activity Logs table initialized successfully!");

        } catch (SQLException e) {

            System.out.println("Database initialization failed: " + e.getMessage());
        }
    }
}
