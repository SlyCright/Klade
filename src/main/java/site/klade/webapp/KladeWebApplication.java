package site.klade.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import site.klade.webapp.config.SimulationProperties;

@EnableAsync
@EnableConfigurationProperties(SimulationProperties.class)
@SpringBootApplication
public class KladeWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(KladeWebApplication.class, args);
    }
}