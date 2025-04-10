package tn.esprit.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.dto.UserUpdateRequest;
import tn.esprit.entity.Role;
import tn.esprit.entity.RoleEnum;
import tn.esprit.entity.User;
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
    public User retrieveUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        Set<Role> validRoles = validateAndGetRoles(user.getRoles());
        user.setRoles(validRoles);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public String removeUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.delete(user);
        return "User deleted successfully";
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UserUpdateRequest updateRequest) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        updateUserFields(existingUser, updateRequest);
        return userRepository.save(existingUser);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    private void updateUserFields(User user, UserUpdateRequest updateRequest) {
        // Basic info
        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }

        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }

        // Email with uniqueness check
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (emailExists(updateRequest.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(updateRequest.getEmail());
        }

        // Password encoding
        if (updateRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        // Role update (single role)
        if (updateRequest.getRole() != null) {
            Role role = roleRepository.findByName(updateRequest.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + updateRequest.getRole()));
            user.setRoles(Set.of(role));
        }
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