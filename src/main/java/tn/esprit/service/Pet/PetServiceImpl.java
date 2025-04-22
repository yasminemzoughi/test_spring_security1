package tn.esprit.service.Pet;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.entity.pets.Pets;
import tn.esprit.repository.PetsRepository;

import java.util.List;

@Getter
@Setter
@Service
public class PetServiceImpl implements IPetService {

    @Autowired
    private PetsRepository petsRepository;

    @Override
    public List<Pets> getAllEquipes() {
        return petsRepository.findAll();
    }

    @Override
    public Pets getPetsById(Long id) {
        return petsRepository.findById(id).orElse(null);
    }

    @Override
    public Pets createPets(Pets pets) {
        return petsRepository.save(pets);
    }

    @Override
    public Pets updatePets(Long id, Pets updatedPet) {
        Pets existingPet = petsRepository.findById(id).orElse(null);
        if (existingPet != null) {
            // Update properties from the updatedPet
            existingPet.setName(updatedPet.getName());
            existingPet.setSpecies(updatedPet.getSpecies());
            existingPet.setAge(updatedPet.getAge());
            existingPet.setColor(updatedPet.getColor());
            existingPet.setSex(updatedPet.getSex());
            existingPet.setForAdoption(updatedPet.isForAdoption());
            existingPet.setLocation(updatedPet.getLocation());
            existingPet.setDescription(updatedPet.getDescription());
            existingPet.setImagePath(updatedPet.getImagePath());
            existingPet.setOwnerId(updatedPet.getOwnerId());
            existingPet.setEmbedding(updatedPet.getEmbeddingAsFloats());
            existingPet.setSimilarityScore(updatedPet.getSimilarityScore());

            return petsRepository.save(existingPet);
        }
        return null;
    }

    @Override
    public void deletePets(Long id) {
        petsRepository.deleteById(id);
    }
}
