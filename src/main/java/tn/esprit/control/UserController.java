package tn.esprit.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.entity.Role;
import tn.esprit.entity.User;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.UserRepository;
import tn.esprit.service.IUserService;
import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final IUserService userService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    public UserController(IUserService userService) {
        this.userService = userService;
    }
//http://localhost:8081/user-service/user/create_user
    @PostMapping("/create_user")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            // First check if email already exists
            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "error", "User already exists",
                                "message", "A user with email '" + user.getEmail() + "' already exists"
                        ));
            }

            // Validate roles
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                return ResponseEntity.badRequest().body("At least one role is required");
            }

            Set<Role> validatedRoles = new HashSet<>();
            for (Role role : user.getRoles()) {
                if (role.getName() == null) {
                    return ResponseEntity.badRequest().body("Role name cannot be null");
                }

                Role existingRole = roleRepository.findByName(role.getName())
                        .orElseThrow(() -> new RuntimeException("Role '" + role.getName() + "' does not exist"));
                validatedRoles.add(existingRole);

            }
            user.setRoles(validatedRoles);

            // Create the user (profile picture URL can be included in the request)
            User savedUser = userService.createUser(user);
            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating user: " + e.getMessage());
        }
    }

    // http://localhost:8081/user-service/user/retrieve-all-users
    @GetMapping("/retrieve-all-users")
    public List<User> getUsers() {
        return userService.retrieveAllUsers();
    }

    //http://localhost:8081/user-service/user/retrieve-user/{{user-id}}
    @GetMapping("/retrieve-user/{user-id}")
    public ResponseEntity<?> retrieveUser(@PathVariable("user-id") Long id) {
        User user = userService.retrieveUser(id);
        return (user != null) ? ResponseEntity.ok(user) : ResponseEntity.badRequest().body("User not found");
    }

    //http://localhost:8081/user-service/user/remove-user/{{user-id}}
    @DeleteMapping("/remove-user/{user-id}")
    public ResponseEntity<?> removeUser(@PathVariable("user-id") Long id) {
        return ResponseEntity.ok(userService.removeUser(id));
    }

    // http://localhost:8081/user-service/user/modify-user
    @PutMapping("/modify-user")
    public ResponseEntity<?> modifyUser(@RequestBody User user) {
        try {
            // 1. Validate required user ID
            if (user.getId() == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "User ID is required")
                );
            }

            // 2. Check if user exists
            Optional<User> existingUser = userRepository.findById(user.getId());
            if (existingUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("error", "User not found")
                );
            }

            // 3. Validate email uniqueness (if email is being changed)
            if (user.getEmail() != null &&
                    !user.getEmail().equals(existingUser.get().getEmail())) {
                if (userRepository.existsByEmail(user.getEmail())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(
                            Map.of("error", "Email already in use")
                    );
                }
            }

            // 4. Handle roles (validate if provided, otherwise keep existing)
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                Set<Role> validatedRoles = new HashSet<>();
                for (Role role : user.getRoles()) {
                    Role existingRole = roleRepository.findByName(role.getName())
                            .orElseThrow(() -> new RuntimeException("Invalid role: " + role.getName())); // Fix the error

                    validatedRoles.add(existingRole);
                }
                user.setRoles(validatedRoles);
            } else {
                // Keep existing roles if none provided
                user.setRoles(existingUser.get().getRoles());
            }

            // 5. Save changes
            User updatedUser = userService.modifyUser(user);
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Update failed", "details", e.getMessage())
            );
        }
    }

}
