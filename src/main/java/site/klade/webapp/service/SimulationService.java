package site.klade.webapp.service;

import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.klade.simulation.Simulation;
import site.klade.simulation.SimulationDto;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.reflections.Reflections.log;

@Service
@SuppressWarnings("unused")
public class SimulationService {

    private final AtomicReference<Simulation> currentSimulation = new AtomicReference<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    private Thread simulationThread;

    @Async
    @SneakyThrows
    @SuppressWarnings("BusyWait")
    public void startSimulation() {
        log.info("Starting simulation");
        simulationThread = Thread.currentThread();
        running.set(true);
        currentSimulation.set(new Simulation());
        long loopCounter = 0;
        while (running.get()) {
            currentSimulation.get().update();
            Thread.sleep(2);
            if (loopCounter % 1000 == 0) {
                SimulationDto simulationDto = currentSimulation.get().getSimulationDto();
                log.info("Total ticks: {}, Rhyme node state changes: {}, Rhyme node active: {}",
                        simulationDto.getTotalTicks(),
                        simulationDto.getRhymeNodeStateChanges(),
                        simulationDto.getIsRhymeNodeCurrentlyActive());
            }
            loopCounter++;
            if (loopCounter > 1_000_000) stopSimulation();
        }
    }

    @SneakyThrows
    public void stopSimulation() {
        running.set(false);
        Thread.sleep(50);
        if (simulationThread.isAlive()) {
            simulationThread.interrupt();
        }
    }

    @SneakyThrows
    public void restartSimulation() {
        stopSimulation();
        Thread.sleep(50);
        startSimulation();
    }

    public SimulationDto getSimulationDto() {
        return currentSimulation.get().getSimulationDto();
    }

    public boolean isRunning() {
        return running.get();
    }

    public void start() {

    }

    public void runCertainGenerations(int i) {

    }

    public void stop() {

    }

    public void reset() {

    }

    public Object getSimulationSnapshot() {
        return null;
    }
}