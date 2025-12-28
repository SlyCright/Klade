package site.klade.webapp.service;

import jakarta.annotation.PostConstruct;
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
public class AsyncSimulationService {

    private final AtomicReference<Simulation> currentSimulation = new AtomicReference<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    private Thread simulationThread;

    @Async
    @SneakyThrows
    //@PostConstruct
    @SuppressWarnings("BusyWait")
    public void startSimulation() {
        log.info("Starting simulation");
        simulationThread = Thread.currentThread();
        running.set(true);
        currentSimulation.set(new Simulation());
        long loopCounter = 0;
        while (running.get()) {
            currentSimulation.get().update();
            Thread.sleep(10);
            if (loopCounter % 100 == 0) {
                SimulationDto simulationDto = currentSimulation.get().getSimulationDto();
                log.info("Total ticks: {}, Rhyme node state changes: {}, Rhyme node active: {}",
                        simulationDto.getTotalTicks(),
                        simulationDto.getRhymeNodeStateChanges(),
                        simulationDto.getIsRhymeNodeCurrentlyActive());
            }
            loopCounter++;
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
}