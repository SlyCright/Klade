package site.klade.webapp.evolution.value;

/**
 * Immutable context for {@link DoubleValueMutator} (SPEC-mutation.md §4.1, §8).
 *
 * <p>Bundles the continuous domain + {@code mutationFactor} so the public API stays at exactly
 * two parameters: {@code DoubleValueMutator.mutate(value, params)}. The RNG is internal to the
 * mutator and is <b>not</b> part of this object.
 */
public final class DoubleParams {

    public final double min;
    public final double max;
    public final LoopMode rangeMode;
    public final double mutationFactor;

    public DoubleParams(double min, double max, LoopMode rangeMode, double mutationFactor) {
        checkMutationFactor(mutationFactor);
        this.min = min;
        this.max = max;
        this.rangeMode = rangeMode;
        this.mutationFactor = mutationFactor;
    }

    private static void checkMutationFactor(double mf) {
        if (Double.isNaN(mf) || mf < 0.0 || mf > 1.0) {
            throw new IllegalArgumentException("mutationFactor must be in [0,1]: " + mf);
        }
    }
}