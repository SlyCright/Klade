package site.klade.webapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.klade.simulation.SimulationSnapshotDto;
import site.klade.webapp.entity.GenerationEntity;
import site.klade.webapp.repository.GenerationRepository;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class GenerationPersistenceService {

    private final GenerationRepository generationRepository;

    private final ObjectMapper objectMapper;

    private final AtomicBoolean saveInProgress = new AtomicBoolean(false);

    public GenerationPersistenceService(GenerationRepository generationRepository, ObjectMapper objectMapper) {
        this.generationRepository = generationRepository;
        this.objectMapper = objectMapper;
    }

    public void saveGenerationSnapshot(SimulationSnapshotDto snapshot) {
        if (!saveInProgress.compareAndSet(false, true)) {
            log.debug("Skipping save for generation {} – DB busy", snapshot.getGenerationNumber());
            return;
        }
        performAsyncSave(snapshot);
    }

    @Async
    public void performAsyncSave(SimulationSnapshotDto snapshot) {
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            GenerationEntity entity = generationRepository.findById(1L)
                    .orElse(new GenerationEntity());
            entity.setGenerationNumber(snapshot.getGenerationNumber());
            entity.setData(json);
            generationRepository.save(entity);
            log.debug("Saved generation {} to DB", snapshot.getGenerationNumber());
        } catch (Exception e) {
            log.error("Failed to save generation snapshot", e);
        } finally {
            saveInProgress.set(false);
        }
    }
}
