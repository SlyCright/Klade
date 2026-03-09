package site.klade.webapp.service;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import site.klade.simulation.Genome;
import site.klade.simulation.SimulationSnapshotDto;
import site.klade.webapp.simulation.Simulation;

@Service
public class SimulationService {

    private final Simulation simulation = Simulation.withDefaultSettings();

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

    @PreDestroy
    public void shutdown() {
        simulation.shutdown();
    }

    // TODO: use Spring's events instead of polling for data. Simulation should notify Spring
    //  application when next generation is ready. Frontend should get notifications as well
    public SimulationSnapshotDto getSimulationSnapshot() {
        return simulation.getSnapshot();
    }

    // TODO: that is domain's logic move to the Simulation level
    public Genome getBestGenome() {
        var snapshot = simulation.getSnapshot();
        return snapshot.getSpeciesList().stream()
                .flatMap(species -> species.getGenomes().stream())
                .reduce((g1, g2) -> g1.getFitness() < g2.getFitness() ? g1 : g2)
                .map(Genome::new)
                .orElseGet(() -> new Genome(snapshot.getSpeciesList().get(0).getGenomes().get(0)));
    }
}