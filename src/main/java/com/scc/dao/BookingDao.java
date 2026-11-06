package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookingDao {

    // Display all study spaces with a perfectly aligned box layout
    public void displayStudySpaces() {
        String sql = "SELECT * FROM study_spaces ORDER BY space_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n╔══════╦═════════════════════════╦══════════════════╦══════╦══════════════════════╦═══════════╗");
            System.out.println("║                                 AVAILABLE STUDY SPACES                                   ║");
            System.out.println("╠══════╬═════════════════════════╬══════════════════╬══════╬══════════════════════╬═══════════╣");
            System.out.printf("║ %-4s ║ %-23s ║ %-16s ║ %-4s ║ %-20s ║ %-9s ║%n",
                    "ID", "Name", "Type", "Cap", "Location", "Status");
            System.out.println("╠══════╬═════════════════════════╬══════════════════╬══════╬══════════════════════╬═══════════╣");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("║ %-4d ║ %-23s ║ %-16s ║ %-4d ║ %-20s ║ %-9s ║%n",
                        rs.getInt("space_id"),
                        rs.getString("space_name"),
                        rs.getString("space_type"),
                        rs.getInt("capacity"),
                        rs.getString("location"),
                        rs.getString("status")
                );
            }

            if (!found) {
                System.out.println("║                       [!] No study spaces found.                                         ║");
            }
            System.out.println("╚══════╩═════════════════════════╩══════════════════╩══════╩══════════════════════╩═══════════╝");

        } catch (SQLException e) {
            System.out.println(" [X] Error displaying study spaces: " + e.getMessage());
        }
    }

    // View-only availability with a perfectly aligned box layout
    public void displaySpaceAvailability() {
        String sql = """
            SELECT space_id, space_name, space_type, location, status
            FROM study_spaces
            ORDER BY space_id
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n╔══════╦═════════════════════════╦══════════════════╦══════════════════════╦════════════════╗");
            System.out.println("║                             SPACE AVAILABILITY STATUS                                    ║");
            System.out.println("╠══════╬═════════════════════════╬══════════════════╬══════════════════════╬════════════════╣");
            System.out.printf("║ %-4s ║ %-23s ║ %-16s ║ %-20s ║ %-14s ║%n",
                    "ID", "Name", "Type", "Location", "Availability");
            System.out.println("╠══════╬═════════════════════════╬══════════════════╬══════════════════════╬════════════════╣");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("║ %-4d ║ %-23s ║ %-16s ║ %-20s ║ %-14s ║%n",
                        rs.getInt("space_id"),
                        rs.getString("space_name"),
                        rs.getString("space_type"),
                        rs.getString("location"),
                        rs.getString("status")
                );
            }

            if (!found) {
                System.out.println("║                       [!] No spaces found.                                               ║");
            }
            System.out.println("╚══════╩═════════════════════════╩══════════════════╩══════════════════════╩════════════════╝");

        } catch (SQLException e) {
            System.out.println(" [X] Error checking availability: " + e.getMessage());
        }
    }

    // Check whether a space is available for the requested time
    public boolean isSpaceAvailable(
            int spaceId,
            String bookingDate,
            String startTime,
            String endTime,
            int excludedBookingId) throws SQLException {

        String spaceSql =
                "SELECT status FROM study_spaces WHERE space_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(spaceSql)) {

            pstmt.setInt(1, spaceId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println(" [X] Study space not found.");
                    return false;
                }

                String status = rs.getString("status");

                if (status == null || !status.equalsIgnoreCase("AVAILABLE")) {
                    System.out.println(" [X] This study space is unavailable.");
                    return false;
                }
            }
        }

        String bookingSql = """
            SELECT COUNT(*)
            FROM bookings
            WHERE space_id = ?
              AND booking_date = ?
              AND status IN ('PENDING', 'APPROVED')
              AND start_time < ?
              AND end_time > ?
              AND booking_id <> ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(bookingSql)) {

            pstmt.setInt(1, spaceId);
            pstmt.setString(2, bookingDate);
            pstmt.setString(3, endTime);
            pstmt.setString(4, startTime);
            pstmt.setInt(5, excludedBookingId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println(" [X] This time slot is already booked.");
                    return false;
                }
            }
        }

        return true;
    }

    // Create and save a booking in SQLite
    public boolean createBooking(
            int userId,
            int spaceId,
            String bookingDate,
            String startTime,
            String endTime) {

        try {
            if (!isSpaceAvailable(
                    spaceId, bookingDate, startTime, endTime, 0)) {
                return false;
            }

            String sql = """
                INSERT INTO bookings
                (user_id, space_id, booking_date,
                 start_time, end_time, status)
                VALUES (?, ?, ?, ?, ?, 'PENDING')
                """;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, userId);
                pstmt.setInt(2, spaceId);
                pstmt.setString(3, bookingDate);
                pstmt.setString(4, startTime);
                pstmt.setString(5, endTime);

                int rowsInserted = pstmt.executeUpdate();

                if (rowsInserted > 0) {
                    System.out.println("\n╔════════════════════════════════════════╗");
                    System.out.println("║      BOOKING SAVED SUCCESSFULLY!       ║");
                    System.out.println("║      Status: PENDING                   ║");
                    System.out.println("╚════════════════════════════════════════╝");
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println(" [X] Error creating booking: " + e.getMessage());
        }

        return false;
    }

    // Display only the logged-in student's upcoming bookings
    public void displayMyUpcomingBookings(int userId) {
        String sql = """
            SELECT b.booking_id, b.space_id, s.space_name,
                   b.booking_date, b.start_time, b.end_time, b.status
            FROM bookings b
            LEFT JOIN study_spaces s ON b.space_id = s.space_id
            WHERE b.user_id = ?
              AND b.booking_date >= date('now')
              AND b.status IN ('PENDING', 'APPROVED')
            ORDER BY b.booking_date, b.start_time
            """;

        displayStudentBookings(sql, userId, "MY UPCOMING BOOKINGS");
    }

    // Display the logged-in student's booking history
    public void displayMyBookings(int userId) {
        String sql = """
            SELECT b.booking_id, b.space_id, s.space_name,
                   b.booking_date, b.start_time, b.end_time, b.status
            FROM bookings b
            LEFT JOIN study_spaces s ON b.space_id = s.space_id
            WHERE b.user_id = ?
            ORDER BY b.booking_date DESC, b.start_time DESC
            """;

        displayStudentBookings(sql, userId, "MY BOOKING HISTORY");
    }

    private void displayStudentBookings(
            String sql, int userId, String heading) {

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n╔══════════════════════════════════════════════════════════════════════╗");
                System.out.println("║ " + String.format("%-68s", heading) + " ║");
                System.out.println("╚══════════════════════════════════════════════════════════════════════╝");

                boolean found = false;

                while (rs.next()) {
                    found = true;
                    System.out.printf(" ID: %-3d | Space ID: %-3d | Space: %-12s | Date: %-10s | Start: %-5s | End: %-5s | Status: %s%n",
                            rs.getInt("booking_id"),
                            rs.getInt("space_id"),
                            rs.getString("space_name"),
                            rs.getString("booking_date"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getString("status")
                    );
                }

                if (!found) {
                    System.out.println(" [!] No booking records found.");
                }
                System.out.println("════════════════════════════════════════════════════════════════════════");
            }

        } catch (SQLException e) {
            System.out.println(" [X] Error displaying bookings: " + e.getMessage());
        }
    }

    // Edit the date and time of the student's own upcoming booking
    public boolean updateBooking(
            int userId,
            int bookingId,
            String bookingDate,
            String startTime,
            String endTime) {

        String findSql = """
            SELECT space_id
            FROM bookings
            WHERE booking_id = ?
              AND user_id = ?
              AND status IN ('PENDING', 'APPROVED')
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findSql)) {

            findStmt.setInt(1, bookingId);
            findStmt.setInt(2, userId);

            int spaceId;

            try (ResultSet rs = findStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println(" [X] Booking not found or cannot be edited.");
                    return false;
                }

                spaceId = rs.getInt("space_id");
            }

            if (!isSpaceAvailable(
                    spaceId, bookingDate, startTime, endTime, bookingId)) {
                return false;
            }

            String updateSql = """
                UPDATE bookings
                SET booking_date = ?,
                    start_time = ?,
                    end_time = ?
                WHERE booking_id = ?
                  AND user_id = ?
                  AND status IN ('PENDING', 'APPROVED')
                """;

            try (PreparedStatement updateStmt =
                         conn.prepareStatement(updateSql)) {

                updateStmt.setString(1, bookingDate);
                updateStmt.setString(2, startTime);
                updateStmt.setString(3, endTime);
                updateStmt.setInt(4, bookingId);
                updateStmt.setInt(5, userId);

                return updateStmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.out.println(" [X] Error updating booking: " + e.getMessage());
            return false;
        }
    }

    // Cancel only the logged-in student's own booking
    public boolean cancelBooking(int userId, int bookingId) {
        String sql = """
            UPDATE bookings
            SET status = 'CANCELLED'
            WHERE booking_id = ?
              AND user_id = ?
              AND status IN ('PENDING', 'APPROVED')
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookingId);
            pstmt.setInt(2, userId);

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println(" [✔] Booking cancelled successfully.");
                return true;
            }

            System.out.println(" [X] Booking not found or cannot be cancelled.");

        } catch (SQLException e) {
            System.out.println(" [X] Error cancelling booking: " + e.getMessage());
        }

        return false;
    }
}