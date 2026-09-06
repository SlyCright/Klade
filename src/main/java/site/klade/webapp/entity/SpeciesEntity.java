package site.klade.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "species")
public class SpeciesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_id", nullable = false)
    private GenerationEntity generation;

    private Integer speciesIndex;

    private Double averageFitness;

    /**
     * Fitness semantics: LOWER = BETTER (fitness is the distance to the arena
     * center). This is the minimum among evaluated specimens, or {@code null}
     * when no specimen of the species has an evaluated fitness.
     */
    private Double bestFitness;

    @OneToMany(mappedBy = "species", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpecimenEntity> specimens = new ArrayList<>();

    public SpeciesEntity() {
    }

    public SpeciesEntity(GenerationEntity generation, Integer speciesIndex, Double averageFitness, Double bestFitness) {
        this.generation = generation;
        this.speciesIndex = speciesIndex;
        this.averageFitness = averageFitness;
        this.bestFitness = bestFitness;
    }
}
