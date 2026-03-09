package site.klade.webapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.klade.simulation.Genome;
import site.klade.webapp.service.SimulationService;

@RestController
@RequestMapping("/api")
public class GenomeController {

    private final SimulationService simulationService;

    public GenomeController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/best-genome")
    public GenomeDto getBestGenome() {
        Genome best = simulationService.getBestGenome();
        if (best == null) {
            // Return a minimal placeholder or null; client will handle it.
            return null;
        }
        return new GenomeDto(
            best.getStartPosition().x,
            best.getStartPosition().y,
            best.getInitialImpulse().x,
            best.getInitialImpulse().y,
            best.getFitness()
        );
    }
}