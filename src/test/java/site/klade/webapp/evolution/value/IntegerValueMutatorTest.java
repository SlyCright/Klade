package site.klade.webapp.evolution.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static site.klade.webapp.evolution.value.ValueMutationTestSupport.N;
import static site.klade.webapp.evolution.value.ValueMutationTestSupport.assertProportion;

/**
 * M2 red tests for {@link IntegerValueMutator} — rank-linear v2.2 §4.2
 * ({@code SPEC-mutation.md} §3, §7; fixture {@code ORACLE.md} §2).
 *
 * <p>The lock is the FORMULA: {@code k' = clip(floor(x' + 0.5), min, max)} (round half up, then clip).
 * The vault's Example 6 table differs at two rows; these tests assert the formula's outcome.</p>
 */
class IntegerValueMutatorTest {

    private static final IntParams ONE_TO_FIVE_HALF = new IntParams(1, 5, 0.5);

    // ---------------------------------------------------------------- freeze

    @Nested
    @DisplayName("emergent freeze at mf = 0")
    class Freeze {

        @Test
        void driftAtZeroFactorIsIdentity() {
            IntParams p = new IntParams(1, 5, 0.0);
            for (int i = 0; i < 10_000; i++) {
                assertEquals(3, IntegerValueMutator.drift(3, p));
            }
        }

        @Test
        void mutateAtZeroFactorIsIdentity() {
            IntParams p = new IntParams(1, 5, 0.0);
            for (int i = 0; i < 10_000; i++) {
                assertEquals(3, IntegerValueMutator.mutate(3, p));
            }
        }
    }

    // ---------------------------------------------------------------- domain

    @Nested
    @DisplayName("domain and locality")
    class DomainAndLocality {

        @Test
        void outputStaysInsideDomain() {
            for (double mf : new double[]{0.0, 0.1, 0.5, 0.8, 1.0}) {
                IntParams p = new IntParams(1, 5, mf);
                for (int i = 0; i < 50_000; i++) {
                    int v = IntegerValueMutator.mutate(3, p);
                    assertTrue(v >= 1 && v <= 5, "mf=" + mf + " produced " + v);
                }
            }
        }

        /** At small mf the drift window is narrow: ORACLE.md §2 gives mf=0.1 -> {2,3,4}. */
        @Test
        void smallFactorDriftReachesOnlyTheNeighbourhood() {
            IntParams p = new IntParams(1, 5, 0.1);
            Set<Integer> seen = new HashSet<>();
            for (int i = 0; i < N; i++) {
                seen.add(IntegerValueMutator.drift(3, p));
            }
            assertEquals(Set.of(2, 3, 4), seen,
                    "mf=0.1 drift reachable set (ORACLE.md §2); the vault's Example 6 says {3}, which is wrong");
        }

        /**
         * ORACLE.md §2: at mf = 0.5 the extremes are NOT reached — the set is exactly {2,3,4}.
         * Proof: {@code x ∈ [2.5, 3.5)} and {@code delta ∈ [-1, +1)} give {@code x' ∈ [1.5, 4.5)},
         * so {@code floor(x' + 0.5) ∈ [2, 4]}. Values 1 and 5 need {@code x'} at the open ends,
         * which is a measure-zero coincidence — not merely "rare".
         */
        @Test
        void midFactorDriftReachesExactlyTheInterior() {
            IntParams p = new IntParams(1, 5, 0.5);
            Set<Integer> seen = new HashSet<>();
            for (int i = 0; i < N; i++) {
                seen.add(IntegerValueMutator.drift(3, p));
            }
            assertEquals(Set.of(2, 3, 4), seen,
                    "mf=0.5 drift reachable set is exactly {2,3,4} (ORACLE.md §2); "
                            + "the vault's Example 6 claims all of {1..5}, which is wrong");
        }

        /** ORACLE.md §2: at mf >= 0.8 the drift window covers the whole domain. */
        @Test
        void highFactorDriftCoversTheWholeDomain() {
            IntParams p = new IntParams(1, 5, 0.8);
            Set<Integer> seen = new HashSet<>();
            for (int i = 0; i < N; i++) {
                seen.add(IntegerValueMutator.drift(3, p));
            }
            assertEquals(Set.of(1, 2, 3, 4, 5), seen, "mf=0.8 drift should reach all of {1..5}");
        }

        /** Round-half-up on the boundary: drift from k=3 with a tiny window must not overshoot. */
        @Test
        void roundingIsHalfUpAndClamped() {
            IntParams p = new IntParams(1, 5, 0.0);   // amp = 0 -> x' = x
            for (int k = 1; k <= 5; k++) {
                assertEquals(k, IntegerValueMutator.drift(k, p));
            }
        }
    }

    // ---------------------------------------------------------------- replacement / operator

    @Nested
    @DisplayName("replacement and full operator")
    class Replacement {

        @Test
        void replacementIsUniformOverTheDomain() {
            int[] counts = new int[6];    // 1..5
            for (int i = 0; i < N; i++) {
                counts[IntegerValueMutator.replace(3, ONE_TO_FIVE_HALF)]++;
            }
            for (int k = 1; k <= 5; k++) {
                assertProportion("replacement P(" + k + ") over {1..5}", 0.2, counts[k] / (double) N);
            }
        }

        /**
         * At mf = 1.0 an integer replacement CAN redraw the current value (1/5 of the time). This is
         * the corrected reading of INV-6 — "only mf == 0 guarantees identity", not "mutation always
         * changes something".
         */
        @Test
        void replacementAtMaximumFactorCanRedrawTheCurrentValue() {
            IntParams p = new IntParams(1, 5, 1.0);
            int same = 0;
            for (int i = 0; i < N; i++) {
                if (IntegerValueMutator.mutate(3, p) == 3) same++;
            }
            assertProportion("mf=1.0 integer self-redraw rate over {1..5}", 0.2, same / (double) N);
        }
    }

    // ---------------------------------------------------------------- edges

    @Nested
    @DisplayName("edge cases (E2, E4, E5)")
    class Edges {

        /** E2: min == max is degenerate — the only value is returned. */
        @Test
        void degenerateDomainReturnsTheOnlyValue() {
            IntParams p = new IntParams(7, 7, 0.75);
            for (int i = 0; i < 10_000; i++) {
                assertEquals(7, IntegerValueMutator.mutate(7, p));
            }
        }

        /** E4: out-of-range mutationFactor is a coding error. */
        @Test
        void outOfRangeMutationFactorIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> IntegerValueMutator.mutate(3, new IntParams(1, 5, 1.5)));
            assertThrows(IllegalArgumentException.class,
                    () -> IntegerValueMutator.mutate(3, new IntParams(1, 5, -0.1)));
        }

        /** E5: NaN mf must be rejected, not silently propagated. */
        @Test
        void nanMutationFactorIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> IntegerValueMutator.mutate(3, new IntParams(1, 5, Double.NaN)));
        }
    }
}
