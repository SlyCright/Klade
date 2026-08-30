package site.klade.webapp.parser;

import org.junit.jupiter.api.Test;
import site.klade.simulation.Genome;
import site.klade.webapp.entity.SpecimenEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class GenomeParserTest {

    @Test
    public void testParseAndSerialize() {
        GenomeParser parser = new GenomeParser();
        
        // Parse the example DSL
        String dslString = SpecimenEntity.EXAMPLE_DNA;
        Genome genome = parser.parse(dslString);
        
        System.out.println("Original genome - MetaGenes: " + genome.getMetaGenes().size());
        System.out.println("Original genome - Morphogens: " + genome.getMorphogens().size());
        System.out.println("Original genome - Genes: " + genome.getGenes().size());
        
        assertThat(genome).isNotNull();
        assertThat(genome.getMetaGenes()).isNotEmpty();
        assertThat(genome.getMorphogens()).isNotEmpty();
        assertThat(genome.getGenes()).isNotEmpty();
        
        // Serialize back to DSL
        String serialized = parser.serialize(genome);
        
        System.out.println("Serialized DSL:\n" + serialized);
        
        assertThat(serialized).isNotNull();
        assertThat(serialized).contains("--- Meta genes");
        assertThat(serialized).contains("--- Morphogens");
        assertThat(serialized).contains("--- Genes");
        
        // Parse the serialized DSL again
        Genome reparsed = parser.parse(serialized);
        
        System.out.println("Reparsed genome - MetaGenes: " + reparsed.getMetaGenes().size());
        System.out.println("Reparsed genome - Morphogens: " + reparsed.getMorphogens().size());
        System.out.println("Reparsed genome - Genes: " + reparsed.getGenes().size());
        
        assertThat(reparsed).isNotNull();
        assertThat(reparsed.getMetaGenes()).hasSize(genome.getMetaGenes().size());
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
