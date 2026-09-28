package site.klade.webapp.evolution.value;

/**
 * Immutable context for {@link EnumValueMutator} (SPEC-mutation.md §4.3, §8).
 *
 * <p>Operates on a 0-based index {@code k ∈ [0, n)} into an {@code n}-value domain. {@code n ≥ 2}
 * to mutate (validated in M3). Ordered drift exits to a neighbour, unordered exits to a uniform
 * over the <b>others</b>; replacement is always uniform over the others.
 */
public final class EnumParams {

    public final int n;
    public final boolean ordered;
    public final double mutationFactor;

    public EnumParams(int n, boolean ordered, double mutationFactor) {
        if (Double.isNaN(mutationFactor) || mutationFactor < 0.0 || mutationFactor > 1.0) {
            throw new IllegalArgumentException("mutationFactor must be in [0,1]: " + mutationFactor);
        }
        this.n = n;
        this.ordered = ordered;
        this.mutationFactor = mutationFactor;
    }
}