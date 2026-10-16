package com.scc.model;

import java.time.LocalDateTime;

public record User(int id, String fullName, String email, String username, String password, String roles, String studentId, String status, LocalDateTime date) {
    @Override
    public String toString(){
        return String.format("[%d] %s (%s) - Status: %s - Date: %s", id, username, roles, status, date);
    }
}