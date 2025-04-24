package tn.esprit.service.Pet;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.entity.pets.Pets;
import tn.esprit.repository.PetsRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PetServiceImpl implements IPetService {

    @Autowired
    private PetsRepository petsRepository;


    @Override
    public List<Pets> getAllPets() {
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
       //     existingPet.setSimilarityScore(updatedPet.getSimilarityScore());

            return petsRepository.save(existingPet);
        }
        return null;
    }

    @Override
    public void deletePets(Long id) {
        petsRepository.deleteById(id);
    }

  public void generateAndStorePetEmbedding(Long petId, String description) {
//        Pets pet = petsRepository.findById(petId).orElseThrow();
//        float[] embedding = embeddingService.getEmbedding(description);
//        pet.setEmbedding(embedding);
//        petsRepository.save(pet);
 }

    @Override
    @Transactional
    public Pets updatePetDescription(Long petId, String description) {
        Pets pet = petsRepository.findById(petId)
                .orElseThrow(() -> new EntityNotFoundException("Pet not found"));
        pet.setDescription(description);

        // Generate and store embedding
        generateAndStorePetEmbedding(petId, description);


        return petsRepository.save(pet);
    }

    @Override
    public List<Pets> findRandomPets(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        return petsRepository.findRandomPets(limit);
    }
}
