package site.klade.webapp.simulation;

import lombok.Getter;
import lombok.Setter;
import site.klade.simulation.DataTransferUtilities;
import site.klade.simulation.SettingsDto;
import site.klade.simulation.SimulationSnapshotDto;
import site.klade.simulation.Species;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Simulation {

    public final int SPECIES_TOTAL;

    public final int SPECIMENS_PER_SPECIES;

    public final int SLEEP_PER_UPDATE_MILLIS;

    private final EvolutionEngine evolutionEngine;

    private final AtomicInteger generationNumber = new AtomicInteger(0);
    // TODO: Use multithread pool in the future. Single thread is for the MVP to prevent complexity
    //  of thread management

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private ArrayList<Species> speciesList = new ArrayList<>();

    @Getter
    private volatile SimulationSnapshotDto snapshot;

    @Setter
    private Consumer<SimulationSnapshotDto> onGenerationComplete;

    private Simulation(SettingsDto settings) {
        this.SPECIES_TOTAL = settings.getSpeciesTotal();
        this.SPECIMENS_PER_SPECIES = settings.getSpecimensPerSpecies();
        this.SLEEP_PER_UPDATE_MILLIS = settings.getSleepPerUpdateMillis();
        this.evolutionEngine = new EvolutionEngine(SPECIMENS_PER_SPECIES);
        initialize();
    }

    public static Simulation withDefaultSettings() {
        return with(SettingsDto.builder().build());
    }

    public static Simulation with(SettingsDto settings) {
        return new Simulation(settings);
    }

    public void start() {
        if (isRunning.get()) return;
        if (isRunning.compareAndSet(false, true)) {
            executor.submit(() -> {
                while (isRunning.get()) update();
            });
        }
    }

    public void runCertainGenerations(int number) {
        if (isRunning.get()) return;
        if (isRunning.compareAndSet(false, true)) {
            executor.submit(() -> {
                for (int i = 0; i < number; i++) {
                    if (!isRunning.get()) break;
                    update();
                }
                isRunning.set(false);
            });
        }
    }

    public void stop() {
        isRunning.set(false);
    }

    public void reset() {
        stop();
        initialize();
    }

    public void shutdown() {
        stop();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void initialize() {
        generationNumber.set(0);
        speciesList.clear();
        for (int i = 0; i < SPECIES_TOTAL; i++) {
            speciesList.add(new Species(SPECIMENS_PER_SPECIES));
        }
        this.snapshot = new SimulationSnapshotDto(
                generationNumber.get(),
                DataTransferUtilities.getDeepCopyOf(speciesList));
    }

    private void update() {
        try {
            if (sleepInterrupted()) return;
            // 1. Evaluate fitness of current generation
            evolutionEngine.evaluateFitness(speciesList);
            // 2. Capture snapshot (before creating next generation, so snapshot shows the current one)
            this.snapshot = new SimulationSnapshotDto(
                    generationNumber.get(),
                    DataTransferUtilities.getDeepCopyOf(speciesList));
            if (onGenerationComplete != null) onGenerationComplete.accept(snapshot);
            // 3. Advance to next generation
            speciesList = new ArrayList<>(evolutionEngine.nextGeneration(speciesList));
            generationNumber.incrementAndGet();
        } catch (Exception e) {
            System.out.println("Simulation error: " + e.getMessage());
            isRunning.set(false);
        }
    }

    private boolean sleepInterrupted() {
        try {
            Thread.sleep(SLEEP_PER_UPDATE_MILLIS);
        } catch (InterruptedException e) {
            isRunning.set(false);
            Thread.currentThread().interrupt();
            return true;
        }
        return false;
    }
}