package site.klade.webapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.klade.simulation.Genome;
import site.klade.simulation.SimulationSnapshotDto;
import site.klade.webapp.entity.GenerationEntity;
import site.klade.webapp.repository.GenerationRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class GenomeQueryService {

    private final GenerationRepository generationRepository;

    private final ObjectMapper objectMapper;

    public GenomeQueryService(GenerationRepository generationRepository, ObjectMapper objectMapper) {
        this.generationRepository = generationRepository;
        this.objectMapper = objectMapper;
    }

    public List<Genome> getBestGenomesPerSpecies() {
        GenerationEntity entity = generationRepository.findById(1L).orElse(null);
        if (entity == null) {
            return null;
        }
        try {
            SimulationSnapshotDto snapshot = objectMapper.readValue(
                    entity.getData(), SimulationSnapshotDto.class);
            List<Genome> bestGenomes = new ArrayList<>();
            for (site.klade.simulation.Species species : snapshot.getSpeciesList()) {
                Genome best = species.getGenomes().stream()
                        .min(Comparator.comparingDouble(Genome::getFitness))
                        .map(Genome::new)
                        .orElse(null);
                if (best != null) {
                    bestGenomes.add(best);
                }
            }
            return bestGenomes;
        } catch (Exception e) {
            log.error("Failed to parse saved generation", e);
            return null;
        }
    }
}
