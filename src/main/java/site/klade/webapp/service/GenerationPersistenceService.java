package site.klade.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.klade.webapp.entity.GenerationEntity;
import site.klade.webapp.entity.SpeciesEntity;
import site.klade.webapp.entity.SpecimenEntity;
import site.klade.webapp.parser.GenomeParser;
import site.klade.webapp.repository.GenerationRepository;
import site.klade.webapp.repository.SpeciesRepository;
import site.klade.webapp.repository.SpecimenRepository;
import site.klade.webapp.simulation.Generation;
import site.klade.webapp.simulation.Species;
import site.klade.simulation.Genome;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class GenerationPersistenceService {

    private static final long CURRENT_GENERATION_ID = 1L;

    private final GenerationRepository generationRepository;

    private final SpeciesRepository speciesRepository;

    private final SpecimenRepository specimenRepository;

    private final GenomeParser genomeParser = new GenomeParser();

    private final AtomicBoolean saveInProgress = new AtomicBoolean(false);

    public GenerationPersistenceService(
            GenerationRepository generationRepository,
            SpeciesRepository speciesRepository,
            SpecimenRepository specimenRepository) {
        this.generationRepository = generationRepository;
        this.speciesRepository = speciesRepository;
        this.specimenRepository = specimenRepository;
    }

    /**
     * Persists a generation asynchronously. Skips the save if a previous
     * save is still in progress (single-writer guard).
     */
    @Async
    @Transactional
    public void saveGeneration(Generation generation) {
        if (!saveInProgress.compareAndSet(false, true)) {
            log.debug("Skipping save for generation {} – DB busy", generation.getGenerationNumber());
            return;
        }
        try {
            replacePersistedGeneration(generation);
            log.debug("Saved generation {} to DB", generation.getGenerationNumber());
        } catch (Exception e) {
            log.error("Failed to save generation", e);
        } finally {
            saveInProgress.set(false);
        }
    }

    /**
     * Single-row persistence design (MVP): the DB intentionally keeps ONLY the current
     * generation — one fixed row (id = {@value CURRENT_GENERATION_ID}), whose species
     * and specimens are fully replaced on every save. No history is kept; that is out
     * of the MVP scope (see the project vault, "Database Schema").
     */
    private void replacePersistedGeneration(Generation generation) {
        GenerationEntity entity = getOrCreateGenerationEntity();
        clearPreviousData(entity);
        entity.setGenerationNumber(generation.getGenerationNumber());
        mapSpeciesToEntities(generation, entity);
        generationRepository.save(entity);
    }

    private GenerationEntity getOrCreateGenerationEntity() {
        return generationRepository.findById(CURRENT_GENERATION_ID)
                .orElse(new GenerationEntity());
    }

    private void clearPreviousData(GenerationEntity entity) {
        entity.getSpecies().clear();
        speciesRepository.deleteAll();
        specimenRepository.deleteAll();
    }

    private void mapSpeciesToEntities(Generation generation, GenerationEntity entity) {
        List<Species> speciesList = generation.getSpeciesList();
        for (int speciesIndex = 0; speciesIndex < speciesList.size(); speciesIndex++) {
            Species species = speciesList.get(speciesIndex);
            SpeciesEntity speciesEntity = mapSpeciesToEntity(species, entity, speciesIndex);
            entity.getSpecies().add(speciesEntity);
        }
    }

    private SpeciesEntity mapSpeciesToEntity(Species species, GenerationEntity generation, int speciesIndex) {
        SpeciesEntity speciesEntity = new SpeciesEntity();
        speciesEntity.setGeneration(generation);
        speciesEntity.setSpeciesIndex(speciesIndex);
        List<Genome> genomes = species.getGenomes();
        List<SpecimenEntity> specimenEntities = mapGenomesToSpecimens(genomes, speciesEntity);
        speciesEntity.setSpecimens(specimenEntities);
        speciesEntity.setAverageFitness(species.getAverageFitness());
        speciesEntity.setBestFitness(species.getBestFitness());
        return speciesEntity;
    }

    private List<SpecimenEntity> mapGenomesToSpecimens(List<Genome> genomes, SpeciesEntity speciesEntity) {
        List<SpecimenEntity> specimenEntities = new ArrayList<>();
        for (Genome genome : genomes) {
            SpecimenEntity specimenEntity = new SpecimenEntity();
            specimenEntity.setSpecies(speciesEntity);
            specimenEntity.setFitness((double) genome.getAccumulatedFitness());
            specimenEntity.setGenome(genomeParser.serialize(genome));
            specimenEntities.add(specimenEntity);
        }
        return specimenEntities;
    }

}

