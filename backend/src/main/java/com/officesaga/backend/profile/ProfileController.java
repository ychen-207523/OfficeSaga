package com.officesaga.backend.profile;

import com.officesaga.backend.auth.AuthenticatedUser;
import com.officesaga.backend.profile.dto.ProfileResponse;
import com.officesaga.backend.profile.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public ProfileResponse getCurrentUserProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return profileService.getCurrentUserProfile(authenticatedUser);
    }

    @PutMapping("/profile")
    public ProfileResponse updateCurrentUserProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return profileService.updateCurrentUserProfile(authenticatedUser, request);
    }
}
