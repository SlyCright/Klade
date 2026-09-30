package site.klade.webapp.parser;

import org.junit.jupiter.api.Test;
import site.klade.simulation.DnaParseException;
import site.klade.simulation.Genome;
import site.klade.simulation.Index;
import site.klade.simulation.gene.Gene;
import site.klade.simulation.gene.GeneAction;
import site.klade.simulation.gene.NumberArg;
import site.klade.webapp.entity.SpecimenEntity;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Backend tests for the DNA codec boundary.
 *
 * <p>Parsing itself is covered in the shared library; what is tested here is the part that belongs to the
 * backend — that {@link GenomeParser} delegates correctly, and that the {@code Genome -> String} writer
 * produces canonical text that survives a round trip with <b>real field equality</b>.</p>
 *
 * <p>The round trip is asserted field by field rather than by counts on purpose. The previous reflection
 * based implementation lost the initial angle silently (the DSL name {@code InitialAngle} did not match
 * the field {@code initialAngle}), and a count-only assertion could not see it.</p>
 */
public class GenomeParserTest {

    private final GenomeParser parser = new GenomeParser();

    @Test
    public void parseReadsTheCanonicalExample() {
        Genome genome = parser.parse(SpecimenEntity.EXAMPLE_DNA);

        assertThat(genome).isNotNull();
        assertThat(genome.getMetaGenes()).isNotNull();
        assertThat(genome.getGenes()).isNotEmpty();
        assertThat(genome.getMorphogens()).isNotEmpty();
    }

    /** The sample DNA doubles as documentation, so it must exercise the whole vocabulary. */
    @Test
    public void exampleDnaExercisesTheWholeVocabulary() {
        Genome genome = parser.parse(SpecimenEntity.EXAMPLE_DNA);

        Set<GeneAction> actions = new HashSet<GeneAction>();
        for (Gene gene : genome.getGenes()) {
            actions.add(gene.getAction());
        }
        assertThat(actions).contains(GeneAction.EMPTY, GeneAction.BECOME,
                GeneAction.LAY_SEGMENT, GeneAction.EXPRESS);

        boolean conditional = false;
        boolean unconditional = false;
        for (Gene gene : genome.getGenes()) {
            if (gene.getAction() == GeneAction.EMPTY) {
                continue;
            }
            if (gene.getConditions() == null) {
                unconditional = true;
            } else {
                conditional = true;
            }
        }
        assertThat(conditional).as("a conditional gene").isTrue();
        assertThat(unconditional).as("the unconditional bootstrap gene").isTrue();

        // Nested dotted indices appear on genes...
        assertThat(genome.findGene(Index.parse("1.1"))).as("nested gene index").isNotNull();
        assertThat(genome.findGene(Index.parse("1.2"))).as("nested gene index").isNotNull();

        // ...and both morphogens are referenced, while the nested unreferenced one is dropped.
        assertThat(genome.getMorphogens()).hasSize(2);
        assertThat(genome.referencedMorphogens()).containsExactly(new Index(1), new Index(2));
        assertThat(genome.findMorphogen(Index.parse("2.1"))).isNull();
    }

    /**
     * The three leading empty genes are load-bearing, not decoration: they create the phase offset that
     * stops the growth chain. Without them the specimen is not the viable one traced in plan-spec §6.3.1.
     */
    @Test
    public void exampleDnaBeginsWithThreeEmptyGenes() {
        Genome genome = parser.parse(SpecimenEntity.EXAMPLE_DNA);

        assertThat(genome.getGenes()).hasSizeGreaterThanOrEqualTo(4);
        assertThat(genome.getGenes().get(0).getAction()).isEqualTo(GeneAction.EMPTY);
        assertThat(genome.getGenes().get(1).getAction()).isEqualTo(GeneAction.EMPTY);
        assertThat(genome.getGenes().get(2).getAction()).isEqualTo(GeneAction.EMPTY);
        assertThat(genome.getGenes().get(3).getAction()).isEqualTo(GeneAction.LAY_SEGMENT);
    }

    /** The regression that motivated the registry: the initial angle must actually arrive. */
    @Test
    public void parsePreservesMetaGeneValuesNotJustTheirPresence() {
        Genome genome = parser.parse(SpecimenEntity.EXAMPLE_DNA);

        assertThat(genome.getInitialAngle()).isEqualTo(45f);
        assertThat(genome.getHyperGene()).isEqualTo(0.5f);
    }

