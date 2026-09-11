package site.klade.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    private final GenerationRepository generationRepository;

    private final GenomeParser genomeParser;

    public GenomeQueryService(GenerationRepository generationRepository, GenomeParser genomeParser) {
        this.generationRepository = generationRepository;
        this.genomeParser = genomeParser;
    }

    public List<Genome> getBestGenomesPerSpecies() {
        GenerationEntity entity = generationRepository.findById(1L).orElse(null);
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
