package site.klade.webapp.evolution.value;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test-only statistical helpers for the value-mutation sub-system (M2, {@code SPEC-mutation.md} §7).
 *
 * <p><b>Why the expected values are constants and not recomputed:</b> a test that re-derives the
 * distribution from the same formula it is testing proves only that two copies of one misunderstanding
 * agree. So the expected proportions live in {@code ORACLE.md} (or are derived analytically in the test)
 * and are asserted here as recorded numbers.</p>
 *
 * <p><b>Why no fixed seed:</b> the Architect removed the injected {@code Random} — the RNG is internal
 * by design, so a test must not depend on a seed. That is only safe if the sample is large enough that
 * the tolerance is many standard deviations wide. With {@code N = 400_000} and {@code TOL = 0.01}, the
 * tolerance is &#8805;13&#963; for every proportion asserted in this suite
 * ({@code sigma = sqrt(p(1-p)/N)}), so flake probability is negligible.</p>
 */
final class ValueMutationTestSupport {

    /** Sample size for statistical assertions. Pinned here so the safety margin is explicit. */
    static final int N = 400_000;

    /** Absolute tolerance on a proportion. See the class javadoc for the sigma arithmetic. */
    static final double TOL = 0.01;

    private ValueMutationTestSupport() {
    }

    /**
     * Asserts {@code actual ~= expected} within {@link #TOL}, reporting the z-score on failure so a
     * genuine regression is distinguishable from an unlucky sample.
     */
    static void assertProportion(String what, double expected, double actual) {
        double sigma = Math.sqrt(expected * (1.0 - expected) / N);
        double delta = Math.abs(actual - expected);
        assertTrue(delta <= TOL, () -> String.format(
                "%s: expected ~%.4f, measured %.4f (delta %.4f > tol %.4f; %.1f sigma)",
                what, expected, actual, delta, TOL, delta / sigma));
    }

    /**
     * Asserts a proportion within an explicit tolerance, for cases where the sample differs from
     * {@link #N} or the quantity is not a clean binomial proportion.
     */
    static void assertProportion(String what, double expected, double actual, double tol, int n) {
        double sigma = Math.sqrt(expected * (1.0 - expected) / n);
        double delta = Math.abs(actual - expected);
        assertTrue(delta <= tol, () -> String.format(
                "%s: expected ~%.4f, measured %.4f (delta %.4f > tol %.4f; %.1f sigma)",
                what, expected, actual, delta, tol, delta / sigma));
    }

    /** Smallest absolute distance between {@code a} and {@code b} on a circle of the given period. */
    static double circularDistance(double a, double b, double period) {
        double d = Math.abs(a - b) % period;
        return Math.min(d, period - d);
    }
}
