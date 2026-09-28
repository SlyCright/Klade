package site.klade.webapp.evolution.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static site.klade.webapp.evolution.value.ValueMutationTestSupport.N;
import static site.klade.webapp.evolution.value.ValueMutationTestSupport.assertProportion;

/**
 * M2 red tests for {@link HyperGeneMutator} — rank-linear v2.2 §4.4 + §6.3
 * ({@code SPEC-mutation.md} §5, §7).
 *
 * <p>The Architect's correction is load-bearing here: the {@code r/2} floor applies to <b>drift</b>
 * as well as replacement, so <b>neither branch can more than halve {@code R_max} in one step</b>.
 * The vault's original drift (without {@code r/2}) violates that.</p>
 */
class HyperGeneMutatorTest {

    private static final double R_MIN = 1e-6;

    @Nested
    @DisplayName("domain floor and anti-degeneration (§6.3)")
    class AntiDegeneration {

        @Test
        void rMinConstantIsTheDocumentedValue() {
            assertEquals(1e-6, HyperGeneMutator.R_MIN, 0.0);
        }

        @Test
        void driftNeverGoesBelowRMin() {
            for (double r : new double[]{1e-6, 1e-5, 0.001, 0.5, 1.0}) {
                HyperGeneParams p = new HyperGeneParams(1.0);
                for (int i = 0; i < 50_000; i++) {
                    double v = HyperGeneMutator.drift(r, p);
                    assertTrue(v >= R_MIN, "drift below R_MIN: " + v + " (r=" + r + ")");
                }
            }
        }

        @Test
        void driftNeverExceedsOne() {
            HyperGeneParams p = new HyperGeneParams(1.0);
            for (int i = 0; i < N; i++) {
                double v = HyperGeneMutator.drift(0.99, p);
                assertTrue(v <= 1.0, "drift above 1.0: " + v);
            }
        }

        /**
         * THE ARCHITECT'S CORRECTION: drift cannot drop below {@code r/2}, so a drift step can never
         * more than halve {@code R_max}. With the OLD formula this failed — the window's lower bound
         * was {@code max(R_MIN, r - amp/2)}, which for {@code r = 0.001, mf = 1.0} reaches almost 0.
         */
        @Test
        void driftNeverMoreThanHalvesTheValue() {
            double r = 0.001;
            HyperGeneParams p = new HyperGeneParams(1.0);
            for (int i = 0; i < N; i++) {
                double v = HyperGeneMutator.drift(r, p);
                assertTrue(v >= r / 2 - 1e-12,
                        "drift more than halved r: got " + v + ", floor is " + (r / 2));
            }
        }

        /** Replacement carries the same {@code r/2} multiplicative floor. */
        @Test
        void replacementNeverMoreThanHalvesTheValue() {
            for (double r : new double[]{1.0, 0.5, 0.1, 0.001}) {
                HyperGeneParams p = new HyperGeneParams(0.5);
                for (int i = 0; i < 50_000; i++) {
                    double v = HyperGeneMutator.replace(r, p);
                    assertTrue(v >= Math.max(R_MIN, r / 2) - 1e-12,
                            "replacement below max(R_MIN, r/2): " + v + " (r=" + r + ")");
                    assertTrue(v <= 1.0, "replacement above 1.0: " + v);
                }
            }
        }

        /** The floor must hold over a long chain of steps, not just one (E7). */
        @Test
        void theFloorHoldsAcrossManySequentialSteps() {
            double r = 0.5;
            HyperGeneParams p = new HyperGeneParams(1.0);
            for (int i = 0; i < 200_000; i++) {
                double next = HyperGeneMutator.mutate(r, p);
                assertTrue(next >= R_MIN, "step " + i + ": below R_MIN -> " + next);
                assertTrue(next >= r / 2 - 1e-12,
                        "step " + i + ": more than halved " + r + " -> " + next);
                r = next;
            }
        }
    }

    @Nested
    @DisplayName("emergent freeze and the full operator")
    class FreezeAndOperator {

        @Test
        void driftAtZeroFactorIsIdentity() {
            HyperGeneParams p = new HyperGeneParams(0.0);
            for (int i = 0; i < 10_000; i++) {
                assertEquals(0.25, HyperGeneMutator.drift(0.25, p), 0.0);
            }
        }

        @Test
        void mutateAtZeroFactorIsIdentity() {
            HyperGeneParams p = new HyperGeneParams(0.0);
            for (int i = 0; i < 10_000; i++) {
                assertEquals(0.25, HyperGeneMutator.mutate(0.25, p), 0.0);
            }
        }

