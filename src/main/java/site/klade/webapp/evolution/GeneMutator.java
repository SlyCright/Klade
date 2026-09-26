package site.klade.webapp.evolution;

import site.klade.simulation.Gene;

import java.util.List;

/**
 * Orchestrates gene mutation by delegating to specialized mutators.
 * <p>
 * This class coordinates structural and parameter mutations for genes.
 */
public class GeneMutator {

    private final GeneStructuralMutator structuralMutator;
    private final GeneParameterMutator parameterMutator = new GeneParameterMutator();

    /**
     * @param deletionBias extra probability of forcing a structural deletion mutation
     *                     (see {@code evolution.mutation.deletion-bias})
     */
    public GeneMutator(double deletionBias) {
        this.structuralMutator = new GeneStructuralMutator(deletionBias);
    }

    /**
     * Mutates genes (structural and parameter mutations).
     *
     * @param genes          the genes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    public void mutateGenes(List<Gene> genes, double mutationFactor) {
        structuralMutator.mutate(genes, mutationFactor);
        parameterMutator.mutate(genes, mutationFactor);
    }
}
