package site.klade.webapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "simulation")
public class SimulationProperties {

    private float specimenMaxInitialDistance = 200f;
    private float specimenMinInitialDistance = 150f;
}
