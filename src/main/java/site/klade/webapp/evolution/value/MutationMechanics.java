package site.klade.webapp.evolution.value;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The shared stochastic mechanics of the rank-linear mutation operator: the Step 6.1 branch trial,
 * the Step 6.2 amplitude law, and the draw primitives every type-specific mutator needs
 * (rank-linear v2.2 §3; SPEC-mutation.md §1).
 *
 * <p><b>Why this class exists:</b> without it, each of the five mutators re-derives the same
 * {@code random < mutationFactor} branch and the same {@code amplitude = mutationFactor × baseRange}
 * rule — which is how four separate implementations drift apart.</p>
 *
 * <p><b>Why the RNG lives here and is never passed:</b> the public API is deliberately two parameters
 * ({@code mutate(value, params)}). A {@code Random} argument would make it three, and would exist only
 * to make tests reproducible — bending the production API for a test convenience. Correctness is
 * therefore asserted statistically over large N (INV-5), and the proportions in {@code ORACLE.md} are
 * seed-independent.</p>
 *
 * <p><b>Why {@link ThreadLocalRandom}:</b> it is allocation-free per call, contention-free across
 * threads, and needs no seeding. It is a utility, so it is obtained at the point of use rather than
 * threaded through every call.</p>
 *
 * <p><b>Why freezing needs no branch:</b> at {@code mutationFactor == 0.0} the branch trial
 * {@code nextDouble() < 0.0} is always false, so the drift branch runs; and the amplitude is {@code 0},
 * so the drift window collapses onto the input and {@code uniform(x, x) == x} bit-exactly. Identity at
 * zero is therefore a property of the ordinary arithmetic, not a special case (INV-1).</p>
 */
final class MutationMechanics {

    private MutationMechanics() {
    }

    /**
     * Step 6.1 — is this step a replacement? True with probability {@code mutationFactor}.
     *
     * <p>At {@code 1.0} this is deterministically true, because {@code nextDouble()} is in {@code [0,1)}
     * and hence always less than {@code 1.0} (F3). At {@code 0.0} it is deterministically false, which is
     * what routes {@code mf = 0} to drift and makes the freeze emergent.</p>
     */
    static boolean isReplacement(double mutationFactor) {
        return ThreadLocalRandom.current().nextDouble() < mutationFactor;
    }

    /** A uniform draw from {@code [0,1)}. */
    static double unit() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * Step 6.2 — a symmetric displacement of total width {@code amp}, i.e. uniform over
     * {@code [-amp/2, +amp/2]}. At {@code amp == 0} this is exactly {@code 0.0}, which is what makes the
     * drift window collapse and the freeze emergent.
     */
    static double symmetric(double amp) {
        return (unit() * 2.0 - 1.0) * (amp / 2.0);
    }

    /** Step 6.2 — {@code amplitude = mutationFactor × baseRange}. */
    static double amplitude(double mutationFactor, double baseRange) {
        return mutationFactor * baseRange;
    }

    /**
     * A uniform draw over {@code [lo, hi]}. When {@code lo == hi} this returns {@code lo} exactly, which
     * is the arithmetic the emergent freeze relies on.
     */
    static double uniform(double lo, double hi) {
        return lo + unit() * (hi - lo);
    }

    /**
     * A uniform index over {@code {0, …, size-1}} — the whole domain, <b>self included</b>, as §4.2 and
     * the current §4.3 both require for replacement. Returns {@code 0} for a degenerate domain, so the
     * caller still produces the only legal value (E1).
     */
    static int index(int size) {
        return size <= 1 ? 0 : ThreadLocalRandom.current().nextInt(size);
    }

    /** Uniform over the contiguous integers {@code {min, …, max}}, self included (E2, INV-6). */
    static int inclusive(int min, int max) {
        return min + ThreadLocalRandom.current().nextInt(max - min + 1);
    }

    /**
     * Rejects a non-finite input value (INV-4). `null`-like and undefined inputs must fail loudly rather
     * than propagate {@code NaN} through the genome — a NaN weight would silently poison every later
     * simulation step and is far harder to trace back than an exception at the boundary.
     */
    static double requireFinite(double value, String what) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(what + " must be finite: " + value);
        }
        return value;
    }
}