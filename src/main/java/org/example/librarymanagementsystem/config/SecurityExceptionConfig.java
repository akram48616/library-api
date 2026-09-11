package org.example.librarymanagementsystem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.librarymanagementsystem.exception.ErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;

@Configuration
public class SecurityExceptionConfig {

    private final ObjectMapper objectMapper =
            new ObjectMapper().findAndRegisterModules();

    // ─── 401 Unauthorized ──────────────────────────
    // called when user is NOT authenticated
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                AuthenticationException ex) -> {

            response.setStatus(401);
            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse error = ErrorResponse.builder()
                    .status(401)
                    .error("Unauthorized")
                    .message("Please login to access this resource")
                    .path(request.getRequestURI())
                    .timestamp(LocalDateTime.now())
                    .build();

            response.getWriter().write(
                    objectMapper.writeValueAsString(error));
        };
    }

    // ─── 403 Forbidden ─────────────────────────────
    // called when user IS authenticated but lacks permission
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                AccessDeniedException ex) -> {

            response.setStatus(403);
            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse error = ErrorResponse.builder()
                    .status(403)
                    .error("Forbidden")
                    .message("You don't have permission " +
                            "to access this resource")
                    .path(request.getRequestURI())
                    .timestamp(LocalDateTime.now())
                    .build();

            response.getWriter().write(
                    objectMapper.writeValueAsString(error));
        };
    }
}