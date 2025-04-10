package tn.esprit.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// RoleEnum.java
public enum RoleEnum {
    USER(Collections.emptySet()),
    ADMIN(
            Set.of(
                    Permission.ADMIN_READ,
                    Permission.ADMIN_UPDATE,
                    Permission.ADMIN_CREATE,
                    Permission.ADMIN_DELETE,
                    Permission.PET_OWNER_READ,
                    Permission.PET_OWNER_UPDATE,
                    Permission.PET_OWNER_DELETE,
                    Permission.PET_OWNER_CREATE
            )
    ),
    PET_OWNER(
            Set.of(
                    Permission.PET_OWNER_READ,
                    Permission.PET_OWNER_UPDATE,
                    Permission.PET_OWNER_CREATE,
                    Permission.PET_OWNER_DELETE
            )
    ),
    VETERINARIAN(
            Set.of(
                    Permission.VETERINARIAN_READ,
                    Permission.VETERINARIAN_UPDATE,
                    Permission.VETERINARIAN_CREATE,
                    Permission.VETERINARIAN_DELETE
            )
    ),
    SERVICE_PROVIDER(
            Set.of(
                    Permission.SERVICE_PROVIDER_READ,
                    Permission.SERVICE_PROVIDER_UPDATE,
                    Permission.SERVICE_PROVIDER_CREATE,
                    Permission.SERVICE_PROVIDER_DELETE
            )
    );

    @Getter
    private final Set<Permission> permissions;

    RoleEnum(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    RoleEnum() {
        this.permissions = Collections.emptySet();
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }

    @JsonCreator
    public static RoleEnum fromString(String value) {
        try {
            return RoleEnum.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle invalid role values (return null or default role)
            return null; // or return RoleEnum.USER;
        }
    }
}