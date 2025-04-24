package tn.esprit.service.user;



import org.springframework.http.ResponseEntity;
import tn.esprit.dto.matching.MatchRequestDTO;
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
     User updateAdoptionPreferences(Long userId, MatchRequestDTO.UserProfile preferencesDTO) ;
     Map<String, String> getAdoptionPreferences(Long userId) ;

    }