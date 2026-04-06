package com.officesaga.backend.auth;

public class ApiErrorResponse {

    private final String message;

    public ApiErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
