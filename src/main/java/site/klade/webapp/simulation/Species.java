package site.klade.webapp.simulation;

import lombok.Getter;
import site.klade.simulation.Genome;

import java.util.ArrayList;

@Getter
public class Species {

    private ArrayList<Genome> genomes = new ArrayList<Genome>();

    private Double averageFitness;

    private Double bestFitness;

    public Species() {
        this.genomes = new ArrayList<Genome>();
    }

    public Species(int specimensPerSpecies) {
        for (int i = 0; i < specimensPerSpecies; i++) {
            genomes.add(new Genome());
        }
    }

    public Species(ArrayList<Genome> genomes) {
        this.genomes.addAll(genomes);
    }

    public void calculateFitnessStatistics() {
        FitnessStatistics stats = FitnessStatistics.of(genomes);
        if (stats.getCount() > 0) {
            this.averageFitness = (double) stats.getAverageFitness();
            this.bestFitness = (double) stats.getBestFitness();
        } else {
            this.averageFitness = null;
            this.bestFitness = null;
        }
    }

    public String toString() {
        return String.format("{\"genomes\": %s}", this.genomes);
    }

}
