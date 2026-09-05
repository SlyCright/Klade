package site.klade.webapp.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.klade.simulation.Genome;
import site.klade.simulation.SimulationSnapshotDto;

import java.util.List;

@Slf4j
@Service
public class SimulationService {

    private final SimulationLifecycleService lifecycleService;

    private final GenerationPersistenceService persistenceService;

    private final GenomeQueryService genomeQueryService;

    private final SimulationSnapshotService snapshotService;

    public SimulationService(
            SimulationLifecycleService lifecycleService,
            GenerationPersistenceService persistenceService,
            GenomeQueryService genomeQueryService,
            SimulationSnapshotService snapshotService) {
        this.lifecycleService = lifecycleService;
        this.persistenceService = persistenceService;
        this.genomeQueryService = genomeQueryService;
        this.snapshotService = snapshotService;

        lifecycleService.getSimulation().setOnGenerationComplete(persistenceService::saveGenerationSnapshot);
    }

    public void start() {
        lifecycleService.start();
    }

    public void runCertainGenerations(int number) {
        lifecycleService.runCertainGenerations(number);
    }

    public void stop() {
        lifecycleService.stop();
    }

    public void reset() {
        lifecycleService.reset();
    }

    @PreDestroy
    public void shutdown() {
        lifecycleService.shutdown();
    }

    public SimulationSnapshotDto getSimulationSnapshot() {
        return snapshotService.getSimulationSnapshot();
    }

    public List<Genome> getBestGenomesPerSpecies() {
        return genomeQueryService.getBestGenomesPerSpecies();
    }
}