package com.officesaga.backend.auth.dto;

public class LoginResponse {

    private final Long userId;
    private final String email;
    private final String message;

    public LoginResponse(Long userId, String email, String message) {
        this.userId = userId;
        this.email = email;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }
}
