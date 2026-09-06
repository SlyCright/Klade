package site.klade.webapp.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO of the persisted specimen. Pure data carrier — all entity&lt;-&gt;DTO mapping
 * logic lives in the {@link site.klade.webapp.converter converter} package.
 */
@Setter
@Getter
public class SpecimenDto {

    private Long id;

    private Long speciesId;

    /**
     * Fitness semantics: LOWER = BETTER (fitness is the distance to the arena center).
     */
    private Double fitness;

    private String genome;

    // Default constructor for Jackson
    public SpecimenDto() {
    }

    public SpecimenDto(Long id, Long speciesId, Double fitness, String genome) {
        this.id = id;
        this.speciesId = speciesId;
        this.fitness = fitness;
        this.genome = genome;
    }
}
