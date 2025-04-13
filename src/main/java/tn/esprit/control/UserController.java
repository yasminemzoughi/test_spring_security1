package tn.esprit.control;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.dto.UserUpdateRequest;
import tn.esprit.entity.Role;
import tn.esprit.entity.User;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.UserRepository;
import tn.esprit.service.IUserService;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/user")
//@PreAuthorize("hasRole('PET_OWNER') and hasRole('ADMIN')")
public class UserController {

    private final IUserService userService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(IUserService userService,
                          RoleRepository roleRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }



   // @PreAuthorize("hasAnyAuthority('pet_owner:read', 'admin:read')")
    @GetMapping("/retrieve-user/{user-id}")
    public ResponseEntity<?> retrieveUser(@PathVariable("user-id") Long id) {
        User user = userService.retrieveUser(id);
        return (user != null) ? ResponseEntity.ok(user) : ResponseEntity.badRequest().body("User not found");
    }

   // @PreAuthorize("hasAnyAuthority('pet_owner:delete', 'admin:delete')")
    @DeleteMapping("/remove-user/{user-id}")
    public ResponseEntity<?> removeUser(@PathVariable("user-id") Long id) {
        return ResponseEntity.ok(userService.removeUser(id));
    }


 //   @PreAuthorize("hasAnyRole('PET_OWNER', 'ADMIN')")
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
