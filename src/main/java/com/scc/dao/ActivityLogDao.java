
package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    private final Connection connection;

    public ActivityLogDao(Connection connection) {
        this.connection = connection;
    }

    // SAVE LOGIN, LOGOUT AND OTHER ACTIVITIES
    public void logAction(int userId, String action,
                          String description) throws SQLException {

        String sql = """
                INSERT INTO activitylogs
                (user_id, action, description, log_timestamp)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, description);
            ps.executeUpdate();
        }
    }

    // GET ALL LOGS
    public List<ActivityLog> getAllLogs() throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT * FROM activitylogs
                ORDER BY log_id DESC
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                logs.add(new ActivityLog(
                        rs.getInt("log_id"),
                        rs.getInt("user_id"),
                        rs.getString("action"),
                        rs.getString("description"),
                        rs.getTimestamp("log_timestamp")
                ));
            }
        }

        return logs;
    }

    // GET LOGS BY USER ID
    public List<ActivityLog> getLogsByUserId(int userId)
            throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT * FROM activitylogs
                WHERE user_id = ?
                ORDER BY log_id DESC
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(new ActivityLog(
                            rs.getInt("log_id"),
                            rs.getInt("user_id"),
                            rs.getString("action"),
                            rs.getString("description"),
                            rs.getTimestamp("log_timestamp")
                    ));
                }
            }
        }

        return logs;
    }

    // COUNT LOGS
    public int getTotalLogs() throws SQLException {

        String sql = "SELECT COUNT(*) FROM activitylogs";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