        /** Drift is local: with a small factor the window is narrow around r. */
        @Test
        void driftIsLocalAtSmallFactor() {
            double r = 0.5;
            HyperGeneParams p = new HyperGeneParams(0.25);          // amp = 0.25
            for (int i = 0; i < N; i++) {
                double v = HyperGeneMutator.drift(r, p);
                assertTrue(Math.abs(v - r) <= 0.25 / 2 + 1e-12,
                        "drift exceeded amp/2: " + v + " vs r=" + r);
            }
        }

        /** Replacement is allowed to jump far: from a small r it spreads over [r/2, 1]. */
        @Test
        void replacementCanMoveFar() {
            double r = 0.01;
            HyperGeneParams p = new HyperGeneParams(0.5);
            int aboveHalf = 0;
            for (int i = 0; i < N; i++) {
                if (HyperGeneMutator.replace(r, p) > 0.5) aboveHalf++;
            }
            // replacement is uniform on [max(R_MIN, r/2), 1] = [0.005, 1];
            // P(> 0.5) = (1 - 0.5) / (1 - 0.005) = 0.50251
            assertProportion("hyper-gene replacement P(> 0.5) from r = 0.01",
                    0.5025, aboveHalf / (double) N);
        }

        /**
         * At {@code mf = 1.0} the replacement branch always wins. The discriminator must be a region
         * where the two windows DIFFER: from {@code r = 0.2} replacement spans {@code [0.1, 1]} while
         * drift spans only {@code [0.1, 0.7]}, so {@code P(value > 0.7)} is {@code 0.3333} for
         * replacement and exactly {@code 0} for drift.
         *
         * <p><b>Correction:</b> my first version of this test used {@code r = 0.9}, where the two
         * windows are <i>identical</i> ({@code [0.45, 1]} both) — the assertion would have been
         * vacuous. Verified before shipping.</p>
         */
        @Test
        void mutateAtMaximumFactorUsesReplacementBranch() {
            HyperGeneParams p = new HyperGeneParams(1.0);
            int aboveDriftCeiling = 0;
            for (int i = 0; i < N; i++) {
                if (HyperGeneMutator.mutate(0.2, p) > 0.7) aboveDriftCeiling++;
            }
            assertProportion("mf=1.0 hyper-gene P(> 0.7) from r = 0.2 — drift cannot reach here",
                    0.3 / 0.9, aboveDriftCeiling / (double) N);
        }

        /** Companion: the drift branch genuinely CANNOT exceed its ceiling at the same parameters. */
        @Test
        void driftBranchCannotExceedItsCeilingAtMaximumFactor() {
            HyperGeneParams p = new HyperGeneParams(1.0);
            for (int i = 0; i < N; i++) {
                assertTrue(HyperGeneMutator.drift(0.2, p) <= 0.7 + 1e-12,
                        "drift from r=0.2, mf=1.0 exceeded min(1, r+amp/2) = 0.7");
            }
        }

        /** The bias note: at small r the r/2 floor pulls the drift MEAN above r (SPEC §5). */
        @Test
        void driftMeanIsSlightlyAboveTheStartingValueAtSmallR() {
            double r = 0.01;
            HyperGeneParams p = new HyperGeneParams(0.5);
            double sum = 0;
            for (int i = 0; i < N; i++) {
                sum += HyperGeneMutator.drift(r, p);
            }
            double mean = sum / N;
            assertTrue(mean > r, "expected the anti-degeneration floor to bias drift upward, got " + mean);
            // Measured in ORACLE/SPEC: r=0.01, mf=0.5 -> the r/2 floor makes the window [0.005, 0.26]
            // whose mean is ~0.1325 (NOT 0.01). Assert it is well above r.
            assertTrue(mean > 0.10, "drift mean should be well above r at small r, got " + mean);
        }
    }

    @Nested
    @DisplayName("validation (E4, E5)")
    class Validation {

        @Test
        void outOfRangeMutationFactorIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> HyperGeneMutator.mutate(0.5, new HyperGeneParams(1.5)));
            assertThrows(IllegalArgumentException.class,
                    () -> HyperGeneMutator.mutate(0.5, new HyperGeneParams(-0.1)));
        }

        @Test
        void nanMutationFactorIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> HyperGeneMutator.mutate(0.5, new HyperGeneParams(Double.NaN)));
        }

        @Test
        void nonFiniteValueIsRejected() {
            HyperGeneParams p = new HyperGeneParams(0.5);
            assertThrows(IllegalArgumentException.class, () -> HyperGeneMutator.mutate(Double.NaN, p));
            assertThrows(IllegalArgumentException.class,
                    () -> HyperGeneMutator.mutate(Double.POSITIVE_INFINITY, p));
        }
    }
}
