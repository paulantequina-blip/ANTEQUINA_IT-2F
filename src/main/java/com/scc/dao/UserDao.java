package com.scc.dao;

import com.scc.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public UserDao() {
        // Ensures there is always an admin account matching your database
        checkAndSeedAdmin();
    }

    private void checkAndSeedAdmin() {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        String insertSql = "INSERT INTO users(full_name, email, username, password, role, student_id, status, date) VALUES('admin', 'admin', 'admin', 'admin', 'ADMIN', 'admin', 'PENDING', ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, String.valueOf(System.currentTimeMillis()));
                    pstmt.executeUpdate();
                    System.out.println("-> Default Admin created (Username: admin | Password: admin)");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking/seeding admin: " + e.getMessage());
        }
    }

    public boolean registerUser(String fullName, String email, String username, String password, String role, String studentId) {
        String sql = "INSERT INTO users(full_name, email, username, password, role, student_id, status, date) VALUES(?, ?, ?, ?, ?, ?, 'PENDING', ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, username);
            pstmt.setString(4, password);
            pstmt.setString(5, role.toUpperCase());
            pstmt.setString(6, studentId);
            pstmt.setString(7, String.valueOf(System.currentTimeMillis()));

            pstmt.executeUpdate();
            System.out.println("-> Successful Registration!, Waiting for Approval");
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to register: " + e.getMessage());
            return false;
        }
    }

    public User login(String username, String password) {
        String sql = "SELECT user_id, full_name, email, username, password, role, student_id, status, date FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    String role = rs.getString("role");

                    if ("PENDING".equalsIgnoreCase(status) && !"ADMIN".equalsIgnoreCase(role)) {
                        System.out.println("-> Account is pending, waiting for Approval");
                        return null;
                    }

                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("username"),
                            rs.getString("password"),
                            role,
                            rs.getString("student_id"),
                            status,
                            rs.getString("date")
                    );
                } else {
                    System.out.println("-> Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during Login: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, username, password, role, student_id, status, date FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                userList.add(new User(
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
            System.err.println("Error fetching all users: " + e.getMessage());
        }
        return userList;
    }

    public List<User> getPendingUsers() {
        List<User> pendingList = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, username, password, role, student_id, status, date FROM users WHERE status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                pendingList.add(new User(
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
            System.err.println("Error fetching pending users: " + e.getMessage());
        }
        return pendingList;
    }

    public boolean approveUser(int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO users(full_name, email, username, password, role, student_id, status, date) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.fullName());
            pstmt.setString(2, user.email());
            pstmt.setString(3, user.username());
            pstmt.setString(4, user.password());
            pstmt.setString(5, user.role());
            pstmt.setString(6, user.studentId());
            pstmt.setString(7, user.status());
            pstmt.setString(8, String.valueOf(System.currentTimeMillis()));

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserRoleAndStatus(int userId, String role, String status) {
        String sql = "UPDATE users SET role = ?, status = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.toUpperCase());
            pstmt.setString(2, status.toUpperCase());
            pstmt.setInt(3, userId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user role and status: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
}