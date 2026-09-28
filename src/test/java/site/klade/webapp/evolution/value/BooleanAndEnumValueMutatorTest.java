package site.klade.webapp.evolution.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static site.klade.webapp.evolution.value.ValueMutationTestSupport.N;
import static site.klade.webapp.evolution.value.ValueMutationTestSupport.assertProportion;

/**
 * M2 red tests for {@link BooleanValueMutator} and {@link EnumValueMutator} — rank-linear v2.2 §4.3
 * ({@code SPEC-mutation.md} §4, §7; fixture {@code ORACLE.md} §1).
 *
 * <p>These carry the F1 correction: the vault's claim that "for boolean, ordered and unordered
 * coincide" is FALSE at a boundary, for every {@code n}. The ordered path clamps an exit to a
 * neighbour, so at a domain edge one exit maps back to the value itself and does not flip.</p>
 */
class BooleanAndEnumValueMutatorTest {

    // ================================================================ BOOLEAN (unordered default)

    @Nested
    @DisplayName("BooleanValueMutator (unordered default, n = 2)")
    class BooleanTests {

        @Test
        void driftAtZeroFactorIsIdentity() {
            BooleanParams p = new BooleanParams(0.0);
            for (int i = 0; i < 10_000; i++) {
                assertFalse(BooleanValueMutator.drift(false, p));
                assertTrue(BooleanValueMutator.drift(true, p));
            }
        }

        /**
         * The "famous number", recomputed for the new §4.3: at `mf = 0.5` (amp = 0.5, delta in
         * [-0.25, +0.25]) the boolean DRIFT changes with probability <b>0.125</b>, not 0.25 — an
         * unordered exit now draws `uniform{0,1}`, which returns the current value half the time, so
         * `P(change) = 0.25 exit × 0.5`. Drift-only. (ORACLE.md §1)
         */
        @Test
        void driftOnlyFlipsAtEighthRateAtHalfFactor() {
            BooleanParams p = new BooleanParams(0.5);
            int flips = 0;
            for (int i = 0; i < N; i++) {
                if (BooleanValueMutator.drift(false, p)) flips++;
            }
            assertProportion("boolean drift-only change rate at mf = 0.5 (unordered)",
                    0.125, flips / (double) N);
        }

        /**
         * The FULL operator changes with probability <b>0.3125</b> at `mf = 0.5`: half the calls take
         * the replacement branch, which now returns the current value half the time too. Conflating this
         * with the drift-only 0.125 is the exact error this test pair guards against.
         */
        @Test
        void fullOperatorChangesAtTheCombinedRate() {
            BooleanParams p = new BooleanParams(0.5);
            int flips = 0;
            for (int i = 0; i < N; i++) {
                if (BooleanValueMutator.mutate(false, p)) flips++;
            }
            assertProportion("boolean FULL-op change rate at mf = 0.5", 0.3125, flips / (double) N);
        }

        /**
         * Replacement is `uniform{0,1}` — it may return the current value (`P = 1/2`). The retired rule
         * excluded `k`, so this must NOT be asserted as a guaranteed flip.
         */
        @Test
        void replacementIsUniformAndMayReturnTheCurrentValue() {
            BooleanParams p = new BooleanParams(0.5);
            int flippedFromFalse = 0;
            for (int i = 0; i < N; i++) {
                if (BooleanValueMutator.replace(false, p)) flippedFromFalse++;
            }
            assertProportion("boolean replacement returns true from false", 0.5, flippedFromFalse / (double) N);
        }

        /**
         * At `mf = 1.0` the replacement branch always runs, and replacement is `uniform{0,1}` — so the
         * operator flips only half the time, NOT always.
         */
        @Test
        void mutateAtMaximumFactorRedrawsUniformly() {
            BooleanParams p = new BooleanParams(1.0);
            int flipped = 0;
            for (int i = 0; i < N; i++) {
                if (BooleanValueMutator.mutate(false, p)) flipped++;
            }
            assertProportion("boolean mutate at mf=1.0 flips at the replacement rate", 0.5, flipped / (double) N);
        }

