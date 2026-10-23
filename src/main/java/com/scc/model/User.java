package com.scc.model;

public record User(int id, String fullName, String email, String username, String password, String role, String studentId, String status, String date) {
    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Status: %s - Date: %s", id, username, role, status, date);
    }
}