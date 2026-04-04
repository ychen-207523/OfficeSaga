package com.officesaga.backend.auth;

import com.officesaga.backend.auth.dto.LoginRequest;
import com.officesaga.backend.auth.dto.LoginResponse;
import com.officesaga.backend.auth.dto.RegisterRequest;
import com.officesaga.backend.auth.dto.RegisterResponse;
import com.officesaga.backend.profile.Profile;
import com.officesaga.backend.profile.ProfileRepository;
import com.officesaga.backend.user.User;
import com.officesaga.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldCreateUserAndProfile() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("  Test@Example.com ");
        request.setPassword("password123");
        request.setDisplayName("  Test User ");
        request.setJobTitle(" Developer ");
        request.setGender(" Female ");
        request.setBirthDate(LocalDate.of(1998, 5, 10));
        request.setBio(" Enjoys building tools for teams. ");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            setUserId(user, 1L);
            return user;
        });

        RegisterResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);

        verify(userRepository).save(userCaptor.capture());
        verify(profileRepository).save(profileCaptor.capture());

        User savedUser = userCaptor.getValue();
        Profile savedProfile = profileCaptor.getValue();

        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("hashed-password", savedUser.getPasswordHash());
        assertEquals("Test User", savedProfile.getDisplayName());
        assertEquals("Developer", savedProfile.getJobTitle());
        assertEquals("Female", savedProfile.getGender());
        assertEquals(LocalDate.of(1998, 5, 10), savedProfile.getBirthDate());
        assertEquals("Enjoys building tools for teams.", savedProfile.getBio());
        assertEquals(1L, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getDisplayName());
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setDisplayName("Test User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals("Email is already in use.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void registerShouldConvertBlankOptionalFieldsToNull() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setDisplayName("Test User");
        request.setJobTitle("   ");
        request.setGender("   ");
        request.setBio("   ");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            setUserId(user, 2L);
            return user;
        });

        authService.register(request);

        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(profileCaptor.capture());

        Profile savedProfile = profileCaptor.getValue();
        assertNull(savedProfile.getJobTitle());
        assertNull(savedProfile.getGender());
        assertNull(savedProfile.getBio());
    }

    @Test
    void loginShouldReturnUserInfoWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("  test@example.com ");
        request.setPassword("password123");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed-password");
        setUserId(user, 3L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals(3L, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void loginShouldRejectUnknownEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password.", exception.getMessage());
    }

    @Test
    void loginShouldRejectWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong-password");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed-password");
        setUserId(user, 4L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password.", exception.getMessage());
    }

    private void setUserId(User user, Long id) {
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }
}
