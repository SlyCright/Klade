package site.klade.webapp.simulation;

import site.klade.simulation.Genome;
import java.util.ArrayList;

public class Species {

    private  ArrayList<Genome> genomes = new ArrayList<Genome>();

    public Species() {
        this.genomes = new ArrayList<Genome>();
    }

    public Species(int specimensPerSpecies) {
        for (int i = 0; i < specimensPerSpecies; i++) {
//            genomes.add(new Genome());
        }
    }

    public Species(ArrayList<Genome> genomes) {
        this.genomes.addAll(genomes);
    }

    public String toString() {
        return String.format("{\"genomes\": %s}", this.genomes);
    }

    public ArrayList<Genome> getGenomes() {
        return this.genomes;
    }

}
