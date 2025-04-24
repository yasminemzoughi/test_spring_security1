package tn.esprit.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tn.esprit.dto.matching.MatchRequestDTO;
import tn.esprit.entity.role.Role;
import tn.esprit.entity.user.User;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;
import java.util.HashMap;


import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Email update logic
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (emailExists(user.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            existingUser.setEmail(user.getEmail());
        }

        // Update other fields
        if (user.getFirstName() != null) {
            existingUser.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            existingUser.setLastName(user.getLastName());
        }
        if (user.getBio() != null) {
            existingUser.setBio(user.getBio());
        }
        if (user.getProfileImageUrl() != null) {
            existingUser.setProfileImageUrl(user.getProfileImageUrl());
        }

        return userRepository.save(existingUser);
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
    @Override
    @Transactional
    public User updateUserBio(Long userId, String bio) {
        if (bio != null && bio.length() > 1000) {
            throw new IllegalArgumentException("Bio exceeds maximum length of 1000 characters");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!Objects.equals(bio, user.getBio())) {
            user.setBio(bio); // this allows null or ""
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateAdoptionPreferences(Long userId, MatchRequestDTO.UserProfile preferencesDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Create a map of preferences
        Map<String, String> preferencesMap = new HashMap<>();
        preferencesMap.put("lifestyle", preferencesDTO.getLifestyle());
        preferencesMap.put("experience", preferencesDTO.getExperience());
        preferencesMap.put("living_space", preferencesDTO.getLiving_space()); // Changed from livingSpace to living_space
        preferencesMap.put("preferences", preferencesDTO.getPreferences());

        // Convert map to JSON and update user
        try {
            String jsonPreferences = objectMapper.writeValueAsString(preferencesMap);
            user.setAdoptionPreferences(jsonPreferences);
            return userRepository.save(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing adoption preferences", e);
        }
    }


    public Map<String, String> getAdoptionPreferences(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // No check on user.isAdopting()
            try {
                // Return an empty map if the user doesn't have adoption preferences
                if (user.getAdoptionPreferences() == null || user.getAdoptionPreferences().isEmpty()) {
                    return new HashMap<>();
                }

                // Convert the JSON string back to a Map
                return objectMapper.readValue(user.getAdoptionPreferences(), new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error retrieving adoption preferences", e);
            }
        } else {
            throw new RuntimeException("User not found with id " + userId);
        }
    }

}