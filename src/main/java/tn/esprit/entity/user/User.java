package tn.esprit.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // Profile Info
    private String profileImageUrl;
    private boolean isAdopting = true;
    @Column(columnDefinition = "TEXT")
    private String adoptionPreferences;

    // Security Tokens
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Token> tokens = new ArrayList<>();

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
}