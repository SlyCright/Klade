package site.klade.webapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import site.klade.simulation.ArenaSettings;

/**
 * Common project simulation settings, loaded from {@code simulation.yaml}
 * (imported by application.yaml). All simulation-tuning values belong HERE —
 * do not hardcode them in the simulation classes.
 */
@Data
@ConfigurationProperties(prefix = "simulation")
public class SimulationProperties {

    private int speciesTotal = 3;

    private int specimensPerSpecies = 10;

    private int sleepPerUpdateMillis = 100;

    /**
     * Base mutation chances (will be scaled by mutationFactor in rank-linear reproduction).
     */
    private double baseMetaGeneMutationChance = 0.15;

    private double baseMorphogenMutationChance = 0.15;

    private double baseGeneMutationChance = 0.15;

    // TODO: ArenaSettings immutable (no setters) — Spring may silently ignore YAML.
    //  CHECK: change YAML, restart, GET /api/arena-settings; if unchanged, binding silently fails.

    private ArenaSettings arena = new ArenaSettings(
            300f,
            0.01f,
            18f,
            10f,
            3000);

}