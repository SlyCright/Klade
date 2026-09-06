package site.klade.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "specimen")
public class SpecimenEntity {

    public static final String EXAMPLE_DNA = """
            --- Meta genes
            InitialAngle: 45.0 (Float)
            
            --- Morphogens
            # Morphogen[id], [diffusion ratio], [decay ratio], [spreading conditions]
            Morphogen[1], 0.80, 0.1, everywhere
            Morphogen[2], 0.95, 0.2, up
            
            --- Genes
            # [condition] [action] [parameters]
            become friction_node
            lay_segment 30°
            express Mrph[1] 2.0
            wait
            wait
            if Mrph[1] > 1.0 become muscle
            if Mrph[1] > 1.0 become rhythm_node
            
            # "if" statement can be omitted
            # also "if" statement can be combined:
            # Mrph[1] < 1.0 AND (Mrph[2] > 5.0 OR (Mrph[3] > 2.5)
            
            #Possible commands (for MVP):
            # become [node type] or [segment type]
            # lay_segment [angle] or [direction] (like "up" or "to center")
            # express [morphogen id] [amount]
            # wait (do nothing one tick)
            """;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false)
    private SpeciesEntity species;

    private Double fitness;

    @Column(columnDefinition = "text")
    private String genome;

    public SpecimenEntity() {
    }

    public SpecimenEntity(SpeciesEntity species, Double fitness, String genome) {
        this.species = species;
        this.fitness = fitness;
        this.genome = genome;
    }

}
