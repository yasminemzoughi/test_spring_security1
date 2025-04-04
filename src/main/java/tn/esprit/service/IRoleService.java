package tn.esprit.service;

import tn.esprit.entity.Role;
import tn.esprit.entity.RoleEnum;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.UserRepository;

import java.util.List;

public interface IRoleService {
    Role getRoleByName(RoleEnum roleEnum);

    List<Role> retrieveAllRoles();
    Role addRole(Role role);

    Role retrieveRole(Long id);
    String removeRole(Long id);

    Role modifyRole(Role role);

}
