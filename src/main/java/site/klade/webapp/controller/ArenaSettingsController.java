package site.klade.webapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.klade.simulation.ArenaSettings;
import site.klade.webapp.config.SimulationProperties;
import site.klade.webapp.dto.ArenaSettingsDto;

@RestController
@RequestMapping("/api")
public class ArenaSettingsController {

    private final SimulationProperties simulationProperties;

    public ArenaSettingsController(SimulationProperties simulationProperties) {
        this.simulationProperties = simulationProperties;
    }

    @GetMapping("/arena-settings")
    public ArenaSettingsDto getArenaSettings() {
        ArenaSettings arena = simulationProperties.getArena();
        return new ArenaSettingsDto(
            arena.getInitialDistance(),
            arena.getFrictionFactor(),
            arena.getNodeSize(),
            arena.getRepulsionFactor(),
            arena.getMaxTicks()
        );
    }
}
