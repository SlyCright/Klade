package site.klade.webapp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import site.klade.simulation.condition.ConditionParser;

/**
 * Configures the ConditionParser limits from simulation.yaml at application startup.
 * This threads the configuration values from the webapp layer into the GWT-shared
 * simulation library, which has no Spring and cannot read the YAML directly.
 */
@Configuration
@EnableConfigurationProperties(SimulationProperties.class)
public class ConditionParserConfig {

    private final SimulationProperties simulationProperties;

    public ConditionParserConfig(SimulationProperties simulationProperties) {
        this.simulationProperties = simulationProperties;
    }

    @PostConstruct
    public void configureConditionParser() {
        ConditionParser.configureLimits(
                simulationProperties.getCondition().getMaxTreeDepth(),
                simulationProperties.getCondition().getMaxParenDepth()
        );
    }
}
