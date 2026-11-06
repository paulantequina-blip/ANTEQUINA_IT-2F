
package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:studyspacebooking.db";

    public static Connection getConnection() throws SQLException {

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
