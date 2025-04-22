package tn.esprit.service.Pet;

import tn.esprit.entity.pets.Pets;

import java.util.List;

public interface IPetService {
    List<Pets> getAllEquipes();
    Pets getPetsById(Long id);
    Pets createPets(Pets pets);
    Pets updatePets(Long id, Pets equipe);
    void deletePets(Long id);
}