        /**
         * {@code drift(mf = 1.0)} changes the value with probability <b>0.25</b> — it is not a
         * guaranteed flip.
         *
         * <p>Two independent halvings: the draw leaves the slice with probability {@code 0.5}, and an
         * unordered exit then draws {@code uniform{0,1}}, which returns the current value half the time.
         * {@code 0.5 × 0.5 = 0.25} (ORACLE.md §B, `mf=1.0` row: 0.2496).</p>
         *
         * <p><b>Correction history:</b> this test first asserted "always flips" (wrong), then "flips
         * 0.5" under the retired rule where an exit always changed the value. Both were wrong for the
         * rule in force at the time it mattered — which is why the suite is run rather than reasoned
         * about.</p>
         */
        @Test
        void driftAtMaximumFactorChangesAtQuarterRate() {
            BooleanParams p = new BooleanParams(1.0);
            int flips = 0;
            for (int i = 0; i < N; i++) {
                if (BooleanValueMutator.drift(false, p)) flips++;
            }
            assertProportion("boolean drift-only change rate at mf = 1.0 (unordered)",
                    0.25, flips / (double) N);
        }

        @Test
        void outOfRangeAndNanFactorsAreRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> BooleanValueMutator.mutate(false, new BooleanParams(1.5)));
            assertThrows(IllegalArgumentException.class,
                    () -> BooleanValueMutator.mutate(false, new BooleanParams(-0.1)));
            assertThrows(IllegalArgumentException.class,
                    () -> BooleanValueMutator.mutate(false, new BooleanParams(Double.NaN)));
        }
    }

    // ================================================================ ENUM

    @Nested
    @DisplayName("EnumValueMutator")
    class EnumTests {

        /**
         * Replacement is `uniform{0..n-1}` — the whole domain, `k` INCLUDED, and identical for ordered
         * and unordered. Each of the `n` values has probability `1/n`, so a self-redraw happens with
         * probability `1/n`.
         */
        @Test
        void replacementIsUniformOverTheWholeDomainForBothOrderings() {
            for (boolean ordered : new boolean[]{true, false}) {
                EnumParams p = new EnumParams(6, ordered, 0.5);
                Map<Integer, Integer> counts = new HashMap<>();
                for (int i = 0; i < N; i++) {
                    int v = EnumValueMutator.replace(2, p);
                    assertTrue(v >= 0 && v < 6, "replacement out of range: " + v);
                    counts.merge(v, 1, Integer::sum);
                }
                for (int k = 0; k < 6; k++) {
                    assertProportion("enum replace P(" + k + ") ordered=" + ordered,
                            1.0 / 6.0, counts.getOrDefault(k, 0) / (double) N);
                }
            }
        }

        /**
         * F1 is WITHDRAWN under the new §4.3. Under the retired `k±1` rule the ordered path clamped a
         * boundary exit back onto the value itself, so ordered (0.1249) and unordered (0.2503) differed
         * at `mf=0.5`. The continuous embedding removes that artefact: for `n = 2` the two paths
         * coincide (ORACLE.md: 0.1251 vs 0.1248).
         */
        @Test
        void orderedAndUnorderedCoincideForBoolean() {
            EnumParams ordered = new EnumParams(2, true, 0.5);
            EnumParams unordered = new EnumParams(2, false, 0.5);
            int orderedFlips = 0;
            int unorderedFlips = 0;
            for (int i = 0; i < N; i++) {
                if (EnumValueMutator.drift(0, ordered) != 0) orderedFlips++;
                if (EnumValueMutator.drift(0, unordered) != 0) unorderedFlips++;
            }
            assertProportion("ordered n=2 drift flip rate at mf=0.5 (F1 withdrawn)",
                    0.1251, orderedFlips / (double) N);
            assertProportion("unordered n=2 drift flip rate at mf=0.5",
                    0.1248, unorderedFlips / (double) N);
        }

        /**
         * For `n >= 3` ordered and unordered still differ, but for a principled reason: ordered CLAMPS
         * the displaced index, unordered REDRAWS over all `n`. At the boundary `k=0`, `n=6`, `mf=0.5`.
         */
        @Test
        void orderedAndUnorderedDifferAtEnumBoundary() {
            EnumParams ordered = new EnumParams(6, true, 0.5);
            EnumParams unordered = new EnumParams(6, false, 0.5);
            int orderedChanges = 0;
            int unorderedChanges = 0;
            for (int i = 0; i < N; i++) {
                if (EnumValueMutator.drift(0, ordered) != 0) orderedChanges++;
                if (EnumValueMutator.drift(0, unordered) != 0) unorderedChanges++;
            }
            assertProportion("ordered n=6 boundary k=0 drift change rate", 0.3328, orderedChanges / (double) N);
            assertProportion("unordered n=6 boundary k=0 drift change rate", 0.5557, unorderedChanges / (double) N);
        }

        /** Ordered interior drift rate at n=6, k=2, mf=0.5. */
        @Test
        void orderedInteriorDriftChangeRate() {
            EnumParams p = new EnumParams(6, true, 0.5);
            int changes = 0;
            for (int i = 0; i < N; i++) {
                if (EnumValueMutator.drift(2, p) != 2) changes++;
            }
            assertProportion("ordered n=6 interior k=2 drift change rate", 0.6664, changes / (double) N);
        }

        /**
         * THE POINT OF THE NEW §4.3: an ordered step may cross MANY slices. The retired `k±1` clamp
         * discarded the displacement — at `n=100, mf=1.0` it reached only `|dk| <= 9`; the embedding now
         * spreads over the full range. Assert that large displacements actually occur, so the old clamp
         * cannot silently reappear.
         */
        @Test
        void orderedDriftCanCrossManySlices() {
            EnumParams p = new EnumParams(100, true, 1.0);
            int far = 0;
            for (int i = 0; i < N; i++) {
                int v = EnumValueMutator.drift(50, p);
                assertTrue(v >= 0 && v < 100, "ordered drift out of range: " + v);
                if (Math.abs(v - 50) > 9) far++;
            }
            // Under the retired k±1 clamp this fraction was exactly 0.
            assertTrue(far / (double) N > 0.5,
                    "ordered drift should routinely cross >9 slices at mf=1.0, n=100; got " + far / (double) N);
        }

        /**
         * Unordered drift draws uniformly over the WHOLE domain, `k` included. A draw that stays in-slice
         * also returns `k` legitimately (INV-6), so the assertion is: every value is reachable, and an
         * EXIT is spread over all `n`.
         */
        @Test
        void unorderedDriftCoversTheWholeDomain() {
            EnumParams p = new EnumParams(6, false, 0.5);
            boolean[] seen = new boolean[6];
            for (int i = 0; i < N; i++) {
                seen[EnumValueMutator.drift(2, p)] = true;
            }
            for (int k = 0; k < 6; k++) {
                assertTrue(seen[k], "unordered drift never reached index " + k);
            }
        }

        /** E1: n = 1 is degenerate — a single-value domain has nothing to mutate to. */
        @Test
        void singleValueEnumIsANoOp() {
            EnumParams p = new EnumParams(1, false, 0.75);
            for (int i = 0; i < 10_000; i++) {
                assertEquals(0, EnumValueMutator.mutate(0, p));
                assertEquals(0, EnumValueMutator.replace(0, p));
            }
        }

        @Test
        void driftAtZeroFactorIsIdentityForBothOrderings() {
            for (boolean ordered : new boolean[]{true, false}) {
                EnumParams p = new EnumParams(5, ordered, 0.0);
                for (int i = 0; i < 10_000; i++) {
                    assertEquals(3, EnumValueMutator.drift(3, p));
                }
            }
        }

        @Test
        void outputStaysInRangeForBothOrderings() {
            for (boolean ordered : new boolean[]{true, false}) {
                for (double mf : new double[]{0.1, 0.5, 1.0}) {
                    EnumParams p = new EnumParams(5, ordered, mf);
                    for (int i = 0; i < 50_000; i++) {
                        int v = EnumValueMutator.mutate(2, p);
                        assertTrue(v >= 0 && v < 5,
                                "out of range: " + v + " (ordered=" + ordered + ", mf=" + mf + ")");
                    }
                }
            }
        }

        @Test
        void outOfRangeAndNanFactorsAreRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> EnumValueMutator.mutate(0, new EnumParams(5, false, 1.5)));
            assertThrows(IllegalArgumentException.class,
                    () -> EnumValueMutator.mutate(0, new EnumParams(5, false, Double.NaN)));
        }
    }
}
