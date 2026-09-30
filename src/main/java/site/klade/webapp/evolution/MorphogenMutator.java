package site.klade.webapp.evolution;

import site.klade.simulation.Genome;
import site.klade.simulation.Morphogen;

import java.util.List;

/**
 * Handles the morphogen half of the mutation pipeline.
 *
 * <p><b>Two responsibilities, matching the two mutators genes have:</b></p>
 * <ol>
 *   <li><b>Structure</b> — aligning the definition set with the genome's references. This is
 *       {@link Genome#deriveMorphogens()}, and it is <i>deterministic</i>: a definition exists because a
 *       gene references it, and disappears when the last reference goes. There is deliberately no
 *       add/delete probability here. The previous {@code p_deletion}/{@code p_addition} scheme could add a
 *       morphogen nobody referenced (immediately removed again) or delete a referenced one (immediately
 *       re-created), so it could not affect anything.</li>
 *   <li><b>Values</b> — mutating each definition's diffusion and decay. Not implemented yet; see the
 *       plan's Step 4.</li>
 * </ol>
 *
 * <p>The signature takes the whole {@link Genome} rather than the morphogen list because structural
 * alignment needs the gene list to read references from.</p>
 */
public class MorphogenMutator {

    /**
     * Aligns the morphogen set with the genome's references, then mutates the surviving values.
     *
     * @param genome         the genome whose morphogens to align and mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    public void mutateMorphogens(Genome genome, double mutationFactor) {
        // Step 4.1 (MorphogenValueMutator) is not implemented yet, so only structure is handled here.
        genome.deriveMorphogens();
    }

    /**
     * Convenience overload kept for callers that hold only the list. It cannot derive, so it is a no-op
     * until value mutation exists.
     *
     * <p>TODO(step-4): remove this overload once {@code MorphogenValueMutator} lands and the genome-based
     * call is the only one.</p>
     */
    public void mutateMorphogens(List<Morphogen> morphogens, double mutationFactor) {
        // Intentionally empty: without the gene list there is nothing that can be derived or mutated.
    }
}
