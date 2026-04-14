package com.officesaga.backend.auth;

import com.officesaga.backend.auth.dto.LoginRequest;
import com.officesaga.backend.auth.dto.LoginResult;
import com.officesaga.backend.auth.dto.LoginResponse;
import com.officesaga.backend.auth.dto.RegisterRequest;
import com.officesaga.backend.auth.dto.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final String jwtCookieName;
    private final long jwtExpirationSeconds;
    private final boolean jwtCookieSecure;

    public AuthController(
            AuthService authService,
            @Value("${security.jwt.cookie-name:auth_token}") String jwtCookieName,
            @Value("${security.jwt.expiration-seconds:3600}") long jwtExpirationSeconds,
            @Value("${security.jwt.cookie-secure:false}") boolean jwtCookieSecure
    ) {
        this.authService = authService;
        this.jwtCookieName = jwtCookieName;
        this.jwtExpirationSeconds = jwtExpirationSeconds;
        this.jwtCookieSecure = jwtCookieSecure;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult loginResult = authService.login(request);

        ResponseCookie authCookie = ResponseCookie.from(jwtCookieName, loginResult.getToken())
                .httpOnly(true)
                .secure(jwtCookieSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(jwtExpirationSeconds)
                .build();

        LoginResponse response = new LoginResponse(
                loginResult.getUserId(),
                loginResult.getEmail(),
                "Login successful."
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie expiredCookie = ResponseCookie.from(jwtCookieName, "")
                .httpOnly(true)
                .secure(jwtCookieSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }
}
