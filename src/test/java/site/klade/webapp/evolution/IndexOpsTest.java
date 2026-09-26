package site.klade.webapp.evolution;

import site.klade.simulation.Index;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class IndexOpsTest {

    private static Index idx(String s) {
        return Index.parse(s);
    }

    @Test
    void segmentCount_terminal() {
        assertEquals(1, IndexOps.segmentCount(new Index(3)));
    }

    @Test
    void segmentCount_nested() {
        assertEquals(2, IndexOps.segmentCount(new Index(2, new Index(1))));
    }

    @Test
    void segmentCountAndAt_deepChain() {
        Index i = new Index(1, new Index(33, new Index(15, new Index(4))));
        assertEquals(4, IndexOps.segmentCount(i));
        assertEquals(1, IndexOps.segmentAt(i, 0));
        assertEquals(33, IndexOps.segmentAt(i, 1));
        assertEquals(15, IndexOps.segmentAt(i, 2));
        assertEquals(4, IndexOps.segmentAt(i, 3));
    }

    @Test
    void segmentAt_outOfBoundsThrows() {
        Index i = idx("1.33.15.4");
        assertThrows(IndexOutOfBoundsException.class, () -> IndexOps.segmentAt(i, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> IndexOps.segmentAt(i, 4));
    }

    @ParameterizedTest(name = "[{index}] incrementLast {0} -> {1}")
    @CsvSource({
            "0.0.,   0.1.",
            "2.,     3.",
            "-1.,    0.",
            "0.-1.,  0.0.",
            "1.2.,   1.3."
    })
    void incrementLast_parameterized(String input, String expected) {
        assertEquals(expected, IndexOps.incrementLast(idx(input)).toString());
    }

    @ParameterizedTest(name = "[{index}] decrementLast {0} -> {1}")
    @CsvSource({
            "0.,     -1.",
            "0.0.,   0.-1.",
            "1.,     0.",
            "0.-2.,  0.-3.",
            "3.,     2."
    })
    void decrementLast_parameterized(String input, String expected) {
        assertEquals(expected, IndexOps.decrementLast(idx(input)).toString());
    }

    @ParameterizedTest(name = "[{index}] extend {0} + {1} -> {2}")
    @CsvSource({
            "2.1.,   4,   2.1.4.",
            "2.1.,   -3,  2.1.-3.",
            "0.,     0,   0.0.",
            "1.0.,   7,   1.0.7."
    })
    void extend_parameterized(String input, int segment, String expected) {
        assertEquals(expected, IndexOps.extend(idx(input), segment).toString());
    }

    @Test
    void derivations_doNotMutateSource() {
        Index source = idx("0.0.");
        IndexOps.incrementLast(source);
        IndexOps.decrementLast(source);
        IndexOps.extend(source, 5);
        assertEquals("0.0.", source.toString());
        assertEquals("0.1.", IndexOps.incrementLast(idx("0.0.")).toString());
        assertEquals(idx("0.0."), idx("0.0."));
    }

    @Test
    void sharedTails_areUnaffectedBySiblingOperations() {
        Index shared = new Index(1);
        Index left = new Index(2, shared);
        Index right = new Index(5, shared);

        IndexOps.incrementLast(left);
        IndexOps.extend(left, 4);
        IndexOps.parent(left);

        assertEquals(1, shared.getValue());
        assertEquals("1.", shared.toString());
        assertEquals("5.1.", right.toString());
        assertEquals("2.1.", left.toString());
    }

    @ParameterizedTest(name = "[{index}] isPrefixOf {0} of {1} -> {2}")
    @CsvSource({
            "2.1.,   2.1.,     true",
            "2.,     2.1.,     true",
            "0.-1.,  0.-1.0.,  true",
            "2.1.3., 2.1.,     false",
            "2.,     3.,       false",
            "0.1.,   0.-1.,    false"
    })
    void isPrefixOf_parameterized(String a, String b, boolean expected) {
        assertEquals(expected, IndexOps.isPrefixOf(idx(a), idx(b)));
    }

    @ParameterizedTest(name = "[{index}] parent of {0} -> {1}")
    @CsvSource({
            "2.1.3.,   2.1.",
            "0.-1.0.,  0.-1.",
            "3.,       ",
            "0.,       "
    })
    void parent_dropsLastSegment(String input, String expected) {
        Index parent = IndexOps.parent(idx(input));
        if (expected == null || expected.isEmpty()) {
            assertNull(parent);
        } else {
            assertEquals(expected, parent.toString());
        }
    }

    @Test
    void parentExtend_roundTripsWithinLevel() {
        assertEquals("2.1.9.", IndexOps.extend(IndexOps.parent(idx("2.1.3.")), 9).toString());
        assertEquals("0.-1.-4.", IndexOps.extend(IndexOps.parent(idx("0.-1.0.")), -4).toString());
    }

    @Test
    void incrementLast_overflowThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> IndexOps.incrementLast(new Index(Integer.MAX_VALUE)));
        assertThrows(IllegalArgumentException.class,
                () -> IndexOps.incrementLast(new Index(0, new Index(Integer.MAX_VALUE))));
    }

    @Test
    void decrementLast_underflowThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> IndexOps.decrementLast(new Index(Integer.MIN_VALUE)));
        assertThrows(IllegalArgumentException.class,
                () -> IndexOps.decrementLast(new Index(0, new Index(Integer.MIN_VALUE))));
    }

    // ---- closestRootWithin ----
    private static String rootOrNull(String left, String right) {
        Index root = IndexOps.closestRootWithin(
                left == null ? null : idx(left),
                right == null ? null : idx(right));
        return root == null ? null : root.toString();
    }

    @ParameterizedTest(name = "[{index}] roots in ({0}, {1}) -> {2}")
    @CsvSource(delimiter = '|', value = {
            "       | 0.-1.  | 0.",
            "       | 0.0.0. | 0.",
            "       | 0.     | -1.",
            " -3.   | 3.     | 0.",
            " 0.    | 3.     | 1.",
            " -3.   | 0.     | -1.",
            " -3.   | 0.0.   | 0.",
            " -3.8. | 3.-5.  | 0.",
            " 1.    | 2.     | ",
            " 0.    | 1.     | "
    })
    void closestRootWithin_parameterized(String left, String right, String expected) {
        String actual = rootOrNull(left, right);
        if (expected == null || expected.isEmpty()) {
            assertNull(actual);
        } else {
            assertEquals(expected, actual);
        }
    }
}