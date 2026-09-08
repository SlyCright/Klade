package site.klade.webapp.service;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.klade.webapp.config.SimulationProperties;
import site.klade.webapp.simulation.EvolutionEngine;
import site.klade.webapp.simulation.GenomeMutator;
import site.klade.webapp.simulation.Simulation;

@Slf4j
@Getter
@Service
public class SimulationLifecycleService {

    private final Simulation simulation;

    public SimulationLifecycleService(
            GenerationPersistenceService persistenceService,
            SimulationProperties properties
    ) {
        GenomeMutator genomeMutator = new GenomeMutator(
                properties.getBaseMetaGeneMutationChance(),
                properties.getBaseMorphogenMutationChance(),
                properties.getBaseGeneMutationChance());
        EvolutionEngine evolutionEngine = new EvolutionEngine(
                properties.getSpecimensPerSpecies(),
                properties.getArena(),
                genomeMutator);
        this.simulation = new Simulation(
                properties.getSpeciesTotal(),
                properties.getSpecimensPerSpecies(),
                properties.getSleepPerUpdateMillis(),
                evolutionEngine);
        this.simulation.setOnGenerationComplete(persistenceService::saveGeneration);
        log.info("Simulation initialized with {} species, {} specimens per species, and {} sleep per update.",
                properties.getSpeciesTotal(),
                properties.getSpecimensPerSpecies(),
                properties.getSleepPerUpdateMillis());
        this.simulation.runCertainGenerations(1);
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

}