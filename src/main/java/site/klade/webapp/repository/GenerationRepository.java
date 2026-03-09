package site.klade.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.klade.webapp.entity.GenerationEntity;

public interface GenerationRepository extends JpaRepository<GenerationEntity, Long> {

}