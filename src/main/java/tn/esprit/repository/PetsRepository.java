package tn.esprit.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.entity.pets.Pets;

import java.util.List;

public interface PetsRepository extends JpaRepository<Pets, Long> {
    List<Pets> findByForAdoptionTrue();
    // In PetsRepository.java

    @Query(value = "SELECT * FROM pets WHERE for_adoption = true ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Pets> findRandomPets(@Param("limit") int limit);
}