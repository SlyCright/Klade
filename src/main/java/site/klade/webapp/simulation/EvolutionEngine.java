package site.klade.webapp.simulation;

import site.klade.simulation.Arena;
import site.klade.simulation.ArenaSettings;
import site.klade.simulation.Genome;

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

    private final ArenaSettings arenaSettings;

    private final Random random = new Random();

    public EvolutionEngine(int specimensPerSpecies, ArenaSettings arenaSettings) {
        this.specimensPerSpecies = specimensPerSpecies;
        this.arenaSettings = arenaSettings;
    }

    /**
     * Evaluates fitness of all genomes in the given species list by running them in an Arena.
     * The fitness values are set directly on the genome objects.
     * <p>
     * FITNESS SEMANTICS: fitness = distance to the arena center, so LOWER = BETTER
     * (see {@link FitnessStatistics} for the full explanation).
     */
    public void evaluateFitness(List<Species> allSpecies) {
        resetFitnesses(allSpecies);
        ArrayList<Genome> competitionPair = new ArrayList<>(2);
        for (int speciesIndexI = 0; speciesIndexI < allSpecies.size(); speciesIndexI++) {
            for (int speciesIndexJ = speciesIndexI + 1; speciesIndexJ < allSpecies.size(); speciesIndexJ++) {
                List<Genome> genomesI = allSpecies.get(speciesIndexI).getGenomes();
                List<Genome> genomesJ = allSpecies.get(speciesIndexJ).getGenomes();
                for (Genome genomeI : genomesI) {
                    for (Genome genomeJ : genomesJ) {
                        genomeI.resetCurrentFitness();
                        genomeJ.resetCurrentFitness();
                        competitionPair.clear();
                        competitionPair.add(genomeI);
                        competitionPair.add(genomeJ);
                        new Arena(competitionPair, arenaSettings).run();
                        genomeI.updateAccumulatedFitness();
                        genomeJ.updateAccumulatedFitness();
                    }
                }
            }
        }
    }

    private static void resetFitnesses(List<Species> allSpecies) {
        for (Species species : allSpecies) {
            for (Genome genome : species.getGenomes()) {
                genome.resetFitnesses();
            }
        }
    }

    public List<Species> getNextGeneration(List<Species> currentSpecies) {
        List<Species> nextSpecies = new ArrayList<>(currentSpecies.size());
        for (Species species : currentSpecies) {
            List<Genome> currentGenomes = species.getGenomes();
            ArrayList<Genome> nextGenomes = new ArrayList<>(specimensPerSpecies);
            // Elitism: keep the single best genome unchanged
            Genome elite = currentGenomes.stream()
                    .min(Comparator.comparingDouble(Genome::getAccumulatedFitness))
                    .orElse(currentGenomes.get(0));
            nextGenomes.add(new Genome(elite));
            // Fill the rest with mutated offspring from tournament selection
//            for (int i = 0; i < specimensPerSpecies - 1; i++) {
//                Genome candidate1 = currentGenomes.get(random.nextInt(currentGenomes.size()));
//                Genome candidate2 = currentGenomes.get(random.nextInt(currentGenomes.size()));
//                Genome winner = (candidate1.getFitness() < candidate2.getFitness())
//                        ? candidate1
//                        : candidate2;
//                Genome offspring = Genome.getOffspringOf(winner);
//                nextGenomes.add(offspring);
//            }
            nextSpecies.add(new Species(nextGenomes));
        }
        return nextSpecies;
    }

}