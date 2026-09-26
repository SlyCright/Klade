package site.klade.webapp.evolution;

import site.klade.simulation.Morphogen;

import java.util.List;
import java.util.Random;

/**
 * Handles mutation of morphogens (structural changes: addition and deletion).
 * <p>
 * Morphogens control the developmental patterns in the simulation.
 */
public class MorphogenMutator {

    private final Random random = new Random();

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
     *   <li>Deletion: {@code p_del = 1 - (1 - a)^(1 + a)} — super-linear in {@code a},
     *       so deletion is slightly more common than addition for the same factor.</li>
     *   <li>Addition: {@code p_add = a}.</li>
     * </ul>
     *
     * @param morphogens     the morphogens to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    public void mutateMorphogens(List<Morphogen> morphogens, double mutationFactor) {
        // TODO: Implement morphogen mutation logic
        // Placeholder
    }
}
