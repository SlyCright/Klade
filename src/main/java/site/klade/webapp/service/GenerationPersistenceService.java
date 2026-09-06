package site.klade.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.klade.webapp.converter.GenerationConverter;
import site.klade.webapp.converter.SimulationSnapshotConverter;
import site.klade.webapp.dto.GenerationDto;
import site.klade.webapp.entity.GenerationEntity;
import site.klade.webapp.repository.GenerationRepository;
import site.klade.webapp.repository.SpeciesRepository;
import site.klade.webapp.repository.SpecimenRepository;
import site.klade.webapp.simulation.SimulationSnapshotDto;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class GenerationPersistenceService {

    private static final long CURRENT_GENERATION_ID = 1L;

    private final GenerationRepository generationRepository;

    private final SpeciesRepository speciesRepository;

    private final SpecimenRepository specimenRepository;

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
     *
     * @param generation the generation to persist; despite the origin (a live snapshot
     *                   of the simulation state), it is just a generation — the service
     *                   does not care whether it will ever be kept as "the current one"
     */
    @Async
    @Transactional
    public void saveGeneration(SimulationSnapshotDto generation) {
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
     * Returns the currently persisted generation mapped to its DTO tree
     * (generation -&gt; species -&gt; specimens), or {@code null} if nothing was persisted yet.
     */
    @Transactional(readOnly = true)
    public GenerationDto getCurrentGeneration() {
        return generationRepository.findById(CURRENT_GENERATION_ID)
                .map(GenerationConverter::toDto)
                .orElse(null);
    }

    /**
     * Single-row persistence design (MVP): the DB intentionally keeps ONLY the current
     * generation — one fixed row (id = {@value CURRENT_GENERATION_ID}), whose species
     * and specimens are fully replaced on every save. No history is kept; that is out
     * of the MVP scope (see the project vault, "Database Schema").
     */
    private void replacePersistedGeneration(SimulationSnapshotDto generation) {
        GenerationEntity entity = generationRepository.findById(CURRENT_GENERATION_ID)
                .orElse(new GenerationEntity());
        entity.setGenerationNumber(generation.getGenerationNumber());

        // Drop the previous generation's species and specimens (single-row replace).
        entity.getSpecies().clear();
        speciesRepository.deleteAll();
        specimenRepository.deleteAll();

        // Map the incoming generation onto the entities (see converter package)
        // and attach it to the persistent row.
        SimulationSnapshotConverter.copySpeciesInto(entity, generation);

        generationRepository.save(entity);
    }
}
