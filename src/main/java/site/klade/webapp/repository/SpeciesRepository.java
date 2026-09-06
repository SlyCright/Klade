package site.klade.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.klade.webapp.entity.SpeciesEntity;

public interface SpeciesRepository extends JpaRepository<SpeciesEntity, Long> {

}
