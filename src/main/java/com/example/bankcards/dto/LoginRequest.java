package com.example.bankcards.dto;

public class LoginRequest {
    private String username;
    private String password;

    // Гетары
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // Сеторы
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}