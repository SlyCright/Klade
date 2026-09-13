package site.klade.webapp.service;

/**
 * Per-species fitness statistics as read from the persisted generation (DB row),
 * used by the Vaadin HUD. Pure value type — no Jackson/REST mapping needed since
 * the only consumer is the server-side view.
 *
 * @param speciesIndex  index of the species within the generation
 * @param specimenCount number of specimens persisted for the species
 * @param averageFitness species average fitness, or {@code null} when not evaluated
 * @param bestFitness    species best (lowest = best) fitness, or {@code null} when not evaluated
 */
public record SpeciesStats(
        int speciesIndex,
        int specimenCount,
        Double averageFitness,
        Double bestFitness
) {
}
