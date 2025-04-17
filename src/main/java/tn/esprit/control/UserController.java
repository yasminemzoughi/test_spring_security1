package tn.esprit.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.dto.user.UserResponseDTO;
import tn.esprit.dto.user.UserUpdateRequest;
import tn.esprit.entity.Role;
import tn.esprit.entity.User;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.UserRepository;
import tn.esprit.service.IUserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/user")
public class UserController {

    private static final long MAX_IMAGE_SIZE = 5_242_880; // 5MB
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/user/uploads/";
    private static final String IMAGE_URL_PREFIX = "/api/user/images/";

    private final IUserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public UserController(IUserService userService,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          ObjectMapper objectMapper,
                          UserRepository userRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
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

            // Parse JSON string to UserUpdateRequest
            UserUpdateRequest updateRequest = objectMapper.readValue(userJson, UserUpdateRequest.class);

            // Check if email already exists (excluding the current user's email)

            // Update fields only if new value is provided
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
                existingUser.setEmail(updateRequest.getEmail());}

            if (updateRequest.getPassword() != null) {
                existingUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
            }

            // Update role only if it's provided
            if (updateRequest.getRole() != null) {
                Role role = roleRepository.findByName(updateRequest.getRole())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + updateRequest.getRole()));
                existingUser.setRoles(Set.of(role));
            }

            // Handle image upload if image provided
            if (image != null && !image.isEmpty()) {
                String imageUrl = handleImageUpload(image, existingUser.getProfileImageUrl());
                existingUser.setProfileImageUrl(imageUrl);
            }
            userRepository.save(existingUser); // Save the updated user to the database

            // Save updated user
            UserResponseDTO updatedUserResponse = UserResponseDTO.fromUser(existingUser);
            return ResponseEntity.ok(updatedUserResponse);


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


    private String handleImageUpload(MultipartFile image, String currentImageUrl) throws IOException {
        validateImageFile(image);

        // Delete old image if exists
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            deleteOldImage(currentImageUrl);
        }

        String fileName = saveImageFile(image);
        return IMAGE_URL_PREFIX + fileName;
    }

    // Add logging for debugging
    private String saveImageFile(MultipartFile image) throws IOException {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID() + fileExtension;
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(fileName).normalize();

        if (!filePath.getParent().equals(uploadPath)) {
            throw new IOException("Invalid file path");
        }

        image.transferTo(filePath);
        System.out.println("Image saved to: " + filePath.toString());
        return fileName;
    }

    private void validateImageFile(MultipartFile image) {
        if (!image.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed (5MB)");
        }
    }

    private void deleteOldImage(String imageUrl) throws IOException {
        String oldFileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        Path oldImagePath = Paths.get(UPLOAD_DIR).resolve(oldFileName);
        Files.deleteIfExists(oldImagePath);
    }



}