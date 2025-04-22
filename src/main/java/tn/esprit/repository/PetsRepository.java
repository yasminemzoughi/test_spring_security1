package tn.esprit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.entity.pets.Pets;

import java.util.List;

public interface PetsRepository extends JpaRepository<Pets, Long> {
    List<Pets> findByForAdoptionTrue();
}