package com.habittracker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/user")
    public ResponseEntity<?> getUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();
                Map<String, Object> userDetails = new HashMap<>();
                userDetails.put("name", oauth2User.getAttribute("name"));
                userDetails.put("picture", oauth2User.getAttribute("picture"));
                userDetails.put("email", oauth2User.getAttribute("email"));
                userDetails.put("authenticated", true);

                logger.debug("User details retrieved successfully for: {}", Optional.ofNullable(oauth2User.getAttribute("email")));
                return ResponseEntity.ok(userDetails);
            }

            logger.debug("No authenticated user found");
            return ResponseEntity.ok(Map.of("authenticated", false));
        } catch (Exception e) {
            logger.error("Error getting user details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error retrieving user details"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            logger.info("Processing logout request");

            // Get current authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Perform logout using SecurityContextLogoutHandler
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
                logger.info("Logout successful for user: {}", auth.getName());
            }

            // Invalidate session if exists
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                logger.debug("Session invalidated");
            }

            // Clean up cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    cookie.setSecure(true);
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);
                }
                logger.debug("Cookies cleared");
            }

            // Clear security context
            SecurityContextHolder.clearContext();
            logger.debug("Security context cleared");

            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "Logged out successfully",
                            "status", "success"
                    ));

        } catch (Exception e) {
            logger.error("Error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Logout failed",
                            "message", e.getMessage()
                    ));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuthStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                auth.getPrincipal() instanceof OAuth2User;

        return ResponseEntity.ok(Map.of("authenticated", isAuthenticated));
    }
}