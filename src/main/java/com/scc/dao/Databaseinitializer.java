
package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Databaseinitializer {

    public static void initializer() {

        String createActivityLogs = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                action TEXT NOT NULL,
                description TEXT NOT NULL,
                log_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;

        String createStudySpaces = """
            CREATE TABLE IF NOT EXISTS study_spaces (
                space_id INTEGER PRIMARY KEY AUTOINCREMENT,
                space_name TEXT NOT NULL,
                space_type TEXT NOT NULL,
                capacity INTEGER NOT NULL,
                location TEXT,
                has_outlet INTEGER DEFAULT 0,
                status TEXT NOT NULL DEFAULT 'AVAILABLE'
            )
            """;

        String createBookings = """
            CREATE TABLE IF NOT EXISTS bookings (
                booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                space_id INTEGER NOT NULL,
                booking_date TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
                date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                FOREIGN KEY (space_id)
                    REFERENCES study_spaces(space_id),

                CHECK (start_time < end_time)
            )
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON");

            stmt.execute(createActivityLogs);
            stmt.execute(createStudySpaces);
            stmt.execute(createBookings);

            // Insert sample study spaces only if the table is empty.
            stmt.execute("""
                INSERT INTO study_spaces
                    (space_name, space_type, capacity, location, has_outlet, status)
                SELECT 'Study Room 1', 'PRIVATE ROOM', 2,
                       'Library - 1st Floor', 1, 'AVAILABLE'
                WHERE NOT EXISTS (
                    SELECT 1 FROM study_spaces
                )
                """);

            stmt.execute("""
                INSERT INTO study_spaces
                    (space_name, space_type, capacity, location, has_outlet, status)
                SELECT 'Discussion Room 1', 'DISCUSSION ROOM', 6,
                       'Library - 2nd Floor', 1, 'AVAILABLE'
                WHERE (SELECT COUNT(*) FROM study_spaces) = 1
                """);

            stmt.execute("""
                INSERT INTO study_spaces
                    (space_name, space_type, capacity, location, has_outlet, status)
                SELECT 'Computer Area 1', 'COMPUTER SPACE', 10,
                       'Computer Laboratory', 1, 'AVAILABLE'
                WHERE (SELECT COUNT(*) FROM study_spaces) = 2
                """);

            System.out.println("Database initialized successfully!");


        } catch (SQLException e) {
            System.out.println(
                    "Database initialization failed: " + e.getMessage()
            );
        }
    }
}
