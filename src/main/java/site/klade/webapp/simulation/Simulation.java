package site.klade.webapp.simulation;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import site.klade.simulation.ArenaSettings;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
public class Simulation {

    public final int SPECIES_TOTAL;

    public final int SPECIMENS_PER_SPECIES;

    public final int SLEEP_PER_UPDATE_MILLIS;

    private final EvolutionEngine evolutionEngine;

    private final AtomicInteger generationNumber = new AtomicInteger(0);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // TODO: Use multithread pool in the future. Single thread is for the MVP to prevent complexity
    //  of thread management

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private ArrayList<Species> speciesList = new ArrayList<>();

    @Getter
    private volatile Generation generation;

    @Setter
    private Consumer<Generation> onGenerationComplete;

    public Simulation(
            int speciesTotal,
            int specimensPerSpecies,
            int sleepPerUpdateMillis,
            ArenaSettings arenaSettings
    ) {
        this.SPECIES_TOTAL = speciesTotal;
        this.SPECIMENS_PER_SPECIES = specimensPerSpecies;
        this.SLEEP_PER_UPDATE_MILLIS = sleepPerUpdateMillis;
        this.evolutionEngine = new EvolutionEngine(SPECIMENS_PER_SPECIES, arenaSettings);
        // TODO: check whether the DB has previous data, if so, load it. If not, initialize a new simulation
        initialize();
    }

    private void initialize() {
        generationNumber.set(0);
        speciesList.clear();
        for (int i = 0; i < SPECIES_TOTAL; i++) speciesList.add(new Species(SPECIMENS_PER_SPECIES));
        this.generation = new Generation(generationNumber.get(), GenerationCopier.getDeepCopyOf(speciesList));
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

    private void update() {
        try {
            if (sleepInterrupted()) return;
            // 1. Evaluate fitness of current generation
            evolutionEngine.evaluateFitness(speciesList);
            // 2. Capture generation snapshot (before creating next generation, so snapshot shows the current one)
            this.generation = new Generation(
                    generationNumber.get(),
                    GenerationCopier.getDeepCopyOf(speciesList));
            if (onGenerationComplete == null) {
                throw new IllegalStateException("onGenerationComplete callback must be set before running simulation");
            }
            onGenerationComplete.accept(generation);
            // 3. Advance to next generation
            speciesList = new ArrayList<>(evolutionEngine.getNextGeneration(speciesList));
            generationNumber.incrementAndGet();
        } catch (Exception e) {
            log.error("Simulation error: {}", e.getMessage(), e);
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