package tn.esprit.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {


    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),
    PET_OWNER_READ("pet_owner:read"),
    PET_OWNER_UPDATE("pet_owner:update"),
    PET_OWNER_CREATE("pet_owner:create"),
    PET_OWNER_DELETE("pet_owner:delete"),

    // Added permissions for VETERINARIAN role
    VETERINARIAN_READ("veterinarian:read"),
    VETERINARIAN_UPDATE("veterinarian:update"),
    VETERINARIAN_CREATE("veterinarian:create"),
    VETERINARIAN_DELETE("veterinarian:delete"),

    // Added permissions for SERVICE_PROVIDER role
    SERVICE_PROVIDER_READ("service_provider:read"),
    SERVICE_PROVIDER_UPDATE("service_provider:update"),
    SERVICE_PROVIDER_CREATE("service_provider:create"),
    SERVICE_PROVIDER_DELETE("service_provider:delete");

    ;

    @Getter
    private final String permission;
}
