package tn.esprit.control;


import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.entity.Role;

import tn.esprit.repository.RoleRepository;
import tn.esprit.service.IRoleService;

import java.util.List;


import java.util.Map;

@RestController
@RequestMapping("/role")
public class RoleController {

    private  IRoleService roleService;
    @Autowired
    private RoleRepository roleRepository;


    public RoleController(IRoleService roleService) {
        this.roleService = roleService;
    }
// get all
    // http://localhost:8089/Backend/role/retrieve-all-roles
    @GetMapping("/retrieve-all-roles")
    public List<Role> getRoles() {
        return roleService.retrieveAllRoles();
    }

// create role
    @PostMapping("/create_role")
    public ResponseEntity<Map<String, String>> createRole(@RequestBody Role role) {
        // Validate if the role name exists in RoleEnum
        // Check if the role already exists
        if (roleRepository.findByName(role.getName()) != null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Role already exists",
                    "message", "The role '" + role.getName() + "' is already in use"
            ));
        }
        // Save and return success response
        Role savedRole = roleRepository.save(role);
        return ResponseEntity.ok(Map.of(
                "message", "Role created successfully",
                "roleId", savedRole.getRoleId().toString()
        ));
    }
// delete role
    //http://localhost:8089/Backend/role/remove-role/role-id}
    @DeleteMapping("/remove-role/{role-id}")
    public String removeRole(@PathVariable("role-id") Long id) {
        return roleService.removeRole(id);
    }

//update role
    // http://localhost:8089/Backend/role/modify-role
    @PutMapping("/modify-role")
    public ResponseEntity<?> modifyRole(@RequestBody Role role) {
        if (role.getRoleId() == null) {
            return ResponseEntity.badRequest().body("Role ID is required for update.");
        }
        return ResponseEntity.ok(roleService.modifyRole(role));
    }

    //get role by id
    // http://localhost:8089/Backend/role/retrieve-role/{role-id}
    @GetMapping("/retrieve-role/{role-id}")
    public Role retrieveRole(@PathVariable("role-id") Long id) {
        return roleService.retrieveRole(id);
    }


}
