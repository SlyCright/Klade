package site.klade.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "species")
public class SpeciesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer speciesIndex;

    private Double averageFitness;

    private Double maxFitness;

    @OneToMany(mappedBy = "species", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpecimenEntity> specimens = new ArrayList<>();

    public SpeciesEntity() {
    }

    public SpeciesEntity(Integer speciesIndex, Double averageFitness, Double maxFitness) {
        this.speciesIndex = speciesIndex;
        this.averageFitness = averageFitness;
        this.maxFitness = maxFitness;
    }
}
