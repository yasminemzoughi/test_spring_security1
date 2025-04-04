package tn.esprit.backend_pi.service;



import tn.esprit.entity.User;

import java.util.List;


public interface IUserService {
    List<User> retrieveAllUsers();
    User retrieveUser(Long id);
    User createUser(User user);

    String removeUser(Long id);

    User modifyUser(User user);

}
