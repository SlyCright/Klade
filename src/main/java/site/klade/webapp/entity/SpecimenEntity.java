package site.klade.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "specimen")
public class SpecimenEntity {

    public static final String EXAMPLE_DNA = """
            --- Meta genes
            Scale: 1.0

            --- Morphogens
            # Morphogen[id], [diffusion ratio], [decay ratio], [spreading conditions]
            Morphogen[1], 0.80, 0.1, everywhere
            Morphogen[2], 0.95, 0.2, up

            --- Genes
            # [condition] [action] [parameters]
            if Mrph[1] < 1.0 become friction_node
            if Mrph[1] < 1.0 lay_segment 30°
            if Mrph[1] < 1.0 express Mrph[1] 2.0
            wait
            wait
            if Mrph[1] > 1.0 become muscle
            if Mrph[1] > 1.0 become rhythm_node
            
            #Possible commands:
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
