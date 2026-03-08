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
    @DisplayName("Given a simulation with default settings, when running multiple generations, then average fitness improves over time")
    void givenSimulationWithDefaultSettings_whenRunningMultipleGenerations_thenAverageFitnessImprovesOverTime() {
        // Given: a simulation with fast updates for testing
        SettingsDto fastSettings = SettingsDto.builder()
                .speciesTotal(2)
                .specimensPerSpecies(5)
                .sleepPerUpdateMillis(0)
                .build();
        
        Simulation simulation = Simulation.with(fastSettings);
        
        List<Float> averageFitnesses = new ArrayList<>();
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
            
            // Calculate average fitness for this generation
            float totalFitness = 0;
            int specimenCount = 0;
            
            for (Species species : simulation.getSnapshot().getSpeciesList()) {
                for (Genome genome : species.getGenomes()) {
                    totalFitness += genome.getFitness();
                    specimenCount++;
                    System.out.printf("Specimen fitness: %.3f%n", genome.getFitness());
                }
            }
            
            float averageFitness = totalFitness / specimenCount;
            averageFitnesses.add(averageFitness);
            
            System.out.printf("Generation %d: Average fitness = %.3f%n", generation, averageFitness);
        }
        
        // Then: average fitness of last generation should be better than first generation
        float firstGenerationAverage = averageFitnesses.get(0);
        float lastGenerationAverage = averageFitnesses.get(averageFitnesses.size() - 1);
        
        assertThat(lastGenerationAverage)
                .as("Last generation average fitness should be better than first generation")
                .isLessThan(firstGenerationAverage);
        
        // Clean up
        simulation.shutdown();
    }
}
