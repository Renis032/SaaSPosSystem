package com.renko.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.boot.internal.Abstract;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Configuration
public class SecurityConfig
{
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception
    {
        return http
                // Use JWT authentication, so do not create or store HTTP sessions
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Define which endpoints require authentication or specific roles
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/super-admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/**")
                        .authenticated()

                        .anyRequest()
                        .permitAll()
                )
                // Validate JWT tokens before Spring's authentication filters run
                .addFilterBefore(
                        new JwtValidator(),
                        BasicAuthenticationFilter.class
                )
                // Disable CSRF because this REST API uses JWT instead of sessions
                .csrf(AbstractHttpConfigurer::disable)
                // Apply the cors rules
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource)
                )
                // Build and return the configured security filter chain
                .build();
    }

    // Configure which frontend origins are allowed to access this REST API.
    private CorsConfigurationSource corsConfigurationSource()
    {
        return new CorsConfigurationSource()
        {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request)
            {
                CorsConfiguration config = new CorsConfiguration();
                // Allow requests only from the specified frontend application
                config.setAllowedOrigins(List.of("http://localhost:8080"));
                // Allow all HTTP methods (GET, POST, PUT, DELETE)
                config.setAllowedMethods(Collections.singletonList("*"));
                // Allow cookies or authentication credentials to be included
                config.setAllowCredentials(true);
                // Accept all request headers
                config.setAllowedHeaders(Collections.singletonList("*"));
                // Expose the Authorization header so the frontend can read it
                config.setExposedHeaders(List.of("Authorization"));
                // Cache the cors preflight response for one hour
                config.setMaxAge(3600L);

                return config;
            }
        };
    }
}
