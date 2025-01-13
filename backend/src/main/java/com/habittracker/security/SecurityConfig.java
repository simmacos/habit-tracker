package com.habittracker.security;

import com.habittracker.model.User;
import com.habittracker.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Environment environment;

    @Value("${app.frontend.url:http://127.0.0.1:5500}")
    private String frontendUrl;

    @Value("${app.domain.url:https://haby.casacocchy.duckdns.org}")
    private String domainUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**", "/error", "/login", "/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> {
                    oauth2.userInfoEndpoint(userInfo -> userInfo
                            .userService(oauth2UserService())
                    );
                    oauth2.successHandler(new AuthenticationSuccessHandler() {
                        @Override
                        public void onAuthenticationSuccess(HttpServletRequest request,
                                                            HttpServletResponse response,
                                                            Authentication authentication) throws IOException, ServletException {
                            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                            String googleId = oauth2User.getAttribute("sub");
                            String email = oauth2User.getAttribute("email");
                            String name = oauth2User.getAttribute("name");
                            String pictureUrl = oauth2User.getAttribute("picture");

                            logger.info("Processing OAuth2 login for user: {}", email);

                            try {
                                User savedUser = userService.findOrCreateUser(googleId, email, name, pictureUrl);
                                logger.info("User processed successfully: {}", savedUser.getEmail());

                                // Determina l'URL di redirect in base all'origine
                                String referer = request.getHeader("Referer");
                                String redirectUrl;
                                if (referer != null && (referer.contains("localhost") || referer.contains("127.0.0.1"))) {
                                    redirectUrl = frontendUrl;
                                } else {
                                    redirectUrl = domainUrl;
                                }

                                logger.info("Redirecting to: {}", redirectUrl);
                                response.sendRedirect(redirectUrl);
                            } catch (Exception e) {
                                logger.error("Error processing OAuth2 user: ", e);
                                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                        "Error during authentication");
                            }
                        }
                    });
                })
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                        })
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                );

        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User user = delegate.loadUser(request);
            logger.info("OAuth2 user loaded: {}", Optional.ofNullable(user.getAttribute("email")));
            return user;
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Configura gli origins permessi
        List<String> allowedOrigins = Arrays.asList(
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "https://haby.casacocchy.duckdns.org"
        );
        configuration.setAllowedOrigins(allowedOrigins);

        // Configura i metodi HTTP permessi
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));

        // Configura gli headers permessi
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Configura gli headers esposti
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        // Abilita le credenziali
        configuration.setAllowCredentials(true);

        // Imposta il tempo di cache per le risposte pre-flight
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}