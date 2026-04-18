package com.officesaga.backend.profile;

import com.officesaga.backend.auth.AuthenticatedUser;
import com.officesaga.backend.profile.dto.ProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
}
