package site.klade.webapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.klade.simulation.Genome;
import site.klade.webapp.dto.GenomeDto;
import site.klade.webapp.dto.GenomesDto;
import site.klade.webapp.service.GenomeQueryService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GenomeController {

    private final GenomeQueryService genomeQueryService;

    public GenomeController(GenomeQueryService genomeQueryService) {
        this.genomeQueryService = genomeQueryService;
    }

    @GetMapping("/best-genome")
    public GenomesDto getBestGenome() {
        List<Genome> bestGenomes = genomeQueryService.getBestGenomesPerSpecies();
        if (bestGenomes == null) {
            // Return a minimal placeholder or null; client will handle it.
            return null;
        }
        List<GenomeDto> genomeDtos = new ArrayList<>();
        int speciesIndex = 0;
//        for (Genome best : bestGenomes) {
//            genomeDtos.add(new GenomeDto(
//                best.getStartPosition().x,
//                best.getStartPosition().y,
//                best.getInitialImpulse().x,
//                best.getInitialImpulse().y,
//                best.getFitness(),
//                speciesIndex
//            ));
//            speciesIndex++;
//        }
        return new GenomesDto(genomeDtos);
    }
}