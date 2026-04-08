package com.officesaga.backend.auth;

import com.officesaga.backend.user.User;
import com.officesaga.backend.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final String jwtCookieName;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository,
            @Value("${security.jwt.cookie-name:auth_token}") String jwtCookieName
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.jwtCookieName = jwtCookieName;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // If Spring already knows who the user is for this request, do nothing.
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Read the JWT from the auth cookie. If it is missing or malformed, continue as unauthenticated.
        String token = extractTokenFromCookie(request);
        if (token == null || jwtService.isTokenInvalid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // The token tells us which user it belongs to, but we still load the user from the database
        // before trusting the request.
        Long userId = jwtService.extractUserId(token);
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = userOptional.get();
        // Final verification step: the token must still be valid for this exact user.
        if (!jwtService.isTokenValid(token, user)) {
            filterChain.doFilter(request, response);
            return;
        }

        // At this point the request is authenticated, so store a lightweight principal in Spring Security.
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(user.getId(), user.getEmail());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                Collections.emptyList()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        // Find the configured auth cookie and return the JWT value inside it.
        return Arrays.stream(cookies)
                .filter(cookie -> jwtCookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
