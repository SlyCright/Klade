package site.klade.webapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO of the persisted species. Pure data carrier — all entity&lt;-&gt;DTO mapping
 * logic lives in the {@link site.klade.webapp.converter converter} package.
 */
@Setter
@Getter
public class SpeciesDto {

    private Long id;

    private Long generationId;

    private Integer speciesIndex;

    private Double averageFitness;

    /**
     * Fitness semantics: LOWER = BETTER (fitness is the distance to the arena
     * center). This is the minimum among evaluated specimens, or {@code null}
     * when no specimen of the species has an evaluated fitness.
     */
    private Double bestFitness;

    private List<SpecimenDto> specimens = new ArrayList<>();

    // Default constructor for Jackson
    public SpeciesDto() {
    }

    public SpeciesDto(Long id, Long generationId, Integer speciesIndex,
                      Double averageFitness, Double bestFitness, List<SpecimenDto> specimens) {
        this.id = id;
        this.generationId = generationId;
        this.speciesIndex = speciesIndex;
        this.averageFitness = averageFitness;
        this.bestFitness = bestFitness;
        this.specimens = specimens;
    }
}
