package site.klade.webapp.evolution;

import java.util.Random;

/**
 * Handles weighted random index selection for mutation operations.
 * <p>
 * Provides methods to select indices with different probability distributions
 * (uniform, linear, etc.) for use in evolutionary algorithms.
 */
public class RandomIndexSelector {

    private final Random random;

    /**
     * Creates a selector backed by its own {@link Random} instance.
     */
    public RandomIndexSelector() {
        this(new Random());
    }

    /**
     * Creates a selector backed by the given random source.
     *
     * @param random the source of randomness (inject a seeded {@link Random} for reproducibility)
     */
    public RandomIndexSelector(Random random) {
        this.random = random;
    }

    /**
     * Picks a random index in [0, size-1] with probability proportional to (i + 1).
     * <p>
     * This corresponds to 1-based indexing where:
     * conceptual index 0 -> probability 0
     * conceptual index 1 -> first real element
     * conceptual index size -> last real element, highest probability
     *
     * @param size the size of the list to pick from
     * @return a random index with linear probability distribution
     */
    public int pickLinearWeightedIndex(int size) {
        if (size <= 0) throw new IllegalArgumentException("Cannot pick from empty list");
        if (size == 1) return 0;

        double r = random.nextDouble();

        // P(i) = (i + 1) / S, where S = size * (size + 1) / 2
        // CDF: F(i) = (i + 1)(i + 2) / (size * (size + 1))
        //
        // Example for size=3: S = 3*4/2 = 6
        //   P(0) = 1/6 ≈ 16.7%
        //   P(1) = 2/6 ≈ 33.3%
        //   P(2) = 3/6 = 50.0%
        //
        // We need the smallest i such that F(i) >= r.
        // Solve (i + 1)(i + 2) >= r * size * (size + 1)
        // i >= (sqrt(1 + 4*r*size*(size+1)) - 3) / 2
        double root = Math.sqrt(1.0 + 4.0 * r * size * (size + 1));
        int index = (int) Math.ceil((root - 3.0) / 2.0);

        // Clamp for floating-point edge cases.
        if (index < 0) return 0;
        if (index >= size) return size - 1;
        return index;
    }

    /**
     * Picks a random index in [0, size-1] with uniform probability.
     *
     * @param size the size of the list to pick from
     * @return a random index with uniform probability distribution
     */
    public int pickUniformIndex(int size) {
        if (size <= 0) throw new IllegalArgumentException("Cannot pick from empty list");
        return random.nextInt(size);
    }
}
