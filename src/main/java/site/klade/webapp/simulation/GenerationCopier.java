package site.klade.webapp.simulation;

import site.klade.simulation.Genome;
import java.util.ArrayList;

public class GenerationCopier {

    public static ArrayList<Species> getDeepCopyOf(ArrayList<Species> speciesList) {
        var newSpeciesList = new ArrayList<Species>();
        for (Species speciesToBeCopied : speciesList) {
            var genomes = new ArrayList<Genome>();
            for (Genome genomeToBeCopied : speciesToBeCopied.getGenomes()) {
                genomes.add(new Genome(genomeToBeCopied));
            }
            // Statistics were already calculated on the source (EvolutionEngine after
            // evaluateFitness); copy them instead of recomputing. Null stays null.
            newSpeciesList.add(new Species(genomes,
                    speciesToBeCopied.getAverageFitness(),
                    speciesToBeCopied.getBestFitness()));
        }
        return newSpeciesList;
    }
}
