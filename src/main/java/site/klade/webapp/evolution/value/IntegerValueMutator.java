package site.klade.webapp.evolution.value;

/**
 * Contiguous integer value mutation, rank-linear v2.2 §4.2 (SPEC-mutation.md §3, §8).
 *
 * <p>Drift uses a continuous embedding {@code k → uniform(k ± 0.5)} then
 * {@code k' = clip(floor(x' + 0.5), min, max)} (round half up). Sparse domains are
 * <b>not</b> handled here — route them through {@link EnumValueMutator} over the sorted values.</p>
 */
public final class IntegerValueMutator {

    private IntegerValueMutator() {
    }

    /** Full operator: replacement with probability {@code p.mutationFactor}, else drift. */
    public static int mutate(int value, IntParams p) {
        return MutationMechanics.isReplacement(p.mutationFactor) ? replace(value, p) : drift(value, p);
    }

    /**
     * Drift branch only (package-private so tests can isolate it).
     *
     * <p>The continuous embedding is what makes the freeze emergent: at {@code mf = 0} the displacement
     * is {@code 0}, the draw stays inside {@code [k-0.5, k+0.5)}, and round-half-up returns {@code k}.
     * It also makes a small step legitimately land back on {@code k} (INV-6).</p>
     */
    static int drift(int value, IntParams p) {
        final double embedded = MutationMechanics.uniform(value - 0.5, value + 0.5);
        final double amp = MutationMechanics.amplitude(p.mutationFactor, p.max - p.min);
        final int moved = (int) Math.floor(embedded + MutationMechanics.symmetric(amp) + 0.5);
        return Math.min(Math.max(moved, p.min), p.max);
    }

    /**
     * Replacement branch only: uniform over the whole range, self included. At {@code mf = 1.0} this
     * redraws the current value with probability {@code 1/n} — correct, not a no-op bug (INV-6).
     */
    static int replace(int value, IntParams p) {
        return MutationMechanics.inclusive(p.min, p.max);
    }
}