package tn.esprit.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
public class UserController {

    private final IUserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ImageController imageController;

    public UserController(IUserService userService,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          ObjectMapper objectMapper,
                          UserRepository userRepository,
                          ImageController imageController) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.imageController = imageController;
    }

    @GetMapping("/retrieve-user/{userId}")
    public ResponseEntity<?> retrieveUser(@PathVariable Long userId) {
        try {
            User user = userService.retrieveUser(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(UserResponseDTO.fromUser(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving user");
        }
    }

    @DeleteMapping("/remove-user/{userId}")
    public ResponseEntity<?> removeUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(userService.removeUser(userId));
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
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            User existingUser = optionalUser.get();
            UserUpdateRequest updateRequest = objectMapper.readValue(userJson, UserUpdateRequest.class);

            if (updateRequest.getFirstName() != null) {
                existingUser.setFirstName(updateRequest.getFirstName());
            }
            if (updateRequest.getLastName() != null) {
                existingUser.setLastName(updateRequest.getLastName());
            }
            if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(existingUser.getEmail())) {
                if (userRepository.existsByEmail(updateRequest.getEmail())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "Email already in use"));
                }
                existingUser.setEmail(updateRequest.getEmail());
            }
            if (updateRequest.getPassword() != null) {
                existingUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
            }
            if (updateRequest.getRole() != null) {
                Role role = roleRepository.findByName(updateRequest.getRole())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + updateRequest.getRole()));
                existingUser.setRoles(Set.of(role));
            }

            if (image != null && !image.isEmpty()) {
                String imageUrl = imageController.handleImageUpload(image, existingUser.getProfileImageUrl());
                existingUser.setProfileImageUrl(imageUrl);
            }

            userRepository.save(existingUser);
            return ResponseEntity.ok(UserResponseDTO.fromUser(existingUser));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid role", "details", e.getMessage()));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid JSON", "details", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Update failed", "details", e.getMessage()));
        }
    }
}
