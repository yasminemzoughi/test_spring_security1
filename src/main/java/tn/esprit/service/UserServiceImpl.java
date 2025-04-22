package tn.esprit.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.entity.role.Role;
import tn.esprit.entity.user.User;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> retrieveAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User retrieveUser(Long id) {
        return userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public User createUser(User user) {
        Set<Role> validRoles = validateAndGetRoles(user.getRoles());
        user.setRoles(validRoles);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public ResponseEntity<?> removeUser(Long id) {
        // Check if user exists
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // Perform the deletion
        userRepository.delete(user);
        return ResponseEntity.ok().body("User deleted successfully");
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        // Get the current user from database to compare emails
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Only validate email if it's being changed
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (emailExists(user.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
        }

        return userRepository.save(user);
    }
    @Override
    @Transactional
    public User updateUserProfileImage(Long userId, String imageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setProfileImageUrl(imageUrl);
        return userRepository.save(user);
    }
    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }


    private Set<Role> validateAndGetRoles(Set<Role> roles) {
        Set<Role> validRoles = new HashSet<>();
        for (Role role : roles) {
            Role existingRole = roleRepository.findByName(role.getName())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + role.getName()));
            validRoles.add(existingRole);
        }
        return validRoles;
    }


}