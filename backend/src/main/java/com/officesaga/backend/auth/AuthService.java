package com.officesaga.backend.auth;

import com.officesaga.backend.auth.dto.RegisterRequest;
import com.officesaga.backend.auth.dto.RegisterResponse;
import com.officesaga.backend.profile.Profile;
import com.officesaga.backend.profile.ProfileRepository;
import com.officesaga.backend.user.User;
import com.officesaga.backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        Profile profile = new Profile();
        profile.setUser(savedUser);
        profile.setDisplayName(request.getDisplayName().trim());
        profile.setJobTitle(normalizeOptional(request.getJobTitle()));
        profile.setGender(normalizeOptional(request.getGender()));
        profile.setBirthDate(request.getBirthDate());
        profile.setBio(normalizeOptional(request.getBio()));

        profileRepository.save(profile);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                profile.getDisplayName()
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
