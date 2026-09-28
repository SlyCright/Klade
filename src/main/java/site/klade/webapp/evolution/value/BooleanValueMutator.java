package site.klade.webapp.evolution.value;

/**
 * Boolean value mutation, rank-linear v2.2 §4.3 with {@code n = 2} (SPEC-mutation.md §4, §8).
 *
 * <p>A boolean has no intrinsic order, so the unordered path is the default and the only path used
 * here. Under the current §4.3 an unordered exit draws uniformly over {@code {0,1}}, so it lands on the
 * other value exactly half the time: drift flips with probability {@code 0.125} at
 * {@code mutationFactor = 0.5} ({@code 0.25 exit × 0.5}), and the full operator flips with probability
 * {@code 0.3125}.</p>
 *
 * <p>Note the deliberate absence of an ordering caveat: for {@code n = 2} the ordered and unordered
 * paths coincide, so the boolean is not a special case (the earlier "boundary" discrepancy is gone
 * with the {@code k±1} clamp).</p>
 */
public final class BooleanValueMutator {

    private BooleanValueMutator() {
    }

    /** Full operator: replacement with probability {@code p.mutationFactor}, else drift. */
    public static boolean mutate(boolean value, BooleanParams p) {
        final int k = value ? 1 : 0;
        return EnumValueMutator.mutate(k, params(p)) == 1;
    }

    /**
     * Drift branch only (package-private so tests can isolate it).
     *
     * <p>Returns the <b>resulting value</b>, decoded from the enum index. It is not the "did it change"
     * answer — that would be {@code newIndex != k}, which agrees with the new value only when the input
     * was {@code false} and returns the negation for {@code true}.</p>
     */
    static boolean drift(boolean value, BooleanParams p) {
        final int k = value ? 1 : 0;
        return EnumValueMutator.drift(k, params(p)) == 1;
    }

    /**
     * Replacement branch only: uniform over {@code {0,1}}, so it may return the current value
     * ({@code P = 1/2}). It is <b>not</b> a guaranteed flip.
     */
    static boolean replace(boolean value, BooleanParams p) {
        return EnumValueMutator.replace(value ? 1 : 0, params(p)) == 1;
    }

    /** The {@code n = 2}, unordered domain carrying this call's mutation factor. */
    private static EnumParams params(BooleanParams p) {
        return new EnumParams(2, false, p.mutationFactor);
    }
}