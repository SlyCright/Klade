package site.klade.webapp.evolution.value;

/**
 * Continuous value mutation, rank-linear v2.2 §4.1 (SPEC-mutation.md §2, §8).
 *
 * <p>Two range realisations, chosen by {@link DoubleParams#rangeMode}:
 * {@link LoopMode#CLAMP} (clip into {@code [min, max]}) or {@link LoopMode#LOOP} (circular wrap).
 * Freezing at {@code mutationFactor = 0.0} is <b>emergent</b> (no special-case {@code if}).</p>
 */
public final class DoubleValueMutator {

    private DoubleValueMutator() {
    }

    /** Full operator: replacement with probability {@code p.mutationFactor}, else drift. */
    public static double mutate(double value, DoubleParams p) {
        MutationMechanics.requireFinite(value, "value");
        return MutationMechanics.isReplacement(p.mutationFactor) ? replace(value, p) : drift(value, p);
    }

    /**
     * Drift branch only (package-private so tests can isolate it).
     *
     * <p>{@code δ = uniform(-amp/2, +amp/2)}, {@code x' = x + δ}, then the range rule:
     * CLAMP clips into the domain, LOOP wraps around the {@code min ↔ max} border.</p>
     */
    static double drift(double value, DoubleParams p) {
        MutationMechanics.requireFinite(value, "value");
        final double moved = value + MutationMechanics.symmetric(
                MutationMechanics.amplitude(p.mutationFactor, p.max - p.min));
        if (p.rangeMode == LoopMode.LOOP) {
            return p.max > p.min ? wrap(moved, p.min, p.max) : p.min;
        }
        return clip(moved, p.min, p.max);
    }

    /** Replacement branch only: uniform over the whole domain, self included (measure-zero). */
    static double replace(double value, DoubleParams p) {
        MutationMechanics.requireFinite(value, "value");
        return MutationMechanics.uniform(p.min, p.max);
    }

    private static double clip(double v, double min, double max) {
        return v < min ? min : (v > max ? max : v);
    }

    /**
     * {@code min + floorMod(v - min, max - min)} in floating point — {@code Math.floorMod} has no
     * {@code double} overload, so the non-negative remainder is computed by hand. The result is always
     * in {@code [min, max)}.
     */
    private static double wrap(double v, double min, double max) {
        final double range = max - min;
        double remainder = (v - min) % range;
        if (remainder < 0) {
            remainder += range;
        }
        return min + remainder;
    }

}