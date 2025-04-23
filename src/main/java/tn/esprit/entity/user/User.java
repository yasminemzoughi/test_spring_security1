package tn.esprit.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.esprit.entity.role.Role;
import tn.esprit.entity.token.Token;

import java.util.*;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(length = 1000)
    private String bio;

    // Roles and Permissions
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // AI Embedding Storage
    @Column(columnDefinition = "TEXT")
    @Convert(converter = FloatArrayConverter.class)
    private float[] bioEmbedding;

    // Security Methods
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().name()));
            role.getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                    .forEach(authorities::add);
        }
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // FloatArrayConverter as a static inner class
    @Converter
    public static class FloatArrayConverter implements AttributeConverter<float[], String> {
        private static final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(float[] attribute) {
            try {
                return attribute != null ? mapper.writeValueAsString(attribute) : null;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert float array to JSON", e);
            }
        }

        @Override
        public float[] convertToEntityAttribute(String dbData) {
            try {
                return dbData != null ? mapper.readValue(dbData, float[].class) : null;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert JSON to float array", e);
            }
        }
    }
}