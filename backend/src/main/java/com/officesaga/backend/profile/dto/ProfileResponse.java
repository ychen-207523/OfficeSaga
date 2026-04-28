package com.officesaga.backend.profile.dto;

import java.time.LocalDate;

public class ProfileResponse {

    private final Long userId;
    private final String displayName;
    private final String jobTitle;
    private final String gender;
    private final LocalDate birthDate;
    private final String bio;

    public ProfileResponse(
            Long userId,
            String displayName,
            String jobTitle,
            String gender,
            LocalDate birthDate,
            String bio
    ) {
        this.userId = userId;
        this.displayName = displayName;
        this.jobTitle = jobTitle;
        this.gender = gender;
        this.birthDate = birthDate;
        this.bio = bio;
    }

    public Long getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getGender() {
        return gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getBio() {
        return bio;
    }
}