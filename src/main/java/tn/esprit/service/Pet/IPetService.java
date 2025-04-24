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

    Pets updatePetDescription(Long petId, String description);

    public List<Pets> findRandomPets(int limit);

    List<Pets> getAllPetsForAdoption();

}