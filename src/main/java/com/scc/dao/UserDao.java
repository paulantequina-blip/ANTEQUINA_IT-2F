package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // =====================================
    // LOGIN USER
    // =====================================
    public User login(String username, String password) {
        String query = "SELECT user_id, full_name, email, username, password, role, student_id, status, date " +
                "FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("student_id"),
                            rs.getString("status"),
                            rs.getString("date")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error during login: " + e.getMessage());
        }

        return null;
    }

    // =====================================
    // REGISTER NEW USER
    // =====================================
    public boolean registerUser(String fullName, String email, String username, String password, String role, String studentId, String status) {
        // Eksaktong pagkakasunod-sunod batay sa database schema mo
        String query = "INSERT INTO users (full_name, email, username, password, role, student_id, status, date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, username);
            pstmt.setString(4, password);
            pstmt.setString(5, role);
            pstmt.setString(6, studentId);
            pstmt.setString(7, status);
            pstmt.setLong(8, System.currentTimeMillis()); // Timestamp format para sa date

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    // =====================================
    // GET ALL USERS (Para sa Admin)
    // =====================================
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT user_id, full_name, email, username, password, role, student_id, status, date FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("student_id"),
                        rs.getString("status"),
                        rs.getString("date")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }

        return users;
    }

    // =====================================
    // UPDATE USER STATUS (Para sa Admin Approval)
    // =====================================
    public boolean updateUserStatus(int userId, String status) {
        String query = "UPDATE users SET status = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }

    // =====================================
    // UPDATE USER ROLE AND STATUS
    // =====================================
    public boolean updateUserRoleAndStatus(int userId, String role, String status) {
        StringBuilder query = new StringBuilder("UPDATE users SET ");
        boolean updateRole = role != null && !role.isEmpty();
        boolean updateStatus = status != null && !status.isEmpty();

        if (updateRole) query.append("role = ?");
        if (updateRole && updateStatus) query.append(", ");
        if (updateStatus) query.append("status = ?");
        query.append(" WHERE user_id = ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

            int index = 1;
            if (updateRole) pstmt.setString(index++, role);
            if (updateStatus) pstmt.setString(index++, status);
            pstmt.setInt(index, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    // =====================================
    // DELETE USER
    // =====================================
    public boolean deleteUser(int userId) {
        String query = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
}