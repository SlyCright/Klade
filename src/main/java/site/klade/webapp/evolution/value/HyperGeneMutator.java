package site.klade.webapp.evolution.value;

/**
 * Self-adapting hyper-gene {@code R_max} mutation, rank-linear v2.2 §4.4 + §6.3
 * (SPEC-mutation.md §5, §8). Domain {@code (R_min, 1]}; the {@code r/2} floor applies to
 * <b>both</b> branches, so neither can more than halve {@code R_max} in one step.
 *
 * <p>Self-adapting: the asymmetric {@code r/2} floor in both branches protects the reproduction
 * schedule from degenerating toward {@code 0} (rank-linear v2.2 §6.3).</p>
 */
public final class HyperGeneMutator {

    /** Divisor for the drift floor (r/dff) to prevent degeneration. */
    public static final double DRIFT_FLOOR_FACTOR = 2.0;

    /** Absolute floor of the hyper-gene domain (SPEC-mutation.md OPEN-4; confirmed by the Architect). */
    public static final double R_MIN = 1e-6;

    private HyperGeneMutator() {
    }

    /** Full operator: replacement with probability {@code p.mutationFactor}, else drift. */
    public static double mutate(double value, HyperGeneParams p) {
        MutationMechanics.requireFinite(value, "R_max");
        return MutationMechanics.isReplacement(p.mutationFactor) ? replace(value, p) : drift(value, p);
    }

    /**
     * Drift branch only (package-private so tests can isolate it).
     *
     * <p>{@code r' = uniform( max(R_min, r - amp/2, r/2), min(1.0, r + amp/2) )}. The window is local
     * around {@code r}, but the {@code r/2} term sits in the <b>lower</b> bound too (Architect
     * correction): when {@code amp > r} it binds and the window is {@code [r/2, r + amp/2]}, so drift
     * can never more than halve {@code R_max} either.</p>
     *
     * <p>Consequence worth knowing: that floor pulls the drift mean slightly <i>above</i> {@code r}
     * at small {@code r}. It is intended anti-degeneration pressure, not a bias bug.</p>
     */
    static double drift(double value, HyperGeneParams p) {
        MutationMechanics.requireFinite(value, "R_max");
        final double amp = MutationMechanics.amplitude(p.mutationFactor, 1.0);
        final double lo = Math.max(Math.max(R_MIN, value - amp / 2.0), value / DRIFT_FLOOR_FACTOR);
        final double hi = Math.min(1.0, value + amp / 2.0);
        return MutationMechanics.uniform(lo, hi);
    }

    /**
     * Replacement branch only: {@code r' = uniform( max(R_min, r/2), 1.0 )} — at most a halving per
     * step, with {@code R_MIN} as the absolute floor (§6.3 anti-degeneration rule).
     */
    static double replace(double value, HyperGeneParams p) {
        MutationMechanics.requireFinite(value, "R_max");
        return MutationMechanics.uniform(Math.max(R_MIN, value / DRIFT_FLOOR_FACTOR), 1.0);
    }
}