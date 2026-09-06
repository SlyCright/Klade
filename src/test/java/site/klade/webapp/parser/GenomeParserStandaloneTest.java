package site.klade.webapp.parser;

import org.junit.jupiter.api.Test;
import site.klade.simulation.Genome;
import site.klade.webapp.entity.SpecimenEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class GenomeParserStandaloneTest {

    @Test
    public void testParse() {
        GenomeParser parser = new GenomeParser();

        // Parse the example DSL
        String dslString = SpecimenEntity.EXAMPLE_DNA;
        Genome genome = parser.parse(dslString);
        System.out.println("genome = " + genome);

        assertThat(genome).isNotNull();
        assertThat(genome.getMetaGenes()).isNotNull();
        assertThat(genome.getMorphogens()).isNotEmpty();
        assertThat(genome.getGenes()).isNotEmpty();
    }

    @Test
    public void testSerializeAndRoundTrip() {
        GenomeParser parser = new GenomeParser();

        // Parse the example DSL to get a genome
        String dslString = SpecimenEntity.EXAMPLE_DNA;
        Genome genome = parser.parse(dslString);

        // Serialize back to DSL
        String serialized = parser.serialize(genome);
        System.out.println("serialized = " + serialized);

        assertThat(serialized).isNotNull();
        assertThat(serialized).contains("--- Meta genes");
        assertThat(serialized).contains("--- Morphogens");
        assertThat(serialized).contains("--- Genes");

        // Parse the serialized DSL again (round-trip)
        Genome reparsed = parser.parse(serialized);

        assertThat(reparsed).isNotNull();
        assertThat(reparsed.getMetaGenes()).isNotNull();
        assertThat(reparsed.getMorphogens()).hasSize(genome.getMorphogens().size());
        assertThat(reparsed.getGenes()).hasSize(genome.getGenes().size());
    }

    @Test
    public void testParseEmptyString() {
        GenomeParser parser = new GenomeParser();
        
        try {
            parser.parse("");
            org.junit.jupiter.api.Assertions.fail("Should throw IllegalArgumentException for empty string");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("empty");
        }
    }

    @Test
    public void testParseNullString() {
        GenomeParser parser = new GenomeParser();
        
        try {
            parser.parse(null);
            org.junit.jupiter.api.Assertions.fail("Should throw IllegalArgumentException for null string");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("null");
        }
    }

    @Test
    public void testSerializeNullGenome() {
        GenomeParser parser = new GenomeParser();
        
        try {
            parser.serialize(null);
            org.junit.jupiter.api.Assertions.fail("Should throw IllegalArgumentException for null genome");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("null");
        }
    }
}
