package site.klade.webapp.simulation;

import lombok.Getter;
import site.klade.simulation.Genome;

import java.util.List;

@Getter
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
        float bestFitness = Float.MAX_VALUE;
        float totalFitness = 0;
        int count = 0;
        for (Genome genome : genomes) {
            float fitness = genome.getAccumulatedFitness();
            if (fitness < bestFitness) {
                bestFitness = fitness;
            }
            totalFitness += fitness;
            count++;
        }
        return new FitnessStatistics(bestFitness, totalFitness, count);
    }

    /**
     * @return the average fitness, or {@code Float.MAX_VALUE} if none were evaluated.
     */
    public float getAverageFitness() {
        return count > 0 ? totalFitness / count : Float.MAX_VALUE;
    }

}
