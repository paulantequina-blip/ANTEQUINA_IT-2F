package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UserDao {

    public boolean registerUser(String fullName, String email, String username, String password, String role, String studentId){

        String sql = "INSERT INTO users(full_name, email, username, password, role, student_id, status, date) VALUES(?, ?, ?, ?, ?, ?, 'PENDING', ?)";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, username);
            pstmt.setString(4, password);
            pstmt.setString(5, role.toUpperCase());
            pstmt.setString(6, studentId);
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            pstmt.executeUpdate();
            System.out.println("-> Successful Registration!, Waiting for Approval");
            return true;
        } catch (SQLException e) {
            System.err.println("Failed: "+ e.getMessage());
            return false;
        }

    }
}