package site.klade.webapp.evolution.value;

/**
 * Enum index mutation, rank-linear v2.2 §4.3 (SPEC-mutation.md §4, §8).
 *
 * <p>Operates on a 0-based index {@code k ∈ [0, n)}. The value is embedded into the normalized segment
 * {@code [0,1]} as a uniform point in its own slice {@code [k/n, (k+1)/n)}, displaced, then resolved
 * back to an index — the same shape as §4.2's integer embedding, which is what makes the two rules
 * consistent.</p>
 *
 * <p><b>Ordered</b> resolves by rounding the displaced position ({@code floor(x'·n)}) and clipping, so a
 * single step may cross <i>many</i> slices — the displacement is {@code floor(u + z)} in slice units,
 * not {@code ±1}. <b>Unordered</b> redraws uniformly over the whole domain. Replacement is
 * {@code uniform{0..n−1}} for both, <b>including</b> {@code k}: a redraw is legitimate
 * ({@code P = 1/n}), not a no-op bug.</p>
 *
 * <p>Consequence: for {@code n = 2} ordered and unordered coincide (both exit from a slice lands on the
 * other value), so a boolean needs no ordering caveat. For {@code n ≥ 3} they differ at boundaries —
 * clamping versus full redraw — which is a meaningful difference, not an artefact.</p>
 */
public final class EnumValueMutator {

    private EnumValueMutator() {
    }

    /** Full operator on index {@code k}: replacement with probability {@code p.mutationFactor}, else drift. */
    public static int mutate(int k, EnumParams p) {
        return MutationMechanics.isReplacement(p.mutationFactor) ? replace(k, p) : drift(k, p);
    }

    /**
     * Drift branch only (package-private so tests can isolate it).
     *
     * <p>Staying inside the slice is the legitimate "no visible change" case (INV-6); leaving it is an
     * exit. An <b>ordered</b> exit keeps the displacement — it rounds the displaced position and clips to
     * the domain, so large amplitudes move many slices. An <b>unordered</b> exit draws uniformly over the
     * whole domain, {@code k} included, with left and right treated identically.</p>
     */
    static int drift(int k, EnumParams p) {
        final int n = p.n;
        final double sliceWidth = 1.0 / n;
        final double embedded = MutationMechanics.uniform(k * sliceWidth, (k + 1) * sliceWidth)
                + MutationMechanics.symmetric(p.mutationFactor);
        if (embedded >= k * sliceWidth && embedded < (k + 1) * sliceWidth) {
            return k;                                   // stayed in slice — a legitimate small step
        }
        if (p.ordered) {
            final int moved = (int) Math.floor(embedded * n);
            return Math.min(Math.max(moved, 0), n - 1);
        }
        return MutationMechanics.index(n);
    }

    /** Replacement branch only: uniform over the whole domain, {@code k} included (INV-3). */
    static int replace(int k, EnumParams p) {
        return MutationMechanics.index(p.n);
    }
}