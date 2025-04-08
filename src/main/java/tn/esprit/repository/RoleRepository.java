package tn.esprit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.entity.Role;
import tn.esprit.entity.RoleEnum;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);

    boolean existsByName(RoleEnum name);}
