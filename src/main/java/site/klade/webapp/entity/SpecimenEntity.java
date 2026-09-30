package site.klade.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "specimen")
public class SpecimenEntity {

    /**
     * The reader-facing sample DNA: a <b>real, viable</b> minimal specimen, and the exact program traced
     * tick by tick in the plan-spec, §6.3.1.
     *
     * <p>It doubles as documentation, so it shows the whole vocabulary rather than a fragment: all three
     * actions, conditional genes, empty genes (the "wait"), nested dotted indices on both genes and
     * morphogens, and two genuinely referenced morphogens.</p>
     *
     * <p><b>Why the three leading empty genes matter.</b> Every element of a specimen reads the <i>same</i>
     * DNA, each with its own program counter, and a newly created element starts at gene 1 on its next
     * tick. The idle prefix buys the phase offset that makes the specimen viable: node 1 waits three ticks,
     * lays a segment, and then emits an inhibitor ({@code Morphogen[2]}). By the time the newly created
     * node 2 reaches the same {@code lay_segment} gene, the inhibitor has diffused to it, its
     * {@code Morphogen[2] < 1.0} guard fails, and growth stops. Without the prefix both nodes meet
     * {@code lay_segment} at the same relative moment, nothing halts the chain, and the organism never
     * settles into its three-element body.</p>
     *
     * <p>The later genes do the differentiation: an unconditional {@code express Morphogen[1]} acts as a
     * bootstrap (a conditioned one would never fire if the morphogen had not arrived yet), and the two
     * {@code Morphogen[1] > 1.0} guards then turn the segment into a muscle and the far node into a
     * friction node. Gene order is the phase structure, which is why the empties sit at the front rather
     * than anywhere else.</p>
     *
     * <p>{@code Morphogen[2.1]} is deliberately <b>unreferenced</b>: it demonstrates a nested morphogen
     * index and the derive-and-drop rule at once, since the definition set follows the references rather
     * than this section.</p>
     */
    public static final String EXAMPLE_DNA = """
            --- Meta genes
            # <name>: <value> (<type>)
            hyperGene: 0.5 (Float)
            initialAngle: 45 (Float)

            --- Genes
            # <index> [if <condition>] <action> [arguments]
            # An index alone on its line is an empty gene: the element does nothing this tick.
            # Every numeric argument is either followed by its unit (30°) or introduced by its label (length 40%).
            # An element type may take its own function arguments: rhythm_node period <ticks>, muscle length <percent>.

            1.        # an empty gene: the element waits one tick
            1.1.      # a nested index, also empty
            1.2.      # a third idle tick — together these give later elements their phase offset

            4.        if Morphogen[2] < 1.0 lay_segment 0°
            5.        if Morphogen[2] < 1.0 become rhythm_node period 40
            6.        if Morphogen[2] < 1.0 express Morphogen[1] amount 2
            7.        express Morphogen[1] amount 2
            8.        if Morphogen[1] > 1.0 become muscle length 40%
            9.        if Morphogen[1] > 1.0 become friction_node

            --- Morphogens
            # Morphogen[<index>]: <diffusion ratio>, <decay ratio>
            # The set of morphogens is derived from the references in the Genes section,
            # so an unreferenced definition such as Morphogen[2.1] below is simply dropped.
            Morphogen[1]: 0.8, 0.1
            Morphogen[2]: 0.95, 0.2
            Morphogen[2.1]: 0.7, 0.15
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
