package com.scc.model;

public record User(int user_id, String fullName, String email, String username, String password, String role, String studentId, String status, String date) {
    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Status: %s - Date: %s", user_id, username, role, status, date);
    }
}