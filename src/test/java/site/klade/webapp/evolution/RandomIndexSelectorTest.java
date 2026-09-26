package site.klade.webapp.evolution;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RandomIndexSelectorTest {

    @Test
    void pickLinearWeightedIndex_throwsExceptionForZeroSize() {
        RandomIndexSelector selector = new RandomIndexSelector(new Random());
        assertThrows(IllegalArgumentException.class, () -> selector.pickLinearWeightedIndex(0));
    }

    @Test
    void pickLinearWeightedIndex_throwsExceptionForNegativeSize() {
        RandomIndexSelector selector = new RandomIndexSelector(new Random());
        assertThrows(IllegalArgumentException.class, () -> selector.pickLinearWeightedIndex(-1));
    }

    @Test
    void pickLinearWeightedIndex_returnsZeroForSizeOne() {
        RandomIndexSelector selector = new RandomIndexSelector(new Random());
        assertEquals(0, selector.pickLinearWeightedIndex(1));
    }

    @Test
    void pickLinearWeightedIndex_returnsValidIndexForSizeThree() {
        RandomIndexSelector selector = new RandomIndexSelector(new Random(42)); // Fixed seed for reproducibility
        int index = selector.pickLinearWeightedIndex(3);
        assertTrue(index >= 0 && index < 3, "Index should be in range [0, 2]");
    }

    @Test
    void pickLinearWeightedIndex_distributionApproximatesLinearForSizeThree() {
        RandomIndexSelector selector = new RandomIndexSelector(new Random(123));
        int[] counts = new int[3];
        int iterations = 10000;

        double[] expected = {1.0 / 6.0, 2.0 / 6.0, 3.0 / 6.0};

        // Generate Fibonacci checkpoints
        java.util.Set<Integer> fibonacciCheckpoints = generateFibonacciCheckpoints(iterations);

        for (int i = 1; i <= iterations; i++) {
            int index = selector.pickLinearWeightedIndex(3);
            counts[index]++;

            if (fibonacciCheckpoints.contains(i)) {
                printProgressBars(i, counts, expected);
            }
        }

        // Expected probabilities: P(0)=1/6≈16.7%, P(1)=2/6≈33.3%, P(2)=3/6=50.0%
        // Allow 5% tolerance
        double tolerance = 0.05;
        assertEquals(expected[0], (double) counts[0] / iterations, tolerance);
        assertEquals(expected[1], (double) counts[1] / iterations, tolerance);
        assertEquals(expected[2], (double) counts[2] / iterations, tolerance);
    }

    private java.util.Set<Integer> generateFibonacciCheckpoints(int max) {
        java.util.Set<Integer> checkpoints = new java.util.HashSet<>();
        int a = 1, b = 1;
        checkpoints.add(a);
        while (b <= max) {
            checkpoints.add(b);
            int next = a + b;
            a = b;
            b = next;
        }
        return checkpoints;
    }

    private void printProgressBars(int iteration, int[] counts, double[] expected) {
        System.out.println("\n--- Iteration " + iteration + " ---");
        for (int i = 0; i < counts.length; i++) {
            double actual = (double) counts[i] / iteration;
            double exp = expected[i];
            int barLength = (int) Math.round(actual * 20);
            String bar = "█".repeat(barLength) + "░".repeat(20 - barLength);
            System.out.printf("P(%d): [%s] %.1f%% (exp %.1f%%)%n", i, bar, actual * 100, exp * 100);
        }
    }

    @Test
    void pickLinearWeightedIndex_returnsValidIndexForLargeSize() {
        RandomIndexSelector selector = new RandomIndexSelector(new Random(99));
        int size = 100;
        int index = selector.pickLinearWeightedIndex(size);
        assertTrue(index >= 0 && index < size, "Index should be in range [0, " + (size - 1) + "]");
    }

    @Test
    void pickUniformIndex_throwsExceptionForZeroSize() {
        RandomIndexSelector selector = new RandomIndexSelector(new Random());
        assertThrows(IllegalArgumentException.class, () -> selector.pickUniformIndex(0));
    }

    @Test
    void pickUniformIndex_throwsExceptionForNegativeSize() {
        RandomIndexSelector selector = new RandomIndexSelector(new Random());
        assertThrows(IllegalArgumentException.class, () -> selector.pickUniformIndex(-1));
    }

    @Test
    void pickUniformIndex_returnsValidIndexForSizeTen() {
        RandomIndexSelector selector = new RandomIndexSelector(new Random(42));
        int size = 10;
        int index = selector.pickUniformIndex(size);
        assertTrue(index >= 0 && index < size, "Index should be in range [0, " + (size - 1) + "]");
    }

    @Test
    void pickUniformIndex_distributionApproximatesUniform() {
        RandomIndexSelector selector = new RandomIndexSelector(new Random(456));
        int[] counts = new int[10];
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            int index = selector.pickUniformIndex(10);
            counts[index]++;
        }

        // Expected probability: 10% for each index
        // Allow 2% tolerance
        double tolerance = 0.02;
        for (int i = 0; i < 10; i++) {
            assertEquals(0.1, (double) counts[i] / iterations, tolerance);
        }
    }
}
