package site.klade.webapp.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import site.klade.simulation.SettingsDto;

import static org.assertj.core.api.Assertions.assertThat;

public class SimulationTest {

    @DisplayName("Given no settings, when withDefaultSettings is called, then simulation is created with default values")
    @Test
    void givenNoSettings_whenWithDefaultSettings_thenSimulationCreatedWithDefaults() {
        // Given
        // no preconditions
        // When
        Simulation simulation = Simulation.withDefaultSettings();
        // Then
        assertThat(simulation).isNotNull();
        assertThat(simulation.SPECIES_TOTAL).isEqualTo(3);
        assertThat(simulation.SPECIMENS_PER_SPECIES).isEqualTo(10);
        assertThat(simulation.SLEEP_PER_UPDATE_MILLIS).isEqualTo(100);
    }

    @DisplayName("Given custom settings, when with is called, then simulation is created with custom values")
    @Test
    void givenCustomSettings_whenWith_thenSimulationCreatedWithCustomValues() {
        // Given
        SettingsDto settings = SettingsDto.builder()
                .speciesTotal(5)
                .specimensPerSpecies(20)
                .sleepPerUpdateMillis(200)
                .build();
        // When
        Simulation simulation = Simulation.with(settings);
        // Then
        assertThat(simulation).isNotNull();
        assertThat(simulation.SPECIES_TOTAL).isEqualTo(5);
        assertThat(simulation.SPECIMENS_PER_SPECIES).isEqualTo(20);
        assertThat(simulation.SLEEP_PER_UPDATE_MILLIS).isEqualTo(200);
    }

    @DisplayName("Given simulation created, when getSnapShot is called, then snapshot is returned")
    @Test
    void givenSimulationCreated_whenGetSnapShot_thenSnapshotReturned() {
        // Given
        Simulation simulation = Simulation.withDefaultSettings();
        // When
        var snapshot = simulation.getSnapshot();
        // Then
        assertThat(snapshot).isNotNull();
        assertThat(snapshot.getGenerationNumber()).isEqualTo(0);
    }

    @DisplayName("Given simulation not running, when runCertainGenerations is called, then generations are advanced")
    @Test
    void givenSimulationNotRunning_whenRunCertainGenerations_thenGenerationsAdvanced() {
        // Given
        Simulation simulation = Simulation.withDefaultSettings();
        int initialGen = simulation.getSnapshot().getGenerationNumber();
        // When
        simulation.runCertainGenerations(3);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Then
        assertThat(simulation.getSnapshot().getGenerationNumber()).isEqualTo(initialGen + 2);
    }

    @DisplayName("Given simulation advanced, when reset is called, then simulation is reset to initial state")
    @Test
    void givenSimulationAdvanced_whenReset_thenSimulationResetToInitialState() {
        // Given
        Simulation simulation = Simulation.withDefaultSettings();
        simulation.runCertainGenerations(2); // advance
        // When
        simulation.reset();
        // Then
        assertThat(simulation.getSnapshot().getGenerationNumber()).isEqualTo(0);
        assertThat(simulation.getSnapshot().getSpeciesList()).isNotEmpty();
    }

    @DisplayName("Given simulation, when stop is called, then no exception is thrown")
    @Test
    void givenSimulation_whenStop_thenNoExceptionThrown() {
        // Given
        Simulation simulation = Simulation.withDefaultSettings();
        // When
        // Then
        assertThat(simulation).isNotNull();
        simulation.stop();
    }

    @DisplayName("Given simulation, when shutdown is called, then no exception is thrown")
    @Test
    void givenSimulation_whenShutdown_thenNoExceptionThrown() {
        // Given
        Simulation simulation = Simulation.withDefaultSettings();
        // When
        // Then
        assertThat(simulation).isNotNull();
        simulation.shutdown();
    }

    @DisplayName("Given simulation, when start is called, then no exception is thrown")
    @Test
    void givenSimulation_whenStart_thenNoExceptionThrown() {
        // Given
        Simulation simulation = Simulation.withDefaultSettings();
        // When
        // Then
        assertThat(simulation).isNotNull();
        simulation.start();
        // To avoid hanging, stop immediately
        simulation.stop();
    }
}
