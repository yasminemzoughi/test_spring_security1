package tn.esprit.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.entity.Role;
import tn.esprit.entity.User;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import tn.esprit.service.IUserService;
@NoArgsConstructor
@AllArgsConstructor
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;


    @Override
    public List<User> retrieveAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User retrieveUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }


    public User createUser(User user) {
        Set<Role> validRoles = new HashSet<>();

        for (Role role : user.getRoles()) {
            Role existingRole = roleRepository.findByName(role.getName())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + role.getName())); // Handle missing role

            validRoles.add(existingRole);
        }

        user.setRoles(validRoles);
        return userRepository.save(user);
    }


    @Override
    public String removeUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return "User deleted successfully.";
        }
        return "User not found.";
    }

    @Override
    public User modifyUser(User user) {
        return userRepository.save(user);
    }






}
