package tn.esprit.service.matching;

import tn.esprit.dto.matching.MatchResponseDTO;

import java.util.List;

public interface IMatchingService {
    MatchResponseDTO matchPetsToUser(Long userId, Integer topN);
  //  public List<Long> getMatchedPetIdsForUser(Long userId, Integer topN) ;

}