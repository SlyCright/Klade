package site.klade.webapp.evolution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import site.klade.simulation.Genome;
import site.klade.simulation.Index;
import site.klade.simulation.Morphogen;
import site.klade.simulation.gene.Gene;
import site.klade.simulation.gene.GeneAction;
import site.klade.simulation.gene.GeneSpecs;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the mutation pipeline at its current stage.
 *
 * <p>The previous version of this suite asserted the <i>probability-based</i> structural morphogen
 * mutation ({@code p_deletion} / {@code p_addition}), which the plan removes: a morphogen definition now
 * exists exactly while something references it, so add and delete are consequences of reference mutation
 * rather than operations of their own. Those assertions are replaced by the invariant that actually holds
 * at every stage — the morphogen set always matches the genome's references, and mutation never produces
 * an invalid gene.</p>
 */
public class GenomeMutatorTest {

    private static final double DELETION_BIAS = 0.05;

    private Genome genomeWithGenes(int geneCount) {
        Genome genome = new Genome(0.5f);
        for (int i = 0; i < geneCount; i++) {
            genome.getGenes().add(new Gene(new Index(i + 1), null, GeneAction.EMPTY,
                    java.util.Collections.<site.klade.simulation.gene.GeneArg>emptyList()));
        }
        return genome;
    }

    @DisplayName("Given any genome, when mutated with rank 0.0, then morphogens stay in step with references")
    @Test
    void givenGenome_whenMutatedWithZeroRank_thenMorphogensMatchReferences() {
        GenomeMutator mutator = new GenomeMutator(DELETION_BIAS);
        Genome genome = genomeWithGenes(3);
        genome.getMorphogens().add(new Morphogen(new Index(1), 0.5f, 0.1f));
        genome.deriveMorphogens();

        Genome mutated = mutator.mutate(genome, 0.0);

        assertThat(mutated.getMorphogens()).isNotSameAs(genome.getMorphogens());
        assertThat(idsOf(mutated)).isEqualTo(mutated.referencedMorphogens());
    }

    @DisplayName("Given any genome, when mutated, every gene remains valid for its action")
    @Test
    void givenGenome_whenMutated_thenEveryGeneIsValid() {
        GenomeMutator mutator = new GenomeMutator(DELETION_BIAS);
        for (int trial = 0; trial < 50; trial++) {
            Genome mutated = mutator.mutate(genomeWithGenes(5), 1.0);
            for (Gene gene : mutated.getGenes()) {
                // Throws if the arguments contradict the action's declared signature.
                GeneSpecs.validate(gene.getAction(), gene.getArguments());
            }
        }
    }

    @DisplayName("Given any genome, when mutated, the morphogen set equals the reference set")
    @Test
    void givenGenome_whenMutated_thenMorphogenSetEqualsReferences() {
        GenomeMutator mutator = new GenomeMutator(DELETION_BIAS);
        for (int trial = 0; trial < 100; trial++) {
            Genome mutated = mutator.mutate(genomeWithGenes(4), 0.7);
            assertThat(idsOf(mutated)).isEqualTo(mutated.referencedMorphogens());
        }
    }

    private static Set<Index> idsOf(Genome genome) {
        Set<Index> ids = new TreeSet<Index>();
        List<Morphogen> morphogens = genome.getMorphogens();
        for (Morphogen morphogen : morphogens) {
            ids.add(morphogen.getId());
        }
        // A TreeSet also proves there are no duplicate ids.
        assertThat(ids).hasSize(morphogens.size());
        return ids;
    }
}
