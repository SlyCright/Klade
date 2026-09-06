package site.klade.webapp.simulation;

import lombok.Getter;

import java.util.ArrayList;

/**
 * Live snapshot of the simulation state at the moment a generation finishes.
 * Pure data carrier — fitness statistics live in {@link FitnessStatistics},
 * display formatting in SimulationSnapshotService (service layer).
 */
@Getter
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

}
