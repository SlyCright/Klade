package site.klade.webapp.converter;

import site.klade.webapp.dto.SpecimenDto;
import site.klade.webapp.entity.SpecimenEntity;

/**
 * Mapping between {@link SpecimenEntity} and {@link SpecimenDto}.
 * All entity&lt;-&gt;DTO mapping logic lives in the converter package.
 */
public final class SpecimenConverter {

    private SpecimenConverter() {
    }

    public static SpecimenDto toDto(SpecimenEntity entity) {
        return new SpecimenDto(
                entity.getId(),
                entity.getSpecies() != null ? entity.getSpecies().getId() : null,
                entity.getFitness(),
                entity.getGenome()
        );
    }

    /**
     * Builds a new entity from the DTO. The back-reference to the owning
     * {@link SpecimenEntity#getSpecies() species} must be provided by the caller.
     * The id is left unset — JPA generates it on save.
     */
    public static SpecimenEntity toEntity(SpecimenDto dto, site.klade.webapp.entity.SpeciesEntity species) {
        return new SpecimenEntity(species, dto.getFitness(), dto.getGenome());
    }
}
