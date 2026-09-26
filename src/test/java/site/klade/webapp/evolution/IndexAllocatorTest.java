package site.klade.webapp.evolution;

import site.klade.simulation.Index;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class IndexAllocatorTest {

    private static Index idx(String s) {
        return Index.parse(s);
    }

    private static String allocate(String left, String right) {
        return IndexAllocator.allocateBetween(
                left == null ? null : idx(left),
                right == null ? null : idx(right)).toString();
    }

    // ---- user's step1 -> step2 ----
    @Test
    void step1_insertBetweenGivesZeroChild() {
        assertEquals("0.0.", allocate("0.", "1."));   // item C
    }

    @Test
    void step1_appendAtEnd() {
        assertEquals("2.", allocate("1.", null));      // item D
    }

    // ---- user's step2 -> step3 ----
    @Test
    void step3_gapAfterPrefix() {
        assertEquals("0.-1.", allocate("0.", "0.0.")); // item E
    }

    @Test
    void step3_gapBeforeLevelJump() {
        assertEquals("0.1.", allocate("0.0.", "1.")); // item F
    }

    @Test
    void step3_gapConsecutive() {
        assertEquals("1.0.", allocate("1.", "2."));   // item G
    }

    @Test
    void step3_appendAtEnd() {
        assertEquals("3.", allocate("2.", null));      // item H
    }

    // ---- nullable endpoints / root preference ----
    @Test
    void prepend_beforeFirst() {
        assertEquals("-1.", allocate(null, "0."));
    }

    @Test
    void prepend_beforeNegativeFirst() {
        assertEquals("0.", allocate(null, "0.-1."));
    }

    @Test
    void prepend_beforeDeepZero() {
        assertEquals("0.", allocate(null, "0.0.0."));
    }

    @Test
    void allocate_rootPreference_zeroInMidGap() {
        assertEquals("0.", allocate("-3.", "3."));
    }

    @Test
    void allocate_rootPreference_positive() {
        assertEquals("1.", allocate("0.", "3."));
    }

    @Test
    void allocate_rootPreference_negative() {
        assertEquals("-1.", allocate("-3.", "0."));
    }

    @Test
    void allocate_rootPreference_zeroBeforeDeep() {
        assertEquals("0.", allocate("-3.", "0.0."));
    }

    @Test
    void allocate_rootPreference_mixedDeep() {
        assertEquals("0.", allocate("-3.8.", "3.-5."));
    }

    @Test
    void emptyGenome_givesRoot() {
        assertEquals("0.", allocate(null, null));
    }

    @Test
    void append_usesNextOuterLevel() {
        assertEquals("1.", allocate("0.1.", null));
    }

    // ---- spec 3.1 ----
    @Test
    void numericHole_isFilled() {
        assertEquals("1.3.", allocate("1.2.", "1.4."));
    }

    @Test
    void differentLevels_growLeft() {
        assertEquals("2.2.", allocate("2.1.", "3."));
    }

    @Test
    void append_afterTwoThree() {
        assertEquals("3.", allocate("2.3.", null));
    }

    @Test
    void kDefaultsToZero_betweenConsecutive() {
        assertEquals("2.2.0.", allocate("2.2.", "2.3."));
    }

    // ---- rule-chain mechanics ----
    @Test
    void multiLevelRight_deepensWithZero() {
        assertEquals("0.0.", allocate("0.", "0.5.7."));
    }

    @Test
    void multiLevelRight_nextSiblingWhenIncrements() {
        assertEquals("0.1.", allocate("0.0.", "0.5.7."));
    }

    @Test
    void currentLevel_isPreferred() {
        assertEquals("1.4.", allocate("1.3.", "1.15."));
    }

    @Test
    void insertBetweenIndexAndItsExtension() {
        assertEquals("0.-1.-1.", allocate("0.-1.", "0.-1.0."));
    }

    @Test
    void negativeMirrorOfG_case() {
        assertEquals("-1.0.", allocate("-1.", "0."));
    }

    @Test
    void sameLevelNegativeFallback() {
        assertEquals("0.-6.", allocate("0.-15.", "0.-5."));
    }

    @Test
    void deepConsecutive_addsLevel() {
        assertEquals("0.0.0.0.", allocate("0.0.0.", "0.0.1."));
    }

    @Test
    void deepConsecutive_negativeElbow() {
        assertEquals("0.0.0.-1.", allocate("0.0.0.", "0.0.0.0."));
        assertEquals("0.0.0.-2.", allocate("0.0.0.", "0.0.0.-1."));
    }

    // ---- contract / validation ----
    @Test
    void leftMustPrecedeRight() {
        assertThrows(IllegalArgumentException.class, () -> allocate("2.", "1."));
        assertThrows(IllegalArgumentException.class, () -> allocate("2.1.", "2.1."));
        assertThrows(IllegalArgumentException.class, () -> allocate("0.-1.", "0."));
    }

    @Test
    void allocation_isPure() {
        assertEquals("1.0.", allocate("1.", "2."));
        assertEquals("1.0.", allocate("1.", "2."));
    }

    @ParameterizedTest(name = "[{index}] {0}|{1} -> {2}")
    @MethodSource("distinctGapsProduceDistinctIndices")
    void distinctGaps_produceDistinctIndices(String left, String right, String expected) {
        assertEquals(expected, allocate(left, right));
    }

    static Stream<Arguments> distinctGapsProduceDistinctIndices() {
        return Stream.of(
                Arguments.of("0.", "0.0.", "0.-1."),
                Arguments.of("0.0.", "1.", "0.1."),
                Arguments.of("1.", "2.", "1.0."),
                Arguments.of("2.", null, "3."),
                Arguments.of(null, "0.", "-1.")
        );
    }
}