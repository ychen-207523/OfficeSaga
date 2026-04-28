package com.officesaga.backend.profile;

import com.officesaga.backend.auth.AuthenticatedUser;
import com.officesaga.backend.profile.dto.UpdateProfileRequest;
import com.officesaga.backend.profile.dto.ProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getCurrentUserProfile(AuthenticatedUser authenticatedUser) {
        Profile profile = profileRepository.findByUserId(authenticatedUser.userId())
                .orElseThrow(() -> new IllegalArgumentException("Profile not found."));

        return toProfileResponse(authenticatedUser.userId(), profile);
    }

    @Transactional
    public ProfileResponse updateCurrentUserProfile(
            AuthenticatedUser authenticatedUser,
            UpdateProfileRequest request
    ) {
        Profile profile = profileRepository.findByUserId(authenticatedUser.userId())
                .orElseThrow(() -> new IllegalArgumentException("Profile not found."));

        profile.setDisplayName(request.getDisplayName().trim());
        profile.setJobTitle(normalizeOptional(request.getJobTitle()));
        profile.setGender(normalizeOptional(request.getGender()));
        profile.setBirthDate(request.getBirthDate());
        profile.setBio(normalizeOptional(request.getBio()));

        return toProfileResponse(authenticatedUser.userId(), profile);
    }

    private ProfileResponse toProfileResponse(Long userId, Profile profile) {
        return new ProfileResponse(
                userId,
                profile.getDisplayName(),
                profile.getJobTitle(),
                profile.getGender(),
                profile.getBirthDate(),
                profile.getBio()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
