package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Eksaktong nakaturo sa iyong studyspacebooking database
    private static final String URL = "jdbc:sqlite:studyspacebooking.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}