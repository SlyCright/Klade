package site.klade.webapp.evolution.value;

/**
 * Immutable context for {@link HyperGeneMutator} (SPEC-mutation.md §4.4/§6.3, §8).
 *
 * <p>The hyper-gene domain is fixed ({@code (R_min, 1]}, {@code R_min = 1e-6}), so the params
 * need only carry {@code mutationFactor}.
 */
public final class HyperGeneParams {

    public final double mutationFactor;

    public HyperGeneParams(double mutationFactor) {
        if (Double.isNaN(mutationFactor) || mutationFactor < 0.0 || mutationFactor > 1.0) {
            throw new IllegalArgumentException("mutationFactor must be in [0,1]: " + mutationFactor);
        }
        this.mutationFactor = mutationFactor;
    }
}