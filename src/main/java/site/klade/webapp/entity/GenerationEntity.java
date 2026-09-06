package site.klade.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "generation")
public class GenerationEntity {

    @Id
    private Long id = 1L;  // single row, fixed ID

    private Integer generationNumber;

    @OneToMany(mappedBy = "generation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpeciesEntity> species = new ArrayList<>();
}