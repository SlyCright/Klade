package site.klade.webapp.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SimulationPropertiesTest {

    @Autowired
    private SimulationProperties properties;

    @DisplayName("Given simulation.yaml on the classpath, when the context loads, then nested evolution mutation settings are bound")
    @Test
    void givenSimulationYaml_whenContextLoads_thenEvolutionMutationSettingsBound() {
        // Then: the nested evolution.mutation block carries the YAML value
        // (simulation.evolution.mutation.deletion-bias: 0.05)
        assertThat(properties.getEvolution().getMutation()).isNotNull();
        assertThat(properties.getEvolution().getMutation().getDeletionBias()).isEqualTo(0.05);
        assertThat(properties.getEvolution().getInitialHyperGene()).isEqualTo(1.0f);
    }

    @DisplayName("Given simulation.yaml on the classpath, when the context loads, then the simulation root prefix is bound")
    @Test
    void givenSimulationYaml_whenContextLoads_thenSimulationRootPrefixBound() {
        // Then: values from the simulation block are bound (run lifecycle settings)
        assertThat(properties.getSpeciesTotal()).isEqualTo(3);
        assertThat(properties.getSpecimensPerSpecies()).isEqualTo(10);
        assertThat(properties.getSleepPerUpdateMillis()).isEqualTo(100);
    }
}
