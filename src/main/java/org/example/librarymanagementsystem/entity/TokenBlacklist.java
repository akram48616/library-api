package org.example.librarymanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true,length=1000,nullable = false)
    private String token;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private LocalDateTime blacklistedAt;
    @Column(nullable = false)
    private LocalDateTime tokenExpiresAt;

}
