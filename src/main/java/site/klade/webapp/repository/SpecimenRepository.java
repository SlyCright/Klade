package site.klade.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.klade.webapp.entity.SpecimenEntity;

public interface SpecimenRepository extends JpaRepository<SpecimenEntity, Long> {

}
