package tn.esprit.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.entity.role.RoleEnum;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {
    @NotEmpty(message = "Firstname is mandatory")
    private String firstName;

    @NotEmpty(message = "Lastname is mandatory")
    private String lastName;

    @Email(message = "Email is not well formatted")
    @NotEmpty(message = "Email is mandatory")
    private String email;

    @NotEmpty(message = "Password is mandatory")
    @Size(min = 8, message = "Password should be 8 characters long minimum")
    private String password;

    @NotNull(message = "Role is mandatory")
    private RoleEnum role;
}