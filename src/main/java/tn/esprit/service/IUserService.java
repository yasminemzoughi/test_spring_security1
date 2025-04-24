package tn.esprit.service;



import org.springframework.http.ResponseEntity;
import tn.esprit.dto.user.AdoptionPreferencesDTO;
import tn.esprit.entity.user.User;

import java.util.List;
import java.util.Map;


public interface IUserService {
    List<User> retrieveAllUsers();
    User retrieveUser(Long id);
    User createUser(User user);
    ResponseEntity<?> removeUser(Long id);

    User updateUser(User user);
    boolean emailExists(String email);
    User updateUserProfileImage(Long userId, String imageUrl) ;
     User updateUserBio(Long userId, String bio);
    public User updateAdoptionPreferences(Long userId, AdoptionPreferencesDTO preferencesDTO) ;
     Map<String, String> getAdoptionPreferences(Long userId) ;

    }