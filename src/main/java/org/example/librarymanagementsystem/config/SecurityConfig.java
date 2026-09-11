package org.example.librarymanagementsystem.config;

import org.example.librarymanagementsystem.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // required for @PreAuthorize to take effect
public class SecurityConfig {
    @Autowired
    private JwtFilter jwtFilter;
    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private AccessDeniedHandler accessDeniedHandler;
    @Autowired
    private PasswordEncoder passwordEncoder;
    // ─── Authentication Manager ────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationEntryPoint authEntryPoint,
            AccessDeniedHandler accessDeniedHandler) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/api-docs.yaml",
                                "/v3/api-docs/**",
                                "/api-docs/swagger-config",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // Books
                        .requestMatchers(HttpMethod.GET, "/api/books/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")

                        // Authors
                        .requestMatchers(HttpMethod.GET, "/api/authors/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/authors/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/authors/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/authors/**").hasRole("ADMIN")

                        // Categories
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")

                        // Publishers
                        .requestMatchers(HttpMethod.GET, "/api/publishers/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/publishers/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/publishers/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/publishers/**").hasRole("ADMIN")

                        // Members
                        .requestMatchers(HttpMethod.GET, "/api/members/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/api/members/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/members/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/members/**").hasRole("ADMIN")

                        // Borrows
                        .requestMatchers(HttpMethod.GET, "/api/borrows/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/api/borrows").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/borrows/*/return").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/borrows/update-overdue").hasRole("ADMIN")

                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }
}