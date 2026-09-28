package site.klade.webapp.evolution;

import site.klade.simulation.MetaGenes;
import site.klade.webapp.evolution.value.DoubleParams;
import site.klade.webapp.evolution.value.DoubleValueMutator;
import site.klade.webapp.evolution.value.HyperGeneMutator;
import site.klade.webapp.evolution.value.HyperGeneParams;
import site.klade.webapp.evolution.value.LoopMode;

/**
 * Handles mutation of meta-genes (hyper-gene and initial angle).
 * <p>
 * Meta-genes control all general aspects of the specimen.
 * <p>
 * This class delegates to the value mutation subsystem for consistent
 * rank-linear mutation mechanics.
 */
public class MetaGeneMutator {

    /**
     * Mutates the hyper-gene (R_max) according to rank-linear operator v2.2.
     *
     * <p>The hyper-gene controls the maximum mutation probability for regular genes.
     * Its domain is (R_min, 1] where R_min = 1e-6.</p>
     *
     * @param metaGenes      the metaGenes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    public void mutateHyperGene(MetaGenes metaGenes, double mutationFactor) {
        HyperGeneParams params = new HyperGeneParams(mutationFactor);
        float newRMax = (float) HyperGeneMutator.mutate(metaGenes.getHyperGene(), params);
        metaGenes.setHyperGene(newRMax);
    }

    /**
     * Mutates meta-genes (currently only initial angle, but extensible for future meta-genes).
     *
     * @param metaGenes      the metaGenes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    public void mutateMetaGenes(MetaGenes metaGenes, double mutationFactor) {
        mutateInitialAngle(metaGenes, mutationFactor);
        // here comes other meta-genes in the future
    }

    /**
     * Mutates the initial angle (continuous gene with circular domain).
     *
     * @param metaGenes      the metaGenes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    public void mutateInitialAngle(MetaGenes metaGenes, double mutationFactor) {
        DoubleParams params = new DoubleParams(0.0, 360.0, LoopMode.LOOP, mutationFactor);
        float newAngle = (float) DoubleValueMutator.mutate(metaGenes.getInitialAngle(), params);
        metaGenes.setInitialAngle(newAngle);
    }
}
