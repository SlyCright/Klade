package site.klade.webapp.evolution.value;

/**
 * Immutable context for {@link BooleanValueMutator} (SPEC-mutation.md §4.3, §8).
 *
 * <p>{@code BooleanValueMutator} defaults to the <b>unordered</b> path (a boolean has no
 * intrinsic order), so its params need only carry {@code mutationFactor}.
 */
public final class BooleanParams {

    public final double mutationFactor;

    public BooleanParams(double mutationFactor) {
        if (Double.isNaN(mutationFactor) || mutationFactor < 0.0 || mutationFactor > 1.0) {
            throw new IllegalArgumentException("mutationFactor must be in [0,1]: " + mutationFactor);
        }
        this.mutationFactor = mutationFactor;
    }
}