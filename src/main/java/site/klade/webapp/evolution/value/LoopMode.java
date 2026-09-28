package site.klade.webapp.evolution.value;

/**
 * How a continuous range is bounded (SPEC-mutation.md §4.1).
 *
 * <p>{@code CLAMP} — a genuinely bounded quantity (weight, concentration): the drift result is
 * clipped into {@code [min, max]}, so at a bound the drift "sticks".
 * {@code LOOP} — a cyclic quantity (angle in {@code [0, 360)}, phase): the drift result wraps
 * around the {@code min ↔ max} border.
 *
 * <p>The caller picks by the <b>meaning</b> of the quantity, not by convenience.
 */
public enum LoopMode {
    CLAMP,
    LOOP
}