package site.klade.webapp.evolution;

import site.klade.simulation.MetaGenes;

import java.util.Random;

/**
 * Handles mutation of meta-genes (hyper-gene and initial angle).
 * <p>
 * Meta-genes control all general aspects of the specimen.
 */
public class MetaGeneMutator {

    private final Random random = new Random();

    /**
     * Mutates the hyper-gene (R_max) according to rank-linear operator v2.2.
     *
     * <p>The hyper-gene controls the maximum mutation probability for regular genes.
     * Its domain is (R_min, 1] where R_min = 1e-6.</p>
     *
     * <p>Algorithm (section 6.3 of the specification):</p>
     * <ol>
     *   <li>With probability {@code replacement_probability = mutation_factor}: <b>replacement</b> —
     *       new value chosen uniformly from [max(R_min, R_max/2), 1.0]</li>
     *   <li>With probability {@code 1 - mutation_factor}: <b>drift</b> —
     *       new value chosen uniformly from
     *       [max(R_min, R_max - drift_amplitude/2), min(1.0, R_max + drift_amplitude/2)]
     *       where {@code drift_amplitude = mutation_factor × (1.0 - R_min)}</li>
     * </ol>
     *
     * @param metaGenes      the metaGenes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    public void mutateHyperGene(MetaGenes metaGenes, double mutationFactor) {
        final float R_MIN = 1e-6f;
        float rMax = metaGenes.getHyperGene();
        boolean isReplacement = random.nextDouble() < mutationFactor;
        float newRMax;
        if (isReplacement) {
            // Replacement: uniform random from [max(R_min, R_max/2), 1.0]
            float lowerBound = Math.max(R_MIN, rMax / 2.0f);
            newRMax = lowerBound + random.nextFloat() * (1.0f - lowerBound);
        } else {
            // Drift: uniform random from [max(R_min, R_max - drift_amplitude/2), min(1.0, R_max + drift_amplitude/2)]
            // where drift_amplitude = mutation_factor × (1.0 - R_min)
            float driftAmplitude = (float) (mutationFactor * (1.0 - R_MIN));
            float lowerBound = Math.max(R_MIN, rMax - driftAmplitude / 2.0f);
            float upperBound = Math.min(1.0f, rMax + driftAmplitude / 2.0f);
            newRMax = lowerBound + random.nextFloat() * (upperBound - lowerBound);
        }
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
    }

    /**
     * Mutates the initial angle (continuous gene with circular domain).
     *
     * @param metaGenes      the metaGenes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    public void mutateInitialAngle(MetaGenes metaGenes, double mutationFactor) {
        // Step 6.1: Determine mutation type (replacement vs drift)
        boolean isReplacement = random.nextDouble() < mutationFactor;
        float currentAngle = metaGenes.getInitialAngle();
        float minAngle = 0.0f;
        float maxAngle = 360.0f;
        float baseRange = maxAngle - minAngle;
        if (isReplacement) {
            // Replacement: uniform random across entire domain
            float newAngle = minAngle + random.nextFloat() * baseRange;
            metaGenes.setInitialAngle(newAngle);
        } else {
            // Drift: small change proportional to mutation_factor
            float driftAmplitude = (float) (mutationFactor * baseRange);
            float delta = (random.nextFloat() - 0.5f) * driftAmplitude;
            float newAngle = currentAngle + delta;
            // Normalize to 0-360 range (circular wrapping)
            newAngle = ((newAngle % 360.0f) + 360.0f) % 360.0f;
            metaGenes.setInitialAngle(newAngle);
        }
    }
}
