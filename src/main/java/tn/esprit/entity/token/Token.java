package tn.esprit.entity.token;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.entity.token.TokenTypes;
import tn.esprit.entity.user.User;

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

    private String token;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime validatedAt;

    @Column(nullable = false)
    private boolean revoked; // Soft-delete flag

    private boolean isActive; // Only relevant for ACTIVATION tokens (true = account activated)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenTypes tokenType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}

