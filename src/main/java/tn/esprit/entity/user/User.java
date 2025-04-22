package tn.esprit.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.esprit.entity.role.Role;
import tn.esprit.entity.token.Token;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class User implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    // Account Status
    private boolean enabled = true;
    private boolean accountLocked = false;

    // Security Tokens
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Token> tokens = new ArrayList<>();

    // Profile Info
    private String profileImageUrl;
    @Column(length = 1000) // Extended length for bio
    private String bio;

    // Roles and Permissions
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // AI Embedding Storage (Optimized)
    @Lob
    @Basic(fetch = FetchType.LAZY) // Lazy load for performance
    @Column(columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] embedding;



    // Security Methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().name()));

            role.getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                    .forEach(authorities::add);
        }

        System.out.println("User authorities: " + authorities);

        return authorities;
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return !accountLocked; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // Embedding Helpers (Thread-safe)
  public void setEmbedding(float[] floats) {
        Objects.requireNonNull(floats, "Embedding array cannot be null");
        this.embedding = floatToByteArray(floats);
    }



    @Transient
    public float[] getEmbeddingAsFloats() {
        return byteToFloatArray(this.embedding);
    }

    // Static conversion utilities
    public static byte[] floatToByteArray(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * Float.BYTES);
        buffer.asFloatBuffer().put(floats);
        return buffer.array();
    }

    public static float[] byteToFloatArray(byte[] bytes) {
        if (bytes == null) return null;
        FloatBuffer buffer = ByteBuffer.wrap(bytes).asFloatBuffer();
        float[] floats = new float[buffer.remaining()];
        buffer.get(floats);
        return floats;
    }


}