package com.officesaga.backend.user.dto;

public class CurrentUserResponse {

    private final Long userId;
    private final String email;

    public CurrentUserResponse(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
