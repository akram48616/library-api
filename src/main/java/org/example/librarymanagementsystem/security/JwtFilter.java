package org.example.librarymanagementsystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.Service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader =
                request.getHeader("Authorization");

        String username = null;
        String token = null;

        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {

            token = authHeader.substring(7);

            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                log.error("Token extraction error: {}",
                        e.getMessage());
            }
        }

        if (username != null
                && SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            // ✅ check if token is blacklisted
            if (refreshTokenService
                    .isAccessTokenBlacklisted(token)) {

                log.warn("Blacklisted token used by: {}",
                        username);

                // continue without authentication
                filterChain.doFilter(request, response);
                return;
            }

            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(username);

            if (jwtUtil.validateToken(token, userDetails)) {

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                auth.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));

                SecurityContextHolder.getContext()
                        .setAuthentication(auth);

                log.debug("Authenticated: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}