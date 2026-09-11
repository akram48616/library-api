package org.example.librarymanagementsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserDetailsConfig {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {

        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails librarian = User.builder()
                .username("librarian")
                .password(encoder.encode("lib123"))
                .roles("LIBRARIAN")
                .build();

        UserDetails user = User.builder()
                .username("akram")
                .password(encoder.encode("akram123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, librarian, user);
    }
}