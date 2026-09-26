package site.klade.webapp.evolution;

import site.klade.simulation.Index;

/**
 * Allocation policy of the index-independent genetic operator: picks a fresh index strictly
 * inside a chosen gap of a sorted genome.
 *
 * <p>Preference order (most canonical / shallowest first):</p>
 * <ol>
 *   <li>A single-segment (root) index that fits the gap, choosing the one closest to zero
 *       (0, then 1/-1, 2/-2, ...).</li>
 *   <li>Otherwise a deeper index (same-level increment, then negative fallback, then extend
 *       with 0).</li>
 * </ol>
 *
 * <p>Design note (spec): indices represent an <b>order</b>, not a tree. The operator tries to
 * keep new genes at the most canonical position so that independently arising mutations land on
 * the same index and meet in crossover.</p>
 *
 * <p>Precondition: {@code left} and {@code right} must be the target genome's current
 * consecutive neighbours (the gap the caller picked), or a null endpoint:</p>
 * <ul>
 *   <li>{@code left == null} - insert before the first index</li>
 *   <li>{@code right == null} - append after the last index</li>
 *   <li>both {@code null} - empty genome, root {@code 0.}</li>
 * </ul>
 *
 * @throws IllegalArgumentException if both endpoints are non-null and left &gt;= right
 */
public final class IndexAllocator {

    private IndexAllocator() {
    }

    /**
     * Allocates a fresh index strictly between {@code left} and {@code right}.
     */
    public static Index allocateBetween(Index left, Index right) {
        if (left == null && right == null) {
            return new Index(0);
        }
        if (left != null && right != null && left.compareTo(right) >= 0) {
            throw new IllegalArgumentException("left must precede right");
        }

        Index root = IndexOps.closestRootWithin(left, right);
        if (root != null) {
            return root;
        }

        if (left == null) {
            return IndexOps.decrementFirst(right);
        }
        if (right == null) {
            return IndexOps.incrementFirst(left);
        }

        if (isNegativeDirection(left, right)) {
            Index previousRight = IndexOps.decrementLast(right);
            if (isStrictlyBetween(left, right, previousRight)) {
                return previousRight;
            }
        } else {
            Index next = IndexOps.incrementLast(left);
            if (isStrictlyBetween(left, right, next)) {
                return next;
            }
        }

        Index previousLeft = IndexOps.decrementLast(left);
        if (isStrictlyBetween(left, right, previousLeft)) {
            return previousLeft;
        }

        return IndexOps.extend(left, 0);
    }

    private static boolean isNegativeDirection(Index left, Index right) {
        return IndexOps.segmentAt(left, IndexOps.segmentCount(left) - 1) < 0
                || (IndexOps.isPrefixOf(left, right)
                    && IndexOps.segmentAt(right, 1) == 0);
    }

    private static boolean isStrictlyBetween(Index left, Index right, Index candidate) {
        return left.compareTo(candidate) < 0 && candidate.compareTo(right) < 0;
    }
}