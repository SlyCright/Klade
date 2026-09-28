package site.klade.webapp.evolution.value;

/**
 * Immutable context for {@link IntegerValueMutator} (SPEC-mutation.md §4.2, §8).
 *
 * <p>Bundles the contiguous integer domain + {@code mutationFactor} so the public API stays at
 * exactly two parameters: {@code IntegerValueMutator.mutate(value, params)}.
 */
public final class IntParams {

    public final int min;
    public final int max;
    public final double mutationFactor;

    public IntParams(int min, int max, double mutationFactor) {
        if (Double.isNaN(mutationFactor) || mutationFactor < 0.0 || mutationFactor > 1.0) {
            throw new IllegalArgumentException("mutationFactor must be in [0,1]: " + mutationFactor);
        }
        this.min = min;
        this.max = max;
        this.mutationFactor = mutationFactor;
    }
}