package site.klade.webapp.evolution;

import site.klade.simulation.Index;

import java.util.ArrayList;
import java.util.List;

/**
 * Unary structural derivations over {@link Index} values.
 *
 * <p>These operations were moved out of the Simulation value object so that
 * {@code site.klade.simulation.Index} stays a pure immutable POJO (data + comparison +
 * parse/toString). All functions here are pure: they read {@link Index} via its public
 * surface ({@code getValue}/{@code getNested}/{@code isTerminal}) and derive a new index
 * without mutating the input.</p>
 *
 * <p>Indexes encode ORDER ONLY (no tree/ownership semantics).</p>
 */
public final class IndexOps {

    private IndexOps() {
    }

    /** Number of segments (dots + 1). */
    public static int segmentCount(Index index) {
        return segments(index).size();
    }

    /** Value of the segment at {@code position} (0 = outermost). */
    public static int segmentAt(Index index, int position) {
        List<Integer> segs = segments(index);
        if (position < 0 || position >= segs.size()) {
            throw new IndexOutOfBoundsException("segment position: " + position);
        }
        return segs.get(position);
    }

    /** Parent = index without its last segment; null for a terminal index. */
    public static Index parent(Index index) {
        List<Integer> segs = segments(index);
        if (segs.size() <= 1) {
            return null;
        }
        return fromSegments(segs.subList(0, segs.size() - 1));
    }

    /** Appends a new terminal segment. */
    public static Index extend(Index index, int segment) {
        List<Integer> segs = new ArrayList<>(segments(index));
        segs.add(segment);
        return fromSegments(segs);
    }

    /** Bumps the last segment by +1. */
    public static Index incrementLast(Index index) {
        return shiftLast(index, +1, "index segment overflow");
    }

    /** Bumps the last segment by -1. */
    public static Index decrementLast(Index index) {
        return shiftLast(index, -1, "index segment underflow");
    }

    /**
     * Bumps the outermost segment by +1 and produces a single-segment root index (any deeper
     * segments are dropped). This mirrors the original Simulation {@code Index.incrementFirst}.
     */
    public static Index incrementFirst(Index index) {
        int first = index.getValue();
        if (first == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("index segment overflow");
        }
        return new Index(first + 1);
    }

    /**
     * Bumps the outermost segment by -1 and produces a single-segment root index (any deeper
     * segments are dropped). This mirrors the original Simulation {@code Index.decrementFirst}.
     */
    public static Index decrementFirst(Index index) {
        int first = index.getValue();
        if (first == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("index segment underflow");
        }
        return new Index(first - 1);
    }

    /** True iff {@code prefix} is a path-prefix of {@code other} (reflexive). */
    public static boolean isPrefixOf(Index prefix, Index other) {
        if (prefix == null || other == null) {
            return false;
        }
        List<Integer> a = segments(prefix);
        List<Integer> b = segments(other);
        if (a.size() > b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(b.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds a single-segment (root) index strictly inside the open interval {@code (left, right)},
     * choosing the one closest to zero (shallowest, most canonical). Returns {@code null} when no
     * root index fits in the gap; {@code left == null} / {@code right == null} mean unbounded sides.
     */
    public static Index closestRootWithin(Index left, Index right) {
        long lo = left != null ? left.getValue() : Long.MIN_VALUE; // need k > lo
        long hi = right != null
                ? (right.isTerminal() ? (long) right.getValue() - 1 : right.getValue()) // need k <= hi
                : Long.MAX_VALUE;
        Long k = closestIntegerInOpenClosed(lo, hi);
        return k == null ? null : new Index(k.intValue());
    }

    /** Smallest-in-absolute-value integer in the half-open range {@code (loExclusive, hiInclusive]}. */
    private static Long closestIntegerInOpenClosed(long loExclusive, long hiInclusive) {
        if (loExclusive < 0 && 0 <= hiInclusive) {
            return 0L; // zero is the canonical home
        }
        if (hiInclusive < 0) {
            // all candidates are negative; the one closest to zero is the largest
            return hiInclusive > loExclusive ? hiInclusive : null;
        }
        // all candidates are positive (loExclusive >= 0); closest to zero is the smallest
        long candidate = loExclusive + 1;
        return candidate <= hiInclusive ? candidate : null;
    }

    private static Index shiftLast(Index index, int delta, String error) {
        List<Integer> segs = new ArrayList<>(segments(index));
        int last = segs.get(segs.size() - 1);
        if (delta > 0) {
            if (last == Integer.MAX_VALUE) throw new IllegalArgumentException(error);
        } else {
            if (last == Integer.MIN_VALUE) throw new IllegalArgumentException(error);
        }
        segs.set(segs.size() - 1, last + delta);
        return fromSegments(segs);
    }

    private static List<Integer> segments(Index index) {
        List<Integer> segs = new ArrayList<>();
        Index current = index;
        while (current != null) {
            segs.add(current.getValue());
            current = current.getNested();
        }
        return segs;
    }

    private static Index fromSegments(List<Integer> segs) {
        Index result = null;
        for (int i = segs.size() - 1; i >= 0; i--) {
            result = new Index(segs.get(i), result);
        }
        return result;
    }
}