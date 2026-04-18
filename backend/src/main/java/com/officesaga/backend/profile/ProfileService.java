package com.officesaga.backend.profile;

import com.officesaga.backend.auth.AuthenticatedUser;
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

        return new ProfileResponse(
                authenticatedUser.userId(),
                profile.getDisplayName(),
                profile.getJobTitle(),
                profile.getGender(),
                profile.getBirthDate(),
                profile.getBio()
        );
    }
}
