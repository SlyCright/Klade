package site.klade.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.klade.simulation.SimulationSnapshotDto;
import site.klade.webapp.simulation.Simulation;

@Slf4j
@Service
public class SimulationSnapshotService {

    private final Simulation simulation;

    public SimulationSnapshotService(SimulationLifecycleService lifecycleService) {
        this.simulation = lifecycleService.getSimulation();
    }

    public SimulationSnapshotDto getSimulationSnapshot() {
        return simulation.getSnapshot();
    }
}
