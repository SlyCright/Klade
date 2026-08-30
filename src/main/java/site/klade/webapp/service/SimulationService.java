package site.klade.webapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.klade.simulation.Genome;
import site.klade.simulation.SimulationSnapshotDto;
import site.klade.webapp.config.SimulationProperties;
import site.klade.webapp.entity.GenerationEntity;
import site.klade.webapp.repository.GenerationRepository;
import site.klade.webapp.simulation.Simulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class SimulationService {

    private final Simulation simulation;

    private final GenerationRepository generationRepository;

    private final ObjectMapper objectMapper;

    private final SimulationProperties simulationProperties;

    private final AtomicBoolean saveInProgress = new AtomicBoolean(false);

    public SimulationService(GenerationRepository generationRepository, ObjectMapper objectMapper, SimulationProperties simulationProperties) {
        simulation = Simulation.withDefaultSettings();
        this.simulation.setOnGenerationComplete(this::saveGenerationSnapshot);
        this.generationRepository = generationRepository;
        this.objectMapper = objectMapper;
        this.simulationProperties = simulationProperties;
    }

    public void start() {
        simulation.start();
    }

    public void runCertainGenerations(int number) {
        simulation.runCertainGenerations(number);
    }

    public void stop() {
        simulation.stop();
    }

    public void reset() {
        simulation.reset();
    }

    @PreDestroy
    public void shutdown() {
        simulation.shutdown();
    }

    // TODO: get rid of the polling. The simulation should notify the Spring application when next
    //  generation is ready. Frontend should get notifications as well
    public SimulationSnapshotDto getSimulationSnapshot() {
        return simulation.getSnapshot();
    }

    // TODO: Each genome should be stored separately in the DB. Finding the best one is
    //  DB logic, involving comparisons of their fitnesses.
    public List<Genome> getBestGenomesPerSpecies() {
        GenerationEntity entity = generationRepository.findById(1L).orElse(null);
        if (entity == null) {
            return null;  // no generation saved yet
        }
        try {
            SimulationSnapshotDto snapshot = objectMapper.readValue(
                    entity.getData(), SimulationSnapshotDto.class);
            List<Genome> bestGenomes = new ArrayList<>();
            int speciesIndex = 0;
            for (site.klade.simulation.Species species : snapshot.getSpeciesList()) {
                Genome best = species.getGenomes().stream()
                        .min(Comparator.comparingDouble(Genome::getFitness))
                        .map(Genome::new)   // return a copy to avoid mutation
                        .orElse(null);
                if (best != null) {
                    bestGenomes.add(best);
                }
                speciesIndex++;
            }
            return bestGenomes;
        } catch (Exception e) {
            log.error("Failed to parse saved generation", e);
            return null;
        }
    }

    // TODO: callback from Simulation with a new generation should start the Spring's event which
    //  leads to update DB and Vaadin's GUI
    private void saveGenerationSnapshot(SimulationSnapshotDto snapshot) {
        // Try to claim the save slot – only one can proceed
        if (!saveInProgress.compareAndSet(false, true)) {
            log.debug("Skipping save for generation {} – DB busy", snapshot.getGenerationNumber());
            return;
        }
        // Launch the actual save asynchronously
        performAsyncSave(snapshot);
    }

    @Async  // uses the default Spring executor (creates a new thread per call)
    public void performAsyncSave(SimulationSnapshotDto snapshot) {
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            GenerationEntity entity = generationRepository.findById(1L)
                    .orElse(new GenerationEntity());
            entity.setGenerationNumber(snapshot.getGenerationNumber());
            entity.setData(json);
            generationRepository.save(entity);
            log.debug("Saved generation {} to DB", snapshot.getGenerationNumber());
        } catch (Exception e) {
            log.error("Failed to save generation snapshot", e);
        } finally {
            // Always clear the flag when done, so future generations can be saved
            saveInProgress.set(false);
        }
    }
}