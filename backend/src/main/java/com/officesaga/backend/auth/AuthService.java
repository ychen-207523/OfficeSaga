package com.officesaga.backend.auth;

import com.officesaga.backend.auth.dto.LoginRequest;
import com.officesaga.backend.auth.dto.LoginResult;
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
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String token = jwtService.generateToken(user);

        return new LoginResult(
                user.getId(),
                user.getEmail(),
                token
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
