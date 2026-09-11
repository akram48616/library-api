package org.example.librarymanagementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="refresh_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
     @Column(nullable = false,unique = true,length = 500)
    private String token;
    // which user owns this token
    @Column(nullable = false)
    private String username;

    // when does it expire
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // is it still valid
    @Column(nullable = false)
    private boolean revoked = false;

    // when was it created
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // check if expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    // check if valid
    public boolean isValid() {
        return !revoked && !isExpired();
    }

}
