package site.klade.webapp.evolution.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static site.klade.webapp.evolution.value.ValueMutationTestSupport.N;
import static site.klade.webapp.evolution.value.ValueMutationTestSupport.assertProportion;
import static site.klade.webapp.evolution.value.ValueMutationTestSupport.circularDistance;

/**
 * M2 red tests for {@link DoubleValueMutator} — rank-linear v2.2 §4.1
 * ({@code SPEC-mutation.md} §2, §7; fixture {@code ORACLE.md}).
 */
class DoubleValueMutatorTest {

    // ---------------------------------------------------------------- 1. emergent freeze (INV-1)

    @Nested
    @DisplayName("emergent freeze at mf = 0 (no special-case if)")
    class EmergentFreeze {

        @Test
        void clampDriftAtZeroFactorIsIdentity() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 0.0);
            assertEquals(0.25, DoubleValueMutator.drift(0.25, p), 0.0);
        }

        @Test
        void loopDriftAtZeroFactorIsIdentity() {
            DoubleParams p = new DoubleParams(0.0, 360.0, LoopMode.LOOP, 0.0);
            assertEquals(123.456, DoubleValueMutator.drift(123.456, p), 0.0);
        }

        /** Full operator at mf = 0: replacement probability is 0, so drift runs and is identity. */
        @Test
        void mutateAtZeroFactorIsIdentityOverManyDraws() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 0.0);
            for (int i = 0; i < 10_000; i++) {
                assertEquals(0.25, DoubleValueMutator.mutate(0.25, p), 0.0);
            }
        }

        /** At mf = 0 the value must be *exactly* unchanged, not merely close. */
        @Test
        void freezeIsBitExactNotApproximate() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 0.0);
            for (int i = 0; i < 1_000; i++) {
                assertEquals(Double.doubleToLongBits(0.1),
                        Double.doubleToLongBits(DoubleValueMutator.mutate(0.1, p)));
            }
        }
    }

    // ---------------------------------------------------------------- 2. CLAMP

    @Nested
    @DisplayName("CLAMP: bounded quantity")
    class Clamp {

        @Test
        void driftStaysInsideDomain() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 0.75);
            for (int i = 0; i < 100_000; i++) {
                double v = DoubleValueMutator.drift(0.5, p);
                assertTrue(v >= 0.0 && v <= 1.0, "drift left the domain: " + v);
            }
        }

        @Test
        void driftIsLocalWithinHalfAmplitude() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 0.5);   // amp = 0.5
            double amp = 0.5;
            for (int i = 0; i < 100_000; i++) {
                double v = DoubleValueMutator.drift(0.5, p);                    // interior
                assertTrue(Math.abs(v - 0.5) <= amp / 2 + 1e-12,
                        "drift exceeded amp/2: " + Math.abs(v - 0.5));
            }
        }

        /**
         * CLAMP drift at the lower bound: the result clips back onto {@code 0.0} whenever the delta is
         * non-positive, i.e. with probability <b>0.5</b> (analytic: {@code P(delta <= 0)}).
         *
         * <p><b>Correction (found by running the suite against a reference implementation):</b> this
         * test originally asserted {@code 0.2505}, which is the <i>full-operator</i> figure from
         * {@code ORACLE.md} §1 ("continuous x=0.0 (at min)"). The drift-only rate is {@code 0.5},
         * because the full-op row halves it by the replacement branch (which is never unchanged):
         * {@code 0.5 x 0.5 = 0.25}. Applying a full-op number to a {@code drift()} call would have
         * failed against a <i>correct</i> implementation.</p>
         */
        @Test
        void driftSticksAtTheLowerBound() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 0.5);
            int stuck = 0;
            for (int i = 0; i < N; i++) {
                if (DoubleValueMutator.drift(0.0, p) == 0.0) stuck++;
            }
            assertProportion("CLAMP drift-only stick rate at x = 0 (mf = 0.5)",
                    0.5, stuck / (double) N);
        }

        /**
         * The full operator's unchanged rate at the bound, which is the {@code ORACLE.md} §1 figure.
         * Kept alongside the drift-only test above so the 0.5 vs 0.25 distinction is pinned by code.
         */
        @Test
        void mutateUnchangedRateAtTheLowerBoundIsHalfTheDriftRate() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 0.5);
            int unchanged = 0;
            for (int i = 0; i < N; i++) {
                if (DoubleValueMutator.mutate(0.0, p) == 0.0) unchanged++;
            }
            assertProportion("CLAMP full-op unchanged rate at x = 0 (mf = 0.5)",
                    0.2505, unchanged / (double) N);
        }

        @Test
        void replacementIsUniformOverTheDomain() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 0.5);
            int below = 0;
            for (int i = 0; i < N; i++) {
                double v = DoubleValueMutator.replace(0.9, p);
                assertTrue(v >= 0.0 && v <= 1.0, "replacement out of domain: " + v);
                if (v < 0.5) below++;
            }
            assertProportion("replacement halves the [0,1] domain evenly", 0.5, below / (double) N);
        }

        /** Interior continuous drift essentially never returns the identical value (OPEN-1). */
        @Test
        void interiorDriftRarelyReturnsIdenticalValue() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 0.5);
            int same = 0;
            for (int i = 0; i < N; i++) {
                if (DoubleValueMutator.drift(0.25, p) == 0.25) same++;
            }
            assertProportion("interior CLAMP drift unchanged rate (mf = 0.5)", 0.0, same / (double) N);
        }

        @Test
        void replacementOutputIsInDomainAcrossExtremeFactors() {
            for (double mf : new double[]{0.0, 0.25, 0.5, 1.0}) {
                DoubleParams p = new DoubleParams(-7.5, 12.25, LoopMode.CLAMP, mf);
                for (int i = 0; i < 20_000; i++) {
                    double v = DoubleValueMutator.replace(0.0, p);
                    assertTrue(v >= -7.5 && v <= 12.25, "mf=" + mf + " produced " + v);
                }
            }
        }
    }

    // ---------------------------------------------------------------- 3. LOOP

    @Nested
    @DisplayName("LOOP: cyclic quantity")
    class Loop {

        @Test
        void driftStaysInsideTheCircle() {
            DoubleParams p = new DoubleParams(0.0, 360.0, LoopMode.LOOP, 0.5);
            for (int i = 0; i < 100_000; i++) {
                double v = DoubleValueMutator.drift(359.0, p);
                assertTrue(v >= 0.0 && v < 360.0, "loop drift left [0,360): " + v);
            }
        }

        /**
         * The CRITICAL rule (SPEC §2): under LOOP, locality is a <b>circular</b> distance.
         * Linear {@code |x' - x| <= amp/2} is false — 359 -> 5 is +6 circular but -354 linear.
         */
        @Test
        void driftLocalityIsCircularNotLinear() {
            DoubleParams p = new DoubleParams(0.0, 360.0, LoopMode.LOOP, 0.5);   // amp = 180
            double amp = 180.0;
            boolean sawSeamCrossing = false;
            for (int i = 0; i < N; i++) {
                double v = DoubleValueMutator.drift(350.0, p);
                double cd = circularDistance(350.0, v, 360.0);
                assertTrue(cd <= amp / 2 + 1e-9,
                        "circular drift exceeded amp/2: " + cd + " (v=" + v + ")");
                if (v < 340.0) sawSeamCrossing = true;   // wrapped past 0
            }
            assertTrue(sawSeamCrossing, "never observed a seam crossing — LOOP is behaving like CLAMP");
        }

        /**
         * Seam crossing is a genuine wrap, not a proximity effect. For {@code x = 359}, {@code mf = 0.5}
         * ({@code amp = 180}): the unwrapped value spans {@code [269, 449]}; it wraps exactly when
         * {@code delta >= 1}, i.e. with probability {@code 89/180 = 0.4944}, landing in {@code [0, 89]}.
         *
         * <p><b>Correction:</b> an earlier number of mine ("17.2%") was a bad proxy — it counted
         * {@code 100 < v < 300}, which includes non-wrapped values in {@code [269, 300)} and excludes
         * wrapped ones. Likewise {@code v < x} is NOT a wrap indicator (non-wrapped {@code [269,359)}
         * also satisfies it and fires 99.4% of the time). The discriminator must be the landing
         * region, not a comparison against {@code x}.</p>
         */
        @Test
        void seamCrossingHappensAtTheAnalyticRate() {
            DoubleParams p = new DoubleParams(0.0, 360.0, LoopMode.LOOP, 0.5);
            int wrapped = 0;
            for (int i = 0; i < N; i++) {
                double v = DoubleValueMutator.drift(359.0, p);
                if (v <= 89.0) wrapped++;      // the wrapped branch lands in [0, 89]
            }
            assertProportion("LOOP wrap rate at x = 359 (mf = 0.5)", 89.0 / 180.0, wrapped / (double) N);
        }

        /** At {@code mf = 1.0} ({@code amp = 360}) drift from {@code x = 5} must reach both sides. */
        @Test
        void loopWrapsBothDirections() {
            DoubleParams p = new DoubleParams(0.0, 360.0, LoopMode.LOOP, 1.0);   // amp = 360
            boolean wrappedBack = false;    // delta < -5  -> v near 360
            boolean stayedForward = false;  // delta > -5  -> v >= 5
            for (int i = 0; i < N; i++) {
                double v = DoubleValueMutator.drift(5.0, p);
                if (v > 180.0) wrappedBack = true;      // came round the far side
                if (v >= 5.0 && v <= 180.0) stayedForward = true;
            }
            assertTrue(wrappedBack, "never wrapped backwards past 0");
            assertTrue(stayedForward, "never stayed on the forward side");
        }

        @Test
        void replacementIsUniformOverTheCircle() {
            DoubleParams p = new DoubleParams(0.0, 360.0, LoopMode.LOOP, 0.5);
            int below = 0;
            for (int i = 0; i < N; i++) {
                double v = DoubleValueMutator.replace(0.0, p);
                assertTrue(v >= 0.0 && v < 360.0, "loop replacement out of domain: " + v);
                if (v < 180.0) below++;
            }
            assertProportion("loop replacement halves the circle evenly", 0.5, below / (double) N);
        }
    }

    // ---------------------------------------------------------------- 4. full operator / branches

    @Nested
    @DisplayName("full operator and edge cases")
    class OperatorAndEdges {

        /**
         * mf = 1.0 is deterministic replacement (F3): {@code nextDouble() < 1.0} is always true.
         * The discriminator is the MEAN — replacement from x = 0.99 averages ~0.5, whereas a pure
         * drift would average ~0.87 (uniform ±0.5 clipped at the upper bound).
         */
        @Test
        void mutateAtMaximumFactorAlwaysTakesReplacement() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 1.0);
            double sum = 0;
            for (int i = 0; i < N; i++) sum += DoubleValueMutator.mutate(0.99, p);
            assertProportion("mf=1.0 mutate mean equals the replacement mean (0.5), not the drift mean",
                    0.5, sum / N, 0.01, N);
        }

        /** {@code drift(mf = 1.0)} remains a legal, well-defined direct call (max-amplitude window). */
        @Test
        void driftAtMaximumFactorIsStillCallableAndBounded() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 1.0);
            for (int i = 0; i < 100_000; i++) {
                double v = DoubleValueMutator.drift(0.5, p);
                assertTrue(v >= 0.0 && v <= 1.0, "mf=1.0 drift out of domain: " + v);
            }
        }

        @Test
        void degenerateDomainReturnsTheOnlyValue() {
            DoubleParams p = new DoubleParams(0.5, 0.5, LoopMode.CLAMP, 0.75);
            for (int i = 0; i < 1_000; i++) {
                assertEquals(0.5, DoubleValueMutator.mutate(0.5, p), 0.0);
            }
        }

        /** E4: mf outside [0,1] is a coding error — fail fast, do not silently clamp. */
        @Test
        void mutationFactorAboveOneIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> DoubleValueMutator.mutate(0.5, new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 1.5)));
        }

        @Test
        void mutationFactorBelowZeroIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> DoubleValueMutator.mutate(0.5, new DoubleParams(0.0, 1.0, LoopMode.CLAMP, -0.1)));
        }

        /** E5: NaN mf would silently produce NaN output — must be rejected explicitly. */
        @Test
        void nanMutationFactorIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> DoubleValueMutator.mutate(0.5, new DoubleParams(0.0, 1.0, LoopMode.CLAMP, Double.NaN)));
        }

        @Test
        void nonFiniteInputIsRejected() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 0.5);
            assertThrows(IllegalArgumentException.class, () -> DoubleValueMutator.mutate(Double.NaN, p));
            assertThrows(IllegalArgumentException.class,
                    () -> DoubleValueMutator.mutate(Double.POSITIVE_INFINITY, p));
        }

        @Test
        void outputIsAlwaysFinite() {
            DoubleParams p = new DoubleParams(0.0, 1.0, LoopMode.CLAMP, 1.0);
            for (int i = 0; i < N; i++) {
                double v = DoubleValueMutator.mutate(0.5, p);
                assertFalse(Double.isNaN(v) || Double.isInfinite(v), "non-finite output: " + v);
            }
        }
    }
}
