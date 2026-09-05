package site.klade.webapp.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.klade.webapp.simulation.Simulation;

@Slf4j
@Service
public class SimulationLifecycleService {

    private final Simulation simulation;

    public SimulationLifecycleService() {
        this.simulation = Simulation.withDefaultSettings();
    }

    public void start() {
        simulation.start();
    }

    public void runCertainGenerations(int number) {
        simulation.runCertainGenerations(number);
    }

    public void stop() {
        simulation.stop();
    }

    public void reset() {
        simulation.reset();
    }

    public Simulation getSimulation() {
        return simulation;
    }

    @PreDestroy
    public void shutdown() {
        simulation.shutdown();
    }
}
