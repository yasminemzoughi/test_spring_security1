package tn.esprit.control;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.dto.user.UserUpdateRequest;
import tn.esprit.entity.Role;
import tn.esprit.entity.User;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.UserRepository;
import tn.esprit.service.IUserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")

@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final IUserService userService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(IUserService userService,
                          RoleRepository roleRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/create_user")
    @PreAuthorize("hasAnyAuthority('admin:create')")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            if (userService.emailExists(user.getEmail())) {
                return errorResponse("User exists", "Email already in use", HttpStatus.CONFLICT);
            }
            User savedUser = userService.createUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (RuntimeException e) {
            return errorResponse("Validation error", e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    private ResponseEntity<Map<String, String>> errorResponse(String error, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(Map.of("error", error, "message", message));
    }

    @PreAuthorize("hasAnyAuthority('admin:read')")
    @GetMapping("/retrieve-all-users")
    public List<User> getUsers() {
        return userService.retrieveAllUsers();
    }

    @PreAuthorize("hasAnyAuthority('admin:get')")
    @GetMapping("/retrieve-user/{user-id}")
    public ResponseEntity<?> retrieveUser(@PathVariable("user-id") Long id) {
        User user = userService.retrieveUser(id);
        return (user != null) ? ResponseEntity.ok(user) : ResponseEntity.badRequest().body("User not found");
    }

    @PreAuthorize("hasAnyAuthority('admin:delete')")
    @DeleteMapping("/remove-user/{user-id}")
    public ResponseEntity<?> removeUser(@PathVariable("user-id") Long id) {
        return ResponseEntity.ok(userService.removeUser(id));
    }

    @PreAuthorize("hasAnyAuthority('admin:update')")
    @PutMapping("/modify-user/{userId}")
    public ResponseEntity<?> modifyUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest updateRequest) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            User existingUser = optionalUser.get();

            // Update fields
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

            // Update role (single role now)
            if (updateRequest.getRole() != null) {
                Role role = roleRepository.findByName(updateRequest.getRole())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + updateRequest.getRole()));
                existingUser.setRoles(Set.of(role)); // Set with single role
            }

            User updatedUser = userService.updateUser(existingUser);
            return ResponseEntity.ok(updatedUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid role", "details", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Update failed", "details", e.getMessage()));
        }
    }


}
