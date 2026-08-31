package site.klade.webapp.simulation;

import site.klade.simulation.Species;

import java.util.ArrayList;

public class SimulationSnapshotDto {

    private int generationNumber;

    private ArrayList<Species> speciesList;

    // Add no-arg constructor for Jackson
    public SimulationSnapshotDto() {
    }

    public SimulationSnapshotDto(int generationNumber, ArrayList<Species> speciesList) {
        this.generationNumber = generationNumber;
        this.speciesList = speciesList;
    }

    public int getGenerationNumber() {
        return this.generationNumber;
    }

    public ArrayList<Species> getSpeciesList() {
        return this.speciesList;
    }

    private static class FitnessStats {
        float bestFitness;
        float totalFitness;
        int count;

        FitnessStats(float bestFitness, float totalFitness, int count) {
            this.bestFitness = bestFitness;
            this.totalFitness = totalFitness;
            this.count = count;
        }
    }

    private FitnessStats calculateFitnessStats(ArrayList<site.klade.simulation.Genome> genomes) {
        float bestFitness = Float.MAX_VALUE;
        float totalFitness = 0;
        int count = 0;

        for (site.klade.simulation.Genome genome : genomes) {
            float fitness = genome.getFitness();
            if (fitness < Float.MAX_VALUE) {
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                }
                totalFitness += fitness;
                count++;
            }
        }

        return new FitnessStats(bestFitness, totalFitness, count);
    }

    public String toString() {
        int totalSpecimens = 0;
        float bestFitnessOverall = Float.MAX_VALUE;
        float totalFitness = 0;
        int fitnessCount = 0;

        if (this.speciesList != null) {
            for (Species species : this.speciesList) {
                if (species.getGenomes() != null) {
                    totalSpecimens += species.getGenomes().size();
                    FitnessStats stats = calculateFitnessStats(species.getGenomes());
                    if (stats.bestFitness < bestFitnessOverall) {
                        bestFitnessOverall = stats.bestFitness;
                    }
                    totalFitness += stats.totalFitness;
                    fitnessCount += stats.count;
                }
            }
        }

        float avgFitnessOverall = fitnessCount > 0 ? totalFitness / fitnessCount : Float.MAX_VALUE;
        String bestFitnessStr = bestFitnessOverall < Float.MAX_VALUE ? String.valueOf(Math.round(bestFitnessOverall * 100) / 100.0) : "N/A";
        String avgFitnessStr = avgFitnessOverall < Float.MAX_VALUE ? String.valueOf(Math.round(avgFitnessOverall * 100) / 100.0) : "N/A";

        StringBuilder sb = new StringBuilder();
        sb.append("SimulationSnapshot{generation=").append(this.generationNumber)
            .append(", speciesCount=").append(this.speciesList != null ? this.speciesList.size() : 0)
            .append(", totalSpecimens=").append(totalSpecimens)
            .append(", bestFitness=").append(bestFitnessStr)
            .append(", avgFitness=").append(avgFitnessStr).append("}");

        if (this.speciesList != null) {
            for (int i = 0; i < this.speciesList.size(); i++) {
                Species species = this.speciesList.get(i);
                if (species.getGenomes() != null) {
                    int specimenCount = species.getGenomes().size();
                    FitnessStats stats = calculateFitnessStats(species.getGenomes());

                    float speciesAvg = stats.count > 0 ? stats.totalFitness / stats.count : Float.MAX_VALUE;
                    String speciesBestStr = stats.bestFitness < Float.MAX_VALUE ? String.valueOf(Math.round(stats.bestFitness * 100) / 100.0) : "N/A";
                    String speciesAvgStr = speciesAvg < Float.MAX_VALUE ? String.valueOf(Math.round(speciesAvg * 100) / 100.0) : "N/A";

                    sb.append("\n  Species[").append(i).append("]: specimens=").append(specimenCount)
                        .append(", bestFitness=").append(speciesBestStr)
                        .append(", avgFitness=").append(speciesAvgStr);
                }
            }
        }

        return sb.toString();
    }
}
