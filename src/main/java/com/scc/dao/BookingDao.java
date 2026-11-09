
package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {

    // ==========================================
    // AUTOMATIC TABLE FORMATTER
    // ==========================================

    private void displayTable(String title, String[] headers,
                              List<String[]> rows) {

        int columns = headers.length;
        int[] widths = new int[columns];

        // Get the longest text in each column
        for (int i = 0; i < columns; i++) {
            widths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < columns; i++) {
                String value = row[i] == null ? "" : row[i];
                widths[i] = Math.max(widths[i], value.length());
            }
        }

        // Create horizontal border
        StringBuilder border = new StringBuilder("+");

        for (int width : widths) {
            border.append("-".repeat(width + 2)).append("+");
        }

        String line = border.toString();

        // Calculate total table width
        int totalWidth = line.length() - 2;

        System.out.println("\n" + line);

        // Display centered title
        int leftPadding = Math.max(0, (totalWidth - title.length()) / 2);
        int rightPadding = Math.max(0,
                totalWidth - title.length() - leftPadding);

        System.out.println("|"
                + " ".repeat(leftPadding)
                + title
                + " ".repeat(rightPadding)
                + "|");

        System.out.println(line);

        // Display column headings
        printTableRow(headers, widths);

        System.out.println(line);

        // Display database records
        if (rows.isEmpty()) {

            String message = "No records found.";
            int padding = Math.max(0, totalWidth - message.length());

            System.out.println("|" + message
                    + " ".repeat(padding) + "|");

        } else {

            for (String[] row : rows) {
                printTableRow(row, widths);
            }
        }

        System.out.println(line);
    }

    // ==========================================
    // PRINT ONE TABLE ROW
    // ==========================================

    private void printTableRow(String[] values, int[] widths) {

        StringBuilder output = new StringBuilder("|");

        for (int i = 0; i < widths.length; i++) {

            String value = values[i] == null ? "" : values[i];

            output.append(" ")
                    .append(String.format("%-" + widths[i] + "s", value))
                    .append(" ");

            if (i < widths.length - 1) {
                output.append("┆");
            }
        }

        output.append("|");

        System.out.println(output);
    }

    // ==========================================
    // DISPLAY ALL STUDY SPACES
    // ==========================================

    public void displayStudySpaces() {

        String sql = """
                SELECT space_id, space_name, space_type,
                       capacity, location, status
                FROM study_spaces
                ORDER BY space_id
                """;

        List<String[]> rows = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                rows.add(new String[]{
                        String.valueOf(rs.getInt("space_id")),
                        rs.getString("space_name"),
                        rs.getString("space_type"),
                        String.valueOf(rs.getInt("capacity")),
                        rs.getString("location"),
                        rs.getString("status")
                });
            }

            displayTable(
                    "AVAILABLE STUDY SPACES",
                    new String[]{
                            "ID",
                            "Name",
                            "Type",
                            "Capacity",
                            "Location",
                            "Status"
                    },
                    rows
            );

        } catch (SQLException e) {
            System.out.println("[X] Error displaying study spaces: "
                    + e.getMessage());
        }
    }

    // ==========================================
    // DISPLAY SPACE AVAILABILITY
    // VIEW ONLY
    // ==========================================

    public void displaySpaceAvailability() {

        String sql = """
                SELECT space_id, space_name, space_type,
                       location, status
                FROM study_spaces
                ORDER BY space_id
                """;

        List<String[]> rows = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                rows.add(new String[]{
                        String.valueOf(rs.getInt("space_id")),
                        rs.getString("space_name"),
                        rs.getString("space_type"),
                        rs.getString("location"),
                        rs.getString("status")
                });
            }

            displayTable(
                    "SPACE AVAILABILITY STATUS",
                    new String[]{
                            "ID",
                            "Name",
                            "Type",
                            "Location",
                            "Availability"
                    },
                    rows
            );

        } catch (SQLException e) {
            System.out.println("[X] Error checking availability: "
                    + e.getMessage());
        }
    }

    // ==========================================
    // CHECK SPACE AVAILABILITY FOR A TIME SLOT
    // ==========================================

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
                    System.out.println("[X] Study space not found.");
                    return false;
                }

                String status = rs.getString("status");

                if (status == null ||
                        !status.equalsIgnoreCase("AVAILABLE")) {

                    System.out.println(
                            "[X] This study space is unavailable.");
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
                    System.out.println(
                            "[X] This time slot is already booked.");
                    return false;
                }
            }
        }

        return true;
    }

    // ==========================================
    // CREATE BOOKING
    // ==========================================

    public boolean createBooking(
            int userId,
            int spaceId,
            String bookingDate,
            String startTime,
            String endTime) {

        try {

            if (!isSpaceAvailable(
                    spaceId,
                    bookingDate,
                    startTime,
                    endTime,
                    0)) {
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

                    System.out.println("\n+------------------------------------------+");
                    System.out.println("|       BOOKING SAVED SUCCESSFULLY!        |");
                    System.out.println("|       Status: PENDING                    |");
                    System.out.println("+------------------------------------------+");

                    return true;
                }
            }

        } catch (SQLException e) {

            System.out.println("[X] Error creating booking: "
                    + e.getMessage());
        }

        return false;
    }

    // ==========================================
    // DISPLAY UPCOMING BOOKINGS
    // ==========================================

    public void displayMyUpcomingBookings(int userId) {

        String sql = """
                SELECT b.booking_id, b.space_id, s.space_name,
                       b.booking_date, b.start_time,
                       b.end_time, b.status
                FROM bookings b
                LEFT JOIN study_spaces s
                    ON b.space_id = s.space_id
                WHERE b.user_id = ?
                  AND b.booking_date >= date('now')
                  AND b.status IN ('PENDING', 'APPROVED')
                ORDER BY b.booking_date, b.start_time
                """;

        displayStudentBookings(
                sql, userId, "MY UPCOMING BOOKINGS");
    }

    // ==========================================
    // DISPLAY BOOKING HISTORY
    // ==========================================

    public void displayMyBookings(int userId) {

        String sql = """
                SELECT b.booking_id, b.space_id, s.space_name,
                       b.booking_date, b.start_time,
                       b.end_time, b.status
                FROM bookings b
                LEFT JOIN study_spaces s
                    ON b.space_id = s.space_id
                WHERE b.user_id = ?
                ORDER BY b.booking_date DESC, b.start_time DESC
                """;

        displayStudentBookings(
                sql, userId, "MY BOOKING HISTORY");
    }

    // ==========================================
    // DISPLAY STUDENT BOOKINGS
    // ==========================================

    private void displayStudentBookings(
            String sql, int userId, String heading) {

        List<String[]> rows = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

                    rows.add(new String[]{
                            String.valueOf(rs.getInt("booking_id")),
                            String.valueOf(rs.getInt("space_id")),
                            rs.getString("space_name"),
                            rs.getString("booking_date"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getString("status")
                    });
                }
            }

            displayTable(
                    heading,
                    new String[]{
                            "Booking ID",
                            "Space ID",
                            "Space Name",
                            "Booking Date",
                            "Start Time",
                            "End Time",
                            "Status"
                    },
                    rows
            );

        } catch (SQLException e) {

            System.out.println("[X] Error displaying bookings: "
                    + e.getMessage());
        }
    }

    // ==========================================
    // UPDATE BOOKING
    // ==========================================

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
             PreparedStatement findStmt =
                     conn.prepareStatement(findSql)) {

            findStmt.setInt(1, bookingId);
            findStmt.setInt(2, userId);

            int spaceId;

            try (ResultSet rs = findStmt.executeQuery()) {

                if (!rs.next()) {
                    System.out.println(
                            "[X] Booking not found or cannot be edited.");
                    return false;
                }

                spaceId = rs.getInt("space_id");
            }

            if (!isSpaceAvailable(
                    spaceId,
                    bookingDate,
                    startTime,
                    endTime,
                    bookingId)) {
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

                boolean updated = updateStmt.executeUpdate() > 0;

                if (updated) {
                    System.out.println(
                            "[SUCCESS] Booking updated successfully.");
                }

                return updated;
            }

        } catch (SQLException e) {

            System.out.println("[X] Error updating booking: "
                    + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // CANCEL STUDENT BOOKING
    // PARAMETERS: userId, bookingId
    // ==========================================

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

                System.out.println(
                        "[SUCCESS] Booking cancelled successfully.");

                return true;
            }

            System.out.println(
                    "[X] Booking not found or cannot be cancelled.");

        } catch (SQLException e) {

            System.out.println("[X] Error cancelling booking: "
                    + e.getMessage());
        }

        return false;
    }
}
