package com.officesaga.backend.auth.dto;

public class RegisterResponse {

    private final Long userId;
    private final String email;
    private final String displayName;

    public RegisterResponse(Long userId, String email, String displayName) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }
}
