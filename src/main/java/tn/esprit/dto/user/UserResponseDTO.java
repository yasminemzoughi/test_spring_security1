package tn.esprit.dto.user;

import lombok.Data;
import tn.esprit.entity.user.User;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String bio;

    private Set<String> roles;
    private boolean enabled;
    private String profileImageUrl;

    // Static conversion method
    public static UserResponseDTO fromUser(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setBio(user.getBio());  // Correct setter method for bio

        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        return dto;
    }
}
