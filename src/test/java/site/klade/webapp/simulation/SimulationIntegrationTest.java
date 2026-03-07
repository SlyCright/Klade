package site.klade.webapp.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import site.klade.simulation.Genome;
import site.klade.simulation.SettingsDto;
import site.klade.simulation.Species;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationIntegrationTest {

    @Test
    @DisplayName("Given a simulation with default settings, when running multiple generations, then minimum fitness tends to approach zero")
    void givenSimulationWithDefaultSettings_whenRunningMultipleGenerations_thenMinimumFitnessTendsToApproachZero() {
        // Given: a simulation with fast updates for testing
        SettingsDto fastSettings = SettingsDto.builder()
                .speciesTotal(2)
                .specimensPerSpecies(5)
                .sleepPerUpdateMillis(0)
                .build();
        
        Simulation simulation = Simulation.with(fastSettings);
        
        List<Float> minimumFitnesses = new ArrayList<>();
        int generationsToRun = 20;
        
        // When: running multiple generations and recording fitness
        for (int generation = 0; generation < generationsToRun; generation++) {
            System.out.printf("Starting generation %d%n", generation);
            simulation.runCertainGenerations(1);
            
            // Wait for the generation to complete
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            
            System.out.printf("Generation %d completed, getting snapshot%n", generation);
            
            var snapshot = simulation.getSnapshot();
            System.out.printf("Snapshot generation: %d%n", snapshot.getGenerationNumber());
            
            // Calculate minimum fitness for this generation
            float minimumFitness = Float.MAX_VALUE;
            
            for (Species species : simulation.getSnapshot().getSpeciesList()) {
                for (Genome genome : species.getGenomes()) {
                    if (genome.getFitness() < minimumFitness) {
                        minimumFitness = genome.getFitness();
                    }
                    System.out.printf("Specimen fitness: %.3f%n", genome.getFitness());
                }
            }
            
            minimumFitnesses.add(minimumFitness);
            
            System.out.printf("Generation %d: Minimum fitness = %.3f%n", generation, minimumFitness);
        }
        
        // Then: fitness should show a decreasing trend over generations
        // We check that the final minimum fitness is less than the initial minimum fitness
        float initialFitness = minimumFitnesses.get(0);
        float finalFitness = minimumFitnesses.get(minimumFitnesses.size() - 1);
        
        assertThat(finalFitness)
                .as("Final fitness should be less than initial fitness")
                .isLessThan(initialFitness);
        
        // Additionally, check that at least 60% of the generations show improvement over the previous one
        int improvements = 0;
        for (int i = 1; i < minimumFitnesses.size(); i++) {
            if (minimumFitnesses.get(i) < minimumFitnesses.get(i - 1)) {
                improvements++;
            }
        }
        
        float improvementRatio = (float) improvements / (minimumFitnesses.size() - 1);
        assertThat(improvementRatio)
                .as("At least 60%% of generations should show fitness improvement")
                .isGreaterThanOrEqualTo(0.6f);
        
        // Clean up
        simulation.shutdown();
    }
}
