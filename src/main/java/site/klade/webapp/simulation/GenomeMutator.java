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
        float sourceHyperGene = genome.getMetaGenes().getHyperGene();
        double initialMutationFactor = sourceHyperGene * rank;
        // Mutate hyper-gene first using initial factor
        mutateHyperGene(mutated.getMetaGenes(), initialMutationFactor);
        // Use UPDATED hyper-gene for subsequent mutations (shorter feedback loop)
        double updatedMutationFactor = mutated.getMetaGenes().getHyperGene() * rank;
        mutateMetaGenes(mutated.getMetaGenes(), updatedMutationFactor);
        mutateMorphogens(mutated.getMorphogens(), updatedMutationFactor);
        mutateGenes(mutated.getGenes(), updatedMutationFactor);
        return mutated;
    }

    /**
     * Mutates metaGenes according to rank-linear operator v2.2
     *
     * @param metaGenes      the metaGenes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    private void mutateMetaGenes(MetaGenes metaGenes, double mutationFactor) {
        mutateInitialAngle(metaGenes, mutationFactor);
    }

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
     */
    private void mutateHyperGene(MetaGenes metaGenes, double mutationFactor) {
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
     * Mutates the initial angle (continuous gene with circular domain)
     */
    private void mutateInitialAngle(MetaGenes metaGenes, double mutationFactor) {
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

    /**
     * Mutates the morphogen list.
     *
     * <p>Structural mutation is performed first: deletion and addition of whole morphogens.
     * Per-gene (field-level) mutation of morphogen parameters is not part of this step.</p>
     *
     * <p>Step 6.0: at {@code mutationFactor = 0.0} the list is frozen (no structural changes).</p>
     *
     * <p>Probabilities:</p>
     * <ul>
     *   <li>Deletion: {@code p_del = 1 - (1 - a)^(1 + a)} — sub-linear in {@code a},
     *       so deletion is rarer than addition for the same factor.</li>
     *   <li>Addition: {@code p_add = a}.</li>
     * </ul>
     *
     * @param morphogens     the morphogens to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    private void mutateMorphogens(List<Morphogen> morphogens, double mutationFactor) {
        if (mutationFactor <= 0.0) {
            return;
        }
        // Deletion first (at least one morphogen is always kept)
        double deletionChance = 1.0 - Math.pow(1.0 - mutationFactor, 1.0 + mutationFactor);
        if (morphogens.size() > 1 && random.nextDouble() < deletionChance) {
            deleteMorphogen(morphogens, mutationFactor);
        }
        // Addition
        if (random.nextDouble() < mutationFactor) {
            addMorphogen(morphogens, mutationFactor);
        }
    }

    /**
     * Deletes one morphogen.
     *
     * <p>Choice of the victim is a two-option enum (see section 4.3 of the specification,
     * n = 2) driven by the standard drift vs replace split of step 6.1:</p>
     * <ul>
     *   <li>Drift (probability {@code 1 - mutationFactor}): local change —
     *       the last morphogen in the list is removed.</li>
     *   <li>Replace (probability {@code mutationFactor}): global change —
     *       a uniformly random morphogen is removed.</li>
     * </ul>
     */
    private void deleteMorphogen(List<Morphogen> morphogens, double mutationFactor) {
        boolean isReplace = random.nextDouble() < mutationFactor;
        int index = isReplace ? random.nextInt(morphogens.size()) : morphogens.size() - 1;
        morphogens.remove(index);
    }

    /**
     * Adds one morphogen.
     *
     * <p>Choice of the source is a two-option enum (see section 4.3 of the specification,
     * n = 2) driven by the standard drift vs replace split of step 6.1:</p>
     * <ul>
     *   <li>Drift (probability {@code 1 - mutationFactor}): local change —
     *       an existing morphogen is duplicated (its parameters are preserved).</li>
     *   <li>Replace (probability {@code mutationFactor}): global change —
     *       a brand-new morphogen with random parameters is created.</li>
     * </ul>
     *
     * <p>The new morphogen always receives a fresh id ({@code max(existing ids) + 1}) so that
     * morphogen identity is never shared within a genome.</p>
     */
    private void addMorphogen(List<Morphogen> morphogens, double mutationFactor) {
        boolean isReplace = random.nextDouble() < mutationFactor;
        Morphogen newMorphogen;
        if (!isReplace && !morphogens.isEmpty()) {
            // Drift: duplicate an existing morphogen (local, structure-preserving)
            newMorphogen = new Morphogen(morphogens.get(random.nextInt(morphogens.size())));
        } else {
            // Replace: brand-new morphogen with random ratios; or drift is impossible (empty list)
            newMorphogen = new Morphogen(0, random.nextFloat(), random.nextFloat(), "everywhere");
        }
        newMorphogen.setId(nextMorphogenId(morphogens));
        morphogens.add(newMorphogen);
    }

    private int nextMorphogenId(List<Morphogen> morphogens) {
        int maxId = 0;
        for (Morphogen morphogen : morphogens) {
            maxId = Math.max(maxId, morphogen.getId());
        }
        return maxId + 1;
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
