package site.klade.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.klade.webapp.simulation.FitnessStatistics;
import site.klade.webapp.simulation.Simulation;
import site.klade.webapp.simulation.Generation;
import site.klade.webapp.simulation.Species;

// TODO: get rid of this class. Its logic should belong to GenerationPersistenceService. Do all calculations there
//  before persisting. There is all needed columns in DB tables to store calculated data. The controller should
//  only get data via GenomeQueryService.
@Slf4j
@Service
public class SimulationSnapshotService {

    private final Simulation simulation;

    public SimulationSnapshotService(SimulationLifecycleService lifecycleService) {
        this.simulation = lifecycleService.getSimulation();
    }

    public Generation getSimulationSnapshot() {
        return simulation.getGeneration();
    }

    /**
     * Human-readable one-screen status of the live simulation, used by the UI.
     * Formerly lived in Generation.toString() — moved here so the DTO
     * stays a pure data carrier (see the TODO resolution on that class).
     */
    public String toStatusText(Generation snapshot) {
        int totalSpecimens = 0;
        float bestFitnessOverall = Float.MAX_VALUE; // lower = better; start from the "worst" sentinel
        float totalFitness = 0;
        int fitnessCount = 0;

        StringBuilder speciesLines = new StringBuilder();

        if (snapshot.getSpeciesList() != null) {
            for (int i = 0; i < snapshot.getSpeciesList().size(); i++) {
                Species species = snapshot.getSpeciesList().get(i);
                if (species.getGenomes() == null) {
                    continue;
                }

                FitnessStatistics stats = FitnessStatistics.of(species.getGenomes());
                totalSpecimens += stats.getCount();
                if (stats.getBestFitness() < bestFitnessOverall) {
                    bestFitnessOverall = stats.getBestFitness();
                }
                totalFitness += stats.getTotalFitness();
                fitnessCount += stats.getCount();

                speciesLines.append("\n  Species[").append(i).append("]: specimens=").append(stats.getCount())
                        .append(", bestFitness=").append(formatFitness(stats.getBestFitness()))
                        .append(", avgFitness=").append(formatFitness(stats.getAverageFitness()));
            }
        }

        float avgFitnessOverall = fitnessCount > 0 ? totalFitness / fitnessCount : Float.MAX_VALUE;

        return "SimulationSnapshot{generation=" + snapshot.getGenerationNumber()
                + ", speciesCount=" + (snapshot.getSpeciesList() != null ? snapshot.getSpeciesList().size() : 0)
                + ", totalSpecimens=" + totalSpecimens
                + ", bestFitness=" + formatFitness(bestFitnessOverall)
                + ", avgFitness=" + formatFitness(avgFitnessOverall)
                + "}"
                + speciesLines;
    }

    /**
     * Fitness semantics: LOWER = BETTER (distance to the arena center).
     * {@code Float.MAX_VALUE} means "nothing evaluated" and is shown as N/A.
     */
    private String formatFitness(float fitness) {
        return fitness < Float.MAX_VALUE ? String.valueOf(Math.round(fitness * 100) / 100.0) : "N/A";
    }
}
