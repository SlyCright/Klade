package site.klade.webapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO of the persisted generation (tree: generation -&gt; species -&gt; specimens).
 * Pure data carrier — all entity&lt;-&gt;DTO mapping logic lives in the
 * {@link site.klade.webapp.converter converter} package.
 */
@Setter
@Getter
public class GenerationDto {

    private Long id;

    private Integer generationNumber;

    private List<SpeciesDto> species = new ArrayList<>();

    // Default constructor for Jackson
    public GenerationDto() {
    }

    public GenerationDto(Long id, Integer generationNumber, List<SpeciesDto> species) {
        this.id = id;
        this.generationNumber = generationNumber;
        this.species = species;
    }
}
