package site.klade.webapp.converter;

import site.klade.simulation.Genome;
import site.klade.webapp.entity.GenerationEntity;
import site.klade.webapp.entity.SpeciesEntity;
import site.klade.webapp.entity.SpecimenEntity;
import site.klade.webapp.simulation.FitnessStatistics;
import site.klade.webapp.simulation.SimulationSnapshotDto;
import site.klade.webapp.simulation.Species;

import java.util.ArrayList;

/**
 * Maps the live simulation snapshot of one generation
 * ({@link SimulationSnapshotDto}, the simulation-side data) onto the
 * persistence entities. This is DTO-to-entity mapping, therefore it lives
 * in the converter package as well.
 */
public final class SimulationSnapshotConverter {

    private SimulationSnapshotConverter() {
    }

    /**
     * Copies all species (with their specimens and fitness stats) from the snapshot
     * into the given persistent {@link GenerationEntity} target, replacing whatever
     * species it held before.
     */
    public static void copySpeciesInto(GenerationEntity target, SimulationSnapshotDto generation) {
        target.getSpecies().clear();

        ArrayList<Species> speciesList = generation.getSpeciesList();
        if (speciesList == null) {
            return;
        }

        for (int speciesIndex = 0; speciesIndex < speciesList.size(); speciesIndex++) {
            ArrayList<Genome> genomes = speciesList.get(speciesIndex).getGenomes();
            if (genomes == null || genomes.isEmpty()) {
                continue;
            }
            target.getSpecies().add(toSpeciesEntity(target, speciesIndex, genomes));
        }
    }

    private static SpeciesEntity toSpeciesEntity(GenerationEntity generation, int speciesIndex, ArrayList<Genome> genomes) {
        /*
         * FITNESS SEMANTICS — see FitnessStatistics for the full explanation.
         * - Fitness = distance to the arena center => LOWER = BETTER.
         * - bestFitness = MINIMUM of valid values, NOT the maximum.
         * - Genomes with Float.MAX_VALUE ("not evaluated" sentinel) are excluded.
         * - If NO genome of the species has a valid fitness, bestFitness is stored as
         *   NULL (unknown) rather than a fake 0 — 0 would mean "perfect specimen".
         */
        FitnessStatistics stats = FitnessStatistics.of(genomes);
        double averageFitness = stats.getCount() > 0 ? stats.getAverageFitness() : 0;
        Double bestFitness = stats.getCount() > 0 ? (double) stats.getBestFitness() : null;

        SpeciesEntity speciesEntity = new SpeciesEntity(generation, speciesIndex, averageFitness, bestFitness);

        for (Genome genome : genomes) {
            SpecimenEntity specimen = new SpecimenEntity(
                    speciesEntity,
                    (double) genome.getFitness(),
                    genome.toString() // DNA serialized as string
            );
            speciesEntity.getSpecimens().add(specimen);
        }

        return speciesEntity;
    }
}
