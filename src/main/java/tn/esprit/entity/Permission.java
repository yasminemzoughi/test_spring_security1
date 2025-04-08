package tn.esprit.entity;

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
    PET_OWNER_DELETE("pet_owner:delete")

    ;

    @Getter
    private final String permission;
}
