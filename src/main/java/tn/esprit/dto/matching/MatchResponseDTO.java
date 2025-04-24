package tn.esprit.dto.matching;

import lombok.Data;

import java.util.List;

@Data
public class MatchResponseDTO {
    private List<Match> matches;

    @Data
    public static class Match {
        private String pet_name;
        private String species;
        private int match_score;
        private List<String> reasons;
        private String consideration;
    }
}