package site.klade.webapp.simulation;

import site.klade.simulation.Gene;
import site.klade.simulation.Genome;
import site.klade.simulation.MetaGenes;
import site.klade.simulation.Morphogen;

import java.util.List;
import java.util.Random;

/**
 * Backend-specific mutation logic for evolutionary algorithms.
 * Mutation is not part of the simulation library since it's only needed on the backend.
 */
public class GenomeMutator {

    private final Random random = new Random();

    private final double baseMetaGeneMutationChance;

    private final double baseMorphogenMutationChance;

    private final double baseGeneMutationChance;

    public GenomeMutator(double baseMetaGeneMutationChance,
                         double baseMorphogenMutationChance,
                         double baseGeneMutationChance) {
        this.baseMetaGeneMutationChance = baseMetaGeneMutationChance;
        this.baseMorphogenMutationChance = baseMorphogenMutationChance;
        this.baseGeneMutationChance = baseGeneMutationChance;
    }

    /**
     * Applies mutations to a genome based on the given factor.
     *
     * @param genome         the genome to mutate (will not be modified)
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     * @return a new mutated genome
     */
    public Genome mutate(Genome genome, double mutationFactor) {
        Genome mutated = new Genome(genome);
        mutateMetaGenes(mutated.getMetaGenes(), mutationFactor);
        mutateMorphogens(mutated.getMorphogens(), mutationFactor);
        mutateGenes(mutated.getGenes(), mutationFactor);
        return mutated;
    }

    /**
     * Mutates metaGenes
     *
     * @param metaGenes      the metaGenes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    private void mutateMetaGenes(MetaGenes metaGenes, double mutationFactor) {
        if (random.nextDouble() < baseMetaGeneMutationChance * mutationFactor) {
            float currentAngle = metaGenes.getInitialAngle();
            float maxDelta = 180.0f * (float) mutationFactor; // Max 180 degree change at full intensity
            float delta = (random.nextFloat() * 2.0f - 1.0f) * maxDelta;
            float newAngle = currentAngle + delta;
            // Normalize to 0-360 range
            newAngle = ((newAngle % 360.0f) + 360.0f) % 360.0f;
            metaGenes.setInitialAngle(newAngle);
        }
    }

    /**
     * Mutates morphogens (placeholder).
     *
     * @param morphogens     the morphogens to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    private void mutateMorphogens(List<Morphogen> morphogens, double mutationFactor) {
        // TODO: Implement morphogen mutation logic
    }

    /**
     * Mutates genes (placeholder).
     *
     * @param genes          the genes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    private void mutateGenes(List<Gene> genes, double mutationFactor) {
        // TODO: Implement gene mutation logic
    }

}
