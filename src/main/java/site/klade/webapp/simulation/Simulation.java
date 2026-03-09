package site.klade.webapp.simulation;

import lombok.Getter;
import lombok.Setter;
import site.klade.simulation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
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

    private final AtomicInteger generationNumber = new AtomicInteger(0);

    // TODO: Use multithread pool in the future. Single thread is for the MVP to prevent complexity
    //  of thread management
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private ArrayList<Species> speciesList = new ArrayList<>();
    // TODO: check whether it really can be used later

    @Getter
    private volatile SimulationSnapshotDto snapshot;

    @Setter
    private Consumer<SimulationSnapshotDto> onGenerationComplete;

    private Simulation(SettingsDto settings) {
        this.SPECIES_TOTAL = settings.getSpeciesTotal();
        this.SPECIMENS_PER_SPECIES = settings.getSpecimensPerSpecies();
        this.SLEEP_PER_UPDATE_MILLIS = settings.getSleepPerUpdateMillis();
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
            calculateFitnesses();
            this.snapshot = new SimulationSnapshotDto(
                    generationNumber.getAndIncrement(),
                    DataTransferUtilities.getDeepCopyOf(speciesList));
            if (onGenerationComplete != null) onGenerationComplete.accept(snapshot);
            createNextGeneration();
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

    private void calculateFitnesses() {
        for (var species : speciesList) {
            for (var genome : species.getGenomes()) {
                new Arena(genome).run();
            }
        }
    }

    private void createNextGeneration() {
        final var random = new Random();
        var offspringsSpeciesList = new ArrayList<Species>();
        for (var species : speciesList) {
            var genomes = species.getGenomes();
            var offspringsGenomes = new ArrayList<Genome>();
            // Find and preserve the elite genome (with the best fitness)
            var eliteGenome = genomes.stream()
                    .min(Comparator.comparingDouble(Genome::getFitness))
                    .orElse(genomes.get(0));
            offspringsGenomes.add(eliteGenome);
            for (int i = 0; i < SPECIMENS_PER_SPECIES - 1; i++) {
                var candidate1 = genomes.get(random.nextInt(genomes.size()));
                var candidate2 = genomes.get(random.nextInt(genomes.size()));
                var winner = candidate1.getFitness() < candidate2.getFitness()
                        ? candidate1
                        : candidate2;
                offspringsGenomes.add(Genome.getMutatedAndFitnessMaxedCopyOf(winner));
            }
            var offspringsSpecies = new Species(offspringsGenomes);
            offspringsSpeciesList.add(offspringsSpecies);
        }
        speciesList = offspringsSpeciesList;
    }
}