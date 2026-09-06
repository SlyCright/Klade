package site.klade.webapp.converter;

import site.klade.webapp.dto.GenerationDto;
import site.klade.webapp.entity.GenerationEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapping between {@link GenerationEntity} and {@link GenerationDto}
 * (deep mapping: generation -&gt; species -&gt; specimens).
 * All entity&lt;-&gt;DTO mapping logic lives in the converter package.
 */
public final class GenerationConverter {

    private GenerationConverter() {
    }

    public static GenerationDto toDto(GenerationEntity entity) {
        List<site.klade.webapp.dto.SpeciesDto> species = new ArrayList<>();
        for (var speciesEntity : entity.getSpecies()) {
            species.add(SpeciesConverter.toDto(speciesEntity));
        }
        return new GenerationDto(entity.getId(), entity.getGenerationNumber(), species);
    }

    /**
     * Builds a new detached entity graph (generation + species + specimens) from the DTO.
     * Ids are left unset — JPA generates them on save.
     */
    public static GenerationEntity toEntity(GenerationDto dto) {
        GenerationEntity entity = new GenerationEntity();
        entity.setGenerationNumber(dto.getGenerationNumber());
        for (var speciesDto : dto.getSpecies()) {
            entity.getSpecies().add(SpeciesConverter.toEntity(speciesDto, entity));
        }
        return entity;
    }
}
