package tn.esprit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.entity.Role;
import tn.esprit.entity.RoleEnum;
import tn.esprit.repository.RoleRepository;

import java.util.List;


@Service
public class RoleServiceImpl implements IRoleService {
    @Autowired
    private RoleRepository roleRepository;


    @Override
    public Role getRoleByName(RoleEnum roleEnum) {
        return roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Role '" + roleEnum + "' not found"));
    }

    @Override
    public List<Role> retrieveAllRoles() {
        return roleRepository.findAll();
    }
    @Override
    public Role addRole(Role role) {
        return roleRepository.save(role);
    }
    @Override
    public Role retrieveRole(Long id) {
        return roleRepository.findById(id).get();
    }

    @Override
    public String removeRole(Long id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return "Role deleted successfully.";
        }
        return "Role not found.";
    }

    @Override
    public Role modifyRole(Role role) {
        return roleRepository.save(role);
    }

}
