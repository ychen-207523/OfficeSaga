package com.officesaga.backend.auth;

import com.officesaga.backend.profile.Profile;
import com.officesaga.backend.profile.ProfileRepository;
import com.officesaga.backend.user.User;
import com.officesaga.backend.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "security.jwt.secret=test_secret_key_that_is_long_enough_1234567890"
})
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ProfileRepository profileRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    @Test
    void loginShouldSetAuthCookie() throws Exception {
        User user = createUser(1L, "test@example.com", "hashed-password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("auth_token"))
                .andExpect(cookie().value("auth_token", "jwt-token"))
                .andExpect(cookie().httpOnly("auth_token", true))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("Login successful."));
    }

    @Test
    void currentUserShouldReturnAuthenticatedUserFromCookie() throws Exception {
        User user = createUser(1L, "test@example.com", "hashed-password");

        when(jwtService.isTokenInvalid("jwt-token")).thenReturn(false);
        when(jwtService.extractUserId("jwt-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("jwt-token", user)).thenReturn(true);

        mockMvc.perform(get("/api/me")
                        .cookie(new Cookie("auth_token", "jwt-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void currentUserShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void currentUserProfileShouldReturnProfileForAuthenticatedUser() throws Exception {
        User user = createUser(1L, "test@example.com", "hashed-password");
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setDisplayName("Test User");
        profile.setJobTitle("Developer");
        profile.setGender("Female");
        profile.setBio("Enjoys building tools for teams.");

        when(jwtService.isTokenInvalid("jwt-token")).thenReturn(false);
        when(jwtService.extractUserId("jwt-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("jwt-token", user)).thenReturn(true);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/api/me/profile")
                        .cookie(new Cookie("auth_token", "jwt-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.displayName").value("Test User"))
                .andExpect(jsonPath("$.jobTitle").value("Developer"))
                .andExpect(jsonPath("$.gender").value("Female"))
                .andExpect(jsonPath("$.bio").value("Enjoys building tools for teams."));
    }

    @Test
    void logoutShouldClearAuthCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("auth_token"))
                .andExpect(cookie().value("auth_token", ""));
    }

    private User createUser(Long id, String email, String passwordHash) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        setUserId(user, id);
        return user;
    }

    private void setUserId(User user, Long id) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }
}
