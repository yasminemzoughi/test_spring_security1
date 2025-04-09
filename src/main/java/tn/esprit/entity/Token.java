package tn.esprit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime validatedAt;
    @Column(nullable = false)
    private boolean revoked;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)

    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}

