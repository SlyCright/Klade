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

    private final GenomeMutator genomeMutator;

    public EvolutionEngine(int specimensPerSpecies, ArenaSettings arenaSettings, GenomeMutator genomeMutator) {
        this.specimensPerSpecies = specimensPerSpecies;
        this.arenaSettings = arenaSettings;
        this.genomeMutator = genomeMutator;
    }

    /**
     * Evaluates fitness of all genomes in the given species list by running them in an Arena.
     * The fitness values are set directly on the genome objects.
     * <p>
     * FITNESS SEMANTICS: fitness = distance to the arena center, so LOWER = BETTER
     * (see {@link FitnessStatistics} for the full explanation).
     */
    public void evaluateFitness(List<Species> allSpecies) {
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

    /**
     * Implements rank-linear reproduction operator (see: Рангово-линейный оператор репродукции и мутации.md).
     * <p>
     * This algorithm is unusual because it eliminates hyperparameters by tying reproduction strategy
     * directly to rank in the fitness-sorted population:
     * - Best genomes (rank 0.0): guaranteed mitosis (cloning) with zero mutation
     * - Worst genomes (rank 1.0): guaranteed meiosis (recombination) with maximum mutation
     * - Intermediate genomes: linearly interpolated probabilities between these extremes
     * <p>
     * This creates a self-adjusting evolutionary gradient where elites refine existing traits
     * while outliers explore new solution spaces, automatically balancing exploitation vs exploration
     * without manual parameter tuning.
     */
    public List<Species> getNextGeneration(List<Species> currentSpecies) {
        List<Species> nextSpecies = new ArrayList<>(currentSpecies.size());
        for (Species species : currentSpecies) {
            List<Genome> currentGenomes = species.getGenomes();
            // Sort by fitness (ascending - lower is better)
            currentGenomes.sort(Comparator.comparingDouble(Genome::getAccumulatedFitness));
            ArrayList<Genome> nextGenomes = new ArrayList<>(specimensPerSpecies);
            int n = currentGenomes.size();
            for (int i = 0; i < specimensPerSpecies; i++) {
                Genome currentGenome = currentGenomes.get(i);
                double rank = (double) i / (n - 1); // rank AKA mutationFactor
                double mitosisProbability = 1.0 - rank;
                Genome offspringGenome;
                if (random.nextDouble() < mitosisProbability) {
                    offspringGenome = mitosis(currentGenome, rank);
                } else {
                    offspringGenome = meiosis(currentGenome, currentGenomes, rank);
                }
                nextGenomes.add(offspringGenome);
            }
            nextSpecies.add(new Species(nextGenomes));
        }
        return nextSpecies;
    }

    private Genome mitosis(Genome genome, double effective_rank) {
        return genomeMutator.mutate(genome, effective_rank);
    }

    private Genome meiosis(Genome currentGenome, List<Genome> genomes, double rank) {
        Genome[] parents = tournamentSelect(currentGenome, genomes);
        Genome offspring = GenomeCrossover.crossover(parents[0], parents[1]);
        return genomeMutator.mutate(offspring, rank);
    }

    private Genome[] tournamentSelect(Genome candidate1, List<Genome> genomes) {
        Genome candidate2 = genomes.get(random.nextInt(genomes.size()));
        Genome candidate3 = genomes.get(random.nextInt(genomes.size()));
        // Select best 2 of 3 (lowest accumulated fitness)
        Genome[] candidates = {candidate1, candidate2, candidate3};
        java.util.Arrays.sort(candidates, Comparator.comparingDouble(Genome::getAccumulatedFitness));
        return new Genome[]{candidates[0], candidates[1]};
    }

}