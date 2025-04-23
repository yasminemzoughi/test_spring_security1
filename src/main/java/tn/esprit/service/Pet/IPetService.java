package tn.esprit.service.Pet;

import feign.Param;
import tn.esprit.entity.pets.Pets;

import java.util.List;

public interface IPetService {
    List<Pets> getAllPets();
    Pets getPetsById(Long id);
    Pets createPets(Pets pets);
    Pets updatePets(Long id, Pets equipe);
    void deletePets(Long id);
    void generateAndStorePetEmbedding(Long petId, String description); // Changed from user to pet
    Pets updatePetDescription(Long petId, String description);

    List<Pets> findRandomPets(@Param("limit") int limit);
}

