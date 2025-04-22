package tn.esprit.dto.user;

import lombok.Getter;
import lombok.Setter;
import tn.esprit.entity.role.RoleEnum;

@Getter
@Setter
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private RoleEnum role;
    private String profileImageUrl;

}