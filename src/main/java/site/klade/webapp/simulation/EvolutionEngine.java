package site.klade.webapp.simulation;

import site.klade.simulation.Arena;
import site.klade.simulation.Genome;
import site.klade.simulation.Species;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Responsible for the evolutionary algorithm: fitness evaluation and next‑generation creation.
 * Does not manage threads, snapshots, or persistent state.
 */
public class EvolutionEngine {

    private final int specimensPerSpecies;

    private final Random random = new Random();

    public EvolutionEngine(int specimensPerSpecies) {
        this.specimensPerSpecies = specimensPerSpecies;
    }

    /**
     * Evaluates fitness of all genomes in the given species list by running them in an Arena.
     * The fitness values are set directly on the genome objects.
     */
    public void evaluateFitness(List<Species> speciesList) {
        for (Species species : speciesList) {
            for (Genome genome : species.getGenomes()) {
                new Arena(genome).run();
            }
        }
    }

    /**
     * Creates the next generation from the current one.
     * Assumes that fitness has already been evaluated for all current genomes.
     *
     * @param currentSpecies the current generation's species list
     * @return a new list of Species representing the next generation
     */
    public List<Species> nextGeneration(List<Species> currentSpecies) {
        List<Species> nextSpecies = new ArrayList<>(currentSpecies.size());
        for (Species species : currentSpecies) {
            List<Genome> currentGenomes = species.getGenomes();
            ArrayList<Genome> nextGenomes = new ArrayList<>(specimensPerSpecies);
            // Elitism: keep the single best genome unchanged
            Genome elite = currentGenomes.stream()
                    .min(Comparator.comparingDouble(Genome::getFitness))
                    .orElse(currentGenomes.get(0));
            nextGenomes.add(elite);  // reference is fine; it won't be mutated further
            // Fill the rest with mutated offspring from tournament selection
            for (int i = 0; i < specimensPerSpecies - 1; i++) {
                Genome candidate1 = currentGenomes.get(random.nextInt(currentGenomes.size()));
                Genome candidate2 = currentGenomes.get(random.nextInt(currentGenomes.size()));
                Genome winner = (candidate1.getFitness() < candidate2.getFitness())
                        ? candidate1
                        : candidate2;
                Genome offspring = Genome.getMutatedAndFitnessMaxedCopyOf(winner);
                nextGenomes.add(offspring);
            }
            nextSpecies.add(new Species(nextGenomes));
        }
        return nextSpecies;
    }
}