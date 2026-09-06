package site.klade.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.klade.simulation.Genome;
import site.klade.webapp.entity.GenerationEntity;
import site.klade.webapp.entity.SpeciesEntity;
import site.klade.webapp.entity.SpecimenEntity;
import site.klade.webapp.repository.GenerationRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class GenomeQueryService {

    private final GenerationRepository generationRepository;

    public GenomeQueryService(GenerationRepository generationRepository) {
        this.generationRepository = generationRepository;
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
                // Parse genome string back to Genome object
                // Note: This assumes Genome has a constructor or factory method that can parse the string
                // For now, return a placeholder or null since Genome parsing is not implemented
                log.debug("Best genome for species {}: fitness={}, genome={}", 
                        species.getSpeciesIndex(), bestSpecimen.getFitness(), bestSpecimen.getGenome());
                // TODO: Implement Genome parsing from string representation
                // bestGenomes.add(new Genome(bestSpecimen.getGenome()));
            }
        }
        
        return bestGenomes.isEmpty() ? null : bestGenomes;
    }
}
