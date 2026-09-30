package site.klade.webapp.evolution;

import site.klade.simulation.gene.Gene;
import site.klade.simulation.Genome;
import site.klade.simulation.MetaGenes;
import site.klade.simulation.Morphogen;

import java.util.List;
import java.util.Random;

/**
 * Backend-specific mutation logic for evolutionary algorithms.
 * Mutation is not part of the simulation library since it's only needed on the backend.
 * <p>
 * This class orchestrates the mutation process by delegating to specialized mutators
 * for different genome components (meta-genes, morphogens, and genes).
 */
public class GenomeMutator {

    private final MetaGeneMutator metaGeneMutator = new MetaGeneMutator();
    private final GeneMutator geneMutator;
    private final MorphogenMutator morphogenMutator = new MorphogenMutator();

    /**
     * @param deletionBias extra probability of forcing a structural deletion mutation
     *                     (see {@code evolution.mutation.deletion-bias})
     */
    public GenomeMutator(double deletionBias) {
        this.geneMutator = new GeneMutator(deletionBias);
    }

    /**
     * Applies mutations to a genome based on the given rank.
     * Hyper-gene is mutated first, then the updated value is used for subsequent mutations
     * (shorter feedback loop for self-adaptation).
     *
     * @param genome the genome to mutate (will not be modified)
     * @param rank   the normalized rank [0.0, 1.0] in the fitness-sorted population
     * @return a new mutated genome
     */
    public Genome mutate(Genome genome, double rank) {
        Genome mutated = new Genome(genome);
        mutated.resetFitnesses();
        // Calculate initial mutation factor from source genome's hyper-gene
        float initialHyperGene = genome.getHyperGene();
        double initialMutationFactor = initialHyperGene * rank;
        // Mutate hyper-gene first using initial factor
        metaGeneMutator.mutateHyperGene(mutated.getMetaGenes(), initialMutationFactor);
        // Use UPDATED hyper-gene for subsequent mutations (shorter feedback loop)
        float updatedHyperGene = mutated.getHyperGene();
        double updatedMutationFactor = updatedHyperGene * rank;
        metaGeneMutator.mutateMetaGenes(mutated.getMetaGenes(), updatedMutationFactor);
        geneMutator.mutateGenes(mutated.getGenes(), updatedMutationFactor);
        // Structure follows the references the gene phase just produced; value mutation is Step 4.
        morphogenMutator.mutateMorphogens(mutated, updatedMutationFactor);
        return mutated;
    }

}