    @Test
    public void parseReadsGeneStructureFaithfully() {
        Genome genome = parser.parse(SpecimenEntity.EXAMPLE_DNA);

        // Three empty genes plus the six that build the organism.
        assertThat(genome.getGenes()).hasSize(9);
        Gene laySegment = genome.findGene(new Index(4));
        assertThat(laySegment).isNotNull();
        assertThat(laySegment.getAction()).isEqualTo(GeneAction.LAY_SEGMENT);
        assertThat(laySegment.getConditions()).isNotNull();
        assertThat(laySegment.getArguments()).hasSize(1);

        Gene becomeMuscle = genome.findGene(new Index(8));
        assertThat(becomeMuscle).isNotNull();
        assertThat(becomeMuscle.getArguments()).hasSize(2);
        assertThat(becomeMuscle.toString()).contains("become muscle length 40%");
    }

    /** Morphogens are derived from references, not taken from the section verbatim. */
    @Test
    public void morphogenSetFollowsTheReferences() {
        Genome genome = parser.parse(SpecimenEntity.EXAMPLE_DNA);

        assertThat(genome.referencedMorphogens())
                .containsExactly(new Index(1), new Index(2));
        assertThat(genome.getMorphogens()).hasSize(2);
        assertThat(genome.getMorphogens().get(0).getDiffusionRatio()).isEqualTo(0.8f);
        assertThat(genome.getMorphogens().get(0).getDecayRatio()).isEqualTo(0.1f);
    }

    /** A definition nobody references is dropped; one that is referenced is materialised. */
    @Test
    public void unreferencedMorphogensAreDroppedAndReferencedOnesMaterialised() {
        String dna = "--- Genes\n"
                + "1.    express Morphogen[5] amount 1\n"
                + "--- Morphogens\n"
                + "Morphogen[1]: 0.5, 0.5\n";
        Genome genome = parser.parse(dna);

        assertThat(genome.getMorphogens()).hasSize(1);
        assertThat(genome.getMorphogens().get(0).getId()).isEqualTo(new Index(5));
    }

    @Test
    public void roundTripPreservesEveryMetaGeneValue() {
        Genome genome = parser.parse(SpecimenEntity.EXAMPLE_DNA);
        String serialized = parser.serialize(genome);
        Genome reparsed = parser.parse(serialized);

        assertThat(reparsed.getInitialAngle()).isEqualTo(genome.getInitialAngle());
        assertThat(reparsed.getHyperGene()).isEqualTo(genome.getHyperGene());
        assertThat(reparsed.getGenes()).hasSize(genome.getGenes().size());
        assertThat(reparsed.getMorphogens()).hasSize(genome.getMorphogens().size());
    }

    @Test
    public void serializationIsCanonicalAndStable() {
        Genome genome = parser.parse(SpecimenEntity.EXAMPLE_DNA);
        String once = parser.serialize(genome);
        String twice = parser.serialize(parser.parse(once));

        assertThat(twice).isEqualTo(once);
        assertThat(once).contains("--- Meta genes");
        assertThat(once).contains("--- Genes");
        assertThat(once).contains("--- Morphogens");
        // Meta-genes are emitted by the registry, so their order is fixed rather than JVM-dependent.
        assertThat(once.indexOf("hyperGene")).isLessThan(once.indexOf("initialAngle"));
        // Sections appear in canonical order: genes before morphogens.
        assertThat(once.indexOf("--- Genes")).isLessThan(once.indexOf("--- Morphogens"));
    }

    @Test
    public void emptyGeneSurvivesTheRoundTrip() {
        String dna = "--- Genes\n"
                + "1.    become muscle length 40%\n"
                + "2.\n"
                + "3.    express Morphogen[1] amount 1\n";
        Genome genome = parser.parse(dna);
        assertThat(genome.getGenes().get(1).getAction()).isEqualTo(GeneAction.EMPTY);

        Genome reparsed = parser.parse(parser.serialize(genome));
        assertThat(reparsed.getGenes().get(1).getAction()).isEqualTo(GeneAction.EMPTY);
        assertThat(reparsed.getGenes()).hasSize(3);
    }

    @Test
    public void rejectsNullOrEmptyInput() {
        assertThatThrownBy(() -> parser.parse(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    public void rejectsNullGenomeOnSerialize() {
        assertThatThrownBy(() -> parser.serialize(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    public void malformedLineIsRejectedWithItsLineNumber() {
        String dna = "--- Genes\n"
                + "1.    become muscle length 40%\n"
                + "2.    this is not a command\n";
        assertThatThrownBy(() -> parser.parse(dna))
                .isInstanceOf(DnaParseException.class)
                .hasMessageContaining("line 3")
                .hasMessageContaining("this is not a command");
    }

    @Test
    public void writerAndParserAgreeOnAHandWrittenGenome() {
        String dna = "--- Genes\n"
                + "1.    become rhythm_node period 40\n"
                + "2.    lay_segment 30°\n";
        Genome genome = parser.parse(dna);
        Genome reparsed = parser.parse(parser.serialize(genome));

        assertThat(reparsed.getGenes()).hasSize(2);
        assertThat(reparsed.getGenes().get(1).getArguments()).containsExactly(new NumberArg(30f));
    }
}
