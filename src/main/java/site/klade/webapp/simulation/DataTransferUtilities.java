package site.klade.webapp.simulation;

import site.klade.simulation.Genome;
import java.util.ArrayList;

public class DataTransferUtilities {

    public static ArrayList<Species> getDeepCopyOf(ArrayList<Species> speciesList) {
        var newSpeciesList = new ArrayList<Species>();
        for (Species speciesToBeCopied : speciesList) {
            var genomes = new ArrayList<Genome>();
            for (Genome genomeToBeCopied : speciesToBeCopied.getGenomes()) {
                genomes.add(new Genome(genomeToBeCopied));
            }
            newSpeciesList.add(new Species(genomes));
        }
        return newSpeciesList;
    }
}
