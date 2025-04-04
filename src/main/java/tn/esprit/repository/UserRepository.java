package tn.esprit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.entity.Role;
import tn.esprit.entity.RoleEnum;
import tn.esprit.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
