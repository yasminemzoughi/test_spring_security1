package tn.esprit.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.dto.user.AdoptionPreferencesDTO;
import tn.esprit.dto.user.UserResponseDTO;
import tn.esprit.dto.user.UserUpdateRequest;
import tn.esprit.entity.role.Role;
import tn.esprit.entity.user.User;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.UserRepository;
import tn.esprit.service.IUserService;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {

    private final IUserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ImageController imageController;

    @GetMapping("/retrieve-user/{userId}")
    public ResponseEntity<?> retrieveUser(@PathVariable Long userId) {
        try {
            User user = userService.retrieveUser(userId);
            return ResponseEntity.ok(UserResponseDTO.fromUser(user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving user");
        }
    }

    @DeleteMapping("/remove-user/{userId}")
    public ResponseEntity<?> removeUser(@PathVariable Long userId) {
        try {
            return userService.removeUser(userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user");
        }
    }

    @PutMapping(value = "/modify-user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> modifyUser(
            @PathVariable Long userId,
            @RequestPart("user") String userJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            User existingUser = userService.retrieveUser(userId);
            UserUpdateRequest updateRequest = objectMapper.readValue(userJson, UserUpdateRequest.class);

            // Update basic fields
            if (updateRequest.getFirstName() != null) {
                existingUser.setFirstName(updateRequest.getFirstName());
            }
            if (updateRequest.getLastName() != null) {
                existingUser.setLastName(updateRequest.getLastName());
            }

            // Handle email update
            if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(existingUser.getEmail())) {
                if (userRepository.existsByEmail(updateRequest.getEmail())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "Email already in use"));
                }
                existingUser.setEmail(updateRequest.getEmail());
            }

            // Handle password update
            if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
            }

            // Handle role update
            if (updateRequest.getRole() != null) {
                Role role = roleRepository.findByName(updateRequest.getRole())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + updateRequest.getRole()));
                existingUser.setRoles(Set.of(role));
            }

            // Handle bio update
            if (updateRequest.getBio() != null) {
                existingUser = userService.updateUserBio(userId, updateRequest.getBio());
            }
            // Handle image update
            if (image != null && !image.isEmpty()) {
                String imageUrl = imageController.handleImageUpload(image, existingUser.getProfileImageUrl());
                existingUser.setProfileImageUrl(imageUrl);
            }

            // Save final changes
            User updatedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(UserResponseDTO.fromUser(updatedUser));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid JSON format"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Update failed: " + e.getMessage()));
        }
    }

    // Endpoint to update adoption preferences
    @PostMapping("/{userId}/adoptionPreferences")
    public User updateAdoptionPreferences(@PathVariable Long userId,
                                          @RequestBody AdoptionPreferencesDTO preferencesDTO) {
        return userService.updateAdoptionPreferences(userId, preferencesDTO);
    }


    // Endpoint to get adoption preferences
    @GetMapping("/{userId}/adoptionPreferences")
    public Map<String, String> getAdoptionPreferences(@PathVariable Long userId) {
        return userService.getAdoptionPreferences(userId);
    }

}