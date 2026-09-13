package site.klade.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.klade.simulation.Genome;
import site.klade.webapp.entity.GenerationEntity;
import site.klade.webapp.entity.SpeciesEntity;
import site.klade.webapp.entity.SpecimenEntity;
import site.klade.webapp.parser.GenomeParser;
import site.klade.webapp.repository.GenerationRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class GenomeQueryService {

    private static final long CURRENT_GENERATION_ID = 1L;

    private final GenerationRepository generationRepository;

    private final GenomeParser genomeParser;

    public GenomeQueryService(GenerationRepository generationRepository, GenomeParser genomeParser) {
        this.generationRepository = generationRepository;
        this.genomeParser = genomeParser;
    }

    /**
     * Per-species fitness statistics from the persisted generation, for the Vaadin HUD.
     * Returns an empty list while no generation has been persisted yet ("no data yet").
     */
    @Transactional(readOnly = true)
    public List<SpeciesStats> getSpeciesStatistics() {
        return generationRepository.findById(CURRENT_GENERATION_ID)
                .map(GenerationEntity::getSpecies)
                .orElse(List.of())
                .stream()
                .map(GenomeQueryService::toSpeciesStats)
                .toList();
    }

    private static SpeciesStats toSpeciesStats(SpeciesEntity species) {
        return new SpeciesStats(
                species.getSpeciesIndex() != null ? species.getSpeciesIndex() : 0,
                species.getSpecimens() != null ? species.getSpecimens().size() : 0,
                species.getAverageFitness(),
                species.getBestFitness());
    }

    public List<Genome> getBestGenomesPerSpecies() {
        GenerationEntity entity = generationRepository.findById(CURRENT_GENERATION_ID).orElse(null);
        if (entity == null) {
            return null;
        }

        List<Genome> bestGenomes = new ArrayList<>();

        for (SpeciesEntity species : entity.getSpecies()) {
            SpecimenEntity bestSpecimen = species.getSpecimens().stream()
                    .min(Comparator.comparingDouble(SpecimenEntity::getFitness))
                    .orElse(null);

            if (bestSpecimen != null && bestSpecimen.getGenome() != null) {
                try {
                    Genome genome = genomeParser.parse(bestSpecimen.getGenome());
                    bestGenomes.add(genome);
                    log.debug("Best genome for species {}: fitness={}",
                            species.getSpeciesIndex(), bestSpecimen.getFitness());
                } catch (Exception e) {
                    log.warn("Failed to parse genome for species {}: {}",
                            species.getSpeciesIndex(), e.getMessage());
                }
            }
        }

        return bestGenomes.isEmpty() ? null : bestGenomes;
    }
}
