package site.klade.webapp.simulation;

import site.klade.simulation.Genome;

import java.util.List;

/**
 * Aggregated fitness statistics over a set of genomes of one species.
 *
 * <p><b>FITNESS SEMANTICS — read this before touching the comparisons:</b></p>
 * <ul>
 *   <li>In the current MVP the fitness function is "proximity to the arena center":
 *       the fitness value IS the specimen's distance to the center.</li>
 *   <li>Therefore <b>LOWER fitness = BETTER specimen</b>, and the best fitness of a
 *       group is the <b>MINIMUM</b> of its values — not the maximum. Do not "fix"
 *       min-comparisons to max: that would silently invert selection.</li>
 *   <li>{@code Float.MAX_VALUE} is the "not evaluated / invalid" sentinel used by the
 *       simulation library ({@code Genome} initializes fitness with it). Genomes with
 *       this value are excluded from the statistics.</li>
 * </ul>
 */
public final class FitnessStatistics {

    private final float bestFitness;

    private final float totalFitness;

    private final int count;

    private FitnessStatistics(float bestFitness, float totalFitness, int count) {
        this.bestFitness = bestFitness;
        this.totalFitness = totalFitness;
        this.count = count;
    }

    public static FitnessStatistics of(List<Genome> genomes) {
        // Start from the "worst possible" sentinel and track the MINIMUM (lower = better).
        float bestFitness = Float.MAX_VALUE;
        float totalFitness = 0;
        int count = 0;

        for (Genome genome : genomes) {
            float fitness = genome.getFitness();
            if (fitness < Float.MAX_VALUE) { // exclude "not evaluated" genomes
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                }
                totalFitness += fitness;
                count++;
            }
        }

        return new FitnessStatistics(bestFitness, totalFitness, count);
    }

    /**
     * @return the best (i.e. the smallest, lower = better) fitness among evaluated genomes,
     * or {@code Float.MAX_VALUE} if none were evaluated.
     */
    public float getBestFitness() {
        return bestFitness;
    }

    public float getTotalFitness() {
        return totalFitness;
    }

    /**
     * @return the average fitness, or {@code Float.MAX_VALUE} if none were evaluated.
     */
    public float getAverageFitness() {
        return count > 0 ? totalFitness / count : Float.MAX_VALUE;
    }

    /**
     * @return how many genomes had a valid (evaluated) fitness.
     */
    public int getCount() {
        return count;
    }
}
