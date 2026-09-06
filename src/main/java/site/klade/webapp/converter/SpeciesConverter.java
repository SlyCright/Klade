package site.klade.webapp.converter;

import site.klade.webapp.dto.SpeciesDto;
import site.klade.webapp.entity.GenerationEntity;
import site.klade.webapp.entity.SpeciesEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapping between {@link SpeciesEntity} and {@link SpeciesDto}.
 * All entity&lt;-&gt;DTO mapping logic lives in the converter package.
 */
public final class SpeciesConverter {

    private SpeciesConverter() {
    }

    public static SpeciesDto toDto(SpeciesEntity entity) {
        List<site.klade.webapp.dto.SpecimenDto> specimens = new ArrayList<>();
        for (var specimen : entity.getSpecimens()) {
            specimens.add(SpecimenConverter.toDto(specimen));
        }
        return new SpeciesDto(
                entity.getId(),
                entity.getGeneration() != null ? entity.getGeneration().getId() : null,
                entity.getSpeciesIndex(),
                entity.getAverageFitness(),
                entity.getBestFitness(),
                specimens
        );
    }

    /**
     * Builds a new entity graph (species + its specimens) from the DTO. The back-reference
     * to the owning {@link SpeciesEntity#getGeneration() generation} must be provided by
     * the caller. Ids are left unset — JPA generates them on save.
     */
    public static SpeciesEntity toEntity(SpeciesDto dto, GenerationEntity generation) {
        SpeciesEntity entity = new SpeciesEntity(
                generation,
                dto.getSpeciesIndex(),
                dto.getAverageFitness(),
                dto.getBestFitness()
        );
        for (var specimen : dto.getSpecimens()) {
            entity.getSpecimens().add(SpecimenConverter.toEntity(specimen, entity));
        }
        return entity;
    }
}
