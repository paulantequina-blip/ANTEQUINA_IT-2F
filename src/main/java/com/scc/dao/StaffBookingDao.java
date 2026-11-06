package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class StaffBookingDao {

    /**
     * Displays all bookings in the system without filtering.
     * @param heading The title header for the output table.
     * @return The number of bookings displayed.
     */
    public int displayBookings(String heading) {
        return displayBookings(heading, new String[0]);
    }

    /**
     * Displays bookings filtered by specific status states.
     * @param heading The title header for the output table.
     * @param statuses Variable arguments containing allowed status filters.
     * @return The number of bookings displayed.
     */
    public int displayBookings(String heading, String... statuses) {
        StringBuilder sql = new StringBuilder(
                "SELECT b.booking_id, b.user_id, u.full_name, " +
                        "b.space_id, s.space_name, b.booking_date, b.start_time, b.end_time, b.status " +
                        "FROM bookings b " +
                        "JOIN users u ON b.user_id = u.user_id " +
                        "JOIN study_spaces s ON b.space_id = s.space_id"
        );

        if (statuses != null && statuses.length > 0) {
            sql.append(" WHERE b.status IN (");
            for (int i = 0; i < statuses.length; i++) {
                sql.append("?");
                if (i < statuses.length - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");
        }

        sql.append(" ORDER BY b.booking_date ASC, b.start_time ASC");

        int count = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (statuses != null) {
                for (int i = 0; i < statuses.length; i++) {
                    ps.setString(i + 1, statuses[i]);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n============================================ " + heading.toUpperCase() + " ============================================");
                System.out.printf("%-10s %-22s %-18s %-12s %-10s %-10s %-12s%n",
                        "Booking ID", "Student Name", "Space Name", "Date", "Start", "End", "Status");
                System.out.println("-".repeat(104));

                while (rs.next()) {
                    count++;
                    String studentName = rs.getString("full_name");
                    System.out.printf("%-10d %-22s %-18s %-12s %-10s %-10s %-12s%n",
                            rs.getInt("booking_id"),
                            studentName != null ? studentName : "Unknown",
                            rs.getString("space_name"),
                            rs.getString("booking_date"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getString("status"));
                }

                if (count == 0) {
                    System.out.println("No records found.");
                }
                System.out.println("-".repeat(104));
            }

        } catch (SQLException e) {
            System.out.println("Error displaying bookings: " + e.getMessage());
        }

        return count;
    }

    /**
     * Changes the status of a booking and logs the activity.
     */
    public boolean changeStatus(int staffId, int bookingId, String newStatus, String actionCode, String... allowedFrom) {
        String verifySql = "SELECT status FROM bookings WHERE booking_id = ?";
        String currentStatus = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(verifySql)) {

            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentStatus = rs.getString("status");
                } else {
                    System.out.println("Error: Booking ID #" + bookingId + " not found.");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error verifying booking: " + e.getMessage());
            return false;
        }

        if (allowedFrom != null && allowedFrom.length > 0) {
            boolean statusAllowed = Arrays.asList(allowedFrom).contains(currentStatus);
            if (!statusAllowed) {
                System.out.println("Action failed: Booking #" + bookingId + " is currently '" + currentStatus +
                        "' and cannot be transitioned to '" + newStatus + "'.");
                return false;
            }
        }

        String updateSql = "UPDATE bookings SET status = ? WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, bookingId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try {
                    ActivityLogDao logDao = new ActivityLogDao(conn);
                    logDao.logAction(staffId, actionCode, "Changed booking #" + bookingId + " status from " + currentStatus + " to " + newStatus);
                } catch (Exception logEx) {
                    System.out.println("(Notice: Activity log entry failed - " + logEx.getMessage() + ")");
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Database error updating status: " + e.getMessage());
        }

        return false;
    }
}