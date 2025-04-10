package tn.esprit.service;



import org.springframework.http.ResponseEntity;
import tn.esprit.dto.UserUpdateRequest;
import tn.esprit.entity.User;

import java.util.List;


public interface IUserService {
    List<User> retrieveAllUsers();
    User retrieveUser(Long id);
    User createUser(User user);
   // String removeUser(Long id);
    ResponseEntity<?> removeUser(Long id);

    User updateUser(User user); // Keep this for backward compatibility
    User updateUser(Long userId, UserUpdateRequest updateRequest); // Add the new method
    boolean emailExists(String email);
}