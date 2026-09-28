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
     * Ontogenesis condition-expression limits (see {@code site.klade.simulation.condition.ConditionParser}).
     */
    private Condition condition = new Condition();

    /**
     * Genetics / evolutionary parameters consumed by the mutator pipeline
     * ({@code site.klade.webapp.evolution}).
     */
    private Evolution evolution = new Evolution();

    // TODO: ArenaSettings immutable (no setters) — Spring may silently ignore YAML.
    //  CHECK: change YAML, restart, GET /api/arena-settings; if unchanged, binding silently fails.

    private ArenaSettings arena = new ArenaSettings(
            300f,
            0.01f,
            18f,
            10f,
            3000);

    /**
     * Genetics / evolutionary parameters.
     */
    @Data
    public static class Evolution {

        /**
         * Initial hyper-gene value (R_max) for generation-0 genomes.
         * Controls the starting mutation intensity before self-adaptation kicks in.
         * Domain: (0, 1].
         */
        private float initialHyperGene = 1.0f;

        /**
         * Mutation tuning consumed by the mutator pipeline
         * ({@code site.klade.webapp.evolution}).
         */
        private Mutation mutation = new Mutation();
    }

    /**
     * Mutation-related tuning values.
     */
    @Data
    public static class Mutation {

        /**
         * Extra probability of forcing a {@code DELETION} structural mutation on top of the
         * flat mutation-type distribution.
         * <p>
         * With the six structural mutation types of
         * {@code site.klade.webapp.evolution.GeneStructuralMutator} the resulting probability is
         * {@code P(DELETION) = 1/6 + deletionBias * 5/6} — for {@code 0.05} that is ~20.8%
         * instead of ~16.7%, so genomes can shed unnecessary genes over long runs.
         * Domain: [0.0, 1.0].
         */
        private double deletionBias = 0.05;
    }

    /**
     * Condition-expression limits.
     */
    @Data
    public static class Condition {

        /**
         * Maximum depth of a condition AST, counting a leaf as depth 1.
         * Enforced by ConditionParser and structural mutation.
         */
        private int maxTreeDepth = 64;

        /**
         * Maximum parenthesis nesting the parser will recurse through.
         */
        private int maxParenDepth = 256;
    }

}