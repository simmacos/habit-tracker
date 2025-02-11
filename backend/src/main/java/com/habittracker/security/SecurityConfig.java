package com.habittracker.security;

import java.util.Arrays;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.habittracker.model.User;
import com.habittracker.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserService userService;

    @Value("${app.frontend.url}")
    private String frontendUrl;


    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            try {
                User user = userService.findByEmail(username);
                if (user == null || !user.isActive()) {
                    logger.warn("User not found or not active for email: {}", username);
                    throw new UsernameNotFoundException("User not found or not active");
                }

                return org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password("") // Password vuota per OAuth2
                        .authorities("ROLE_USER")
                        .accountExpired(!user.isActive())
                        .credentialsExpired(false)
                        .disabled(!user.isActive())
                        .accountLocked(!user.isActive())
                        .build();
            } catch (Exception e) {
                logger.error("Error loading user by email: {}", username, e);
                throw new UsernameNotFoundException("Error loading user", e);
            }
        };
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security with frontend URL: {}", frontendUrl);
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/api/public/**", "/login/**", "/oauth2/**", "/api/auth/**").permitAll()
                    .anyRequest().authenticated();
                log.info("Configured authorization rules");
            })
            .oauth2Login(oauth2 -> {
                oauth2.successHandler((request, response, authentication) -> {
                    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                    String email = oauth2User.getAttribute("email");
                    log.info("OAuth2 login successful for user: {}", email);
                    
                    // CORS headers
                    response.setHeader("Access-Control-Allow-Origin", frontendUrl);
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    
                    log.info("Redirecting to frontend: {}", frontendUrl);
                    response.sendRedirect(frontendUrl);
                });
            });

        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User user = delegate.loadUser(request);
            logger.info("OAuth2 user loaded successfully");
            return user;
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList(frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        log.info("Configured CORS with allowed origin: {}", frontendUrl);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}