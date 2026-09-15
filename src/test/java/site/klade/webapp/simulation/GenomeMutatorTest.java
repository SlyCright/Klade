package site.klade.webapp.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import site.klade.simulation.Genome;
import site.klade.simulation.Morphogen;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GenomeMutatorTest {

    private Genome genomeWithMorphogens(int count) {
        Genome genome = new Genome(0.5f);
        for (int i = 0; i < count; i++) {
            genome.getMorphogens().add(new Morphogen(i + 1, 0.5f, 0.1f, "everywhere"));
        }
        return genome;
    }

    @DisplayName("Given any genome, when mutated with rank 0.0, then morphogen list is frozen")
    @Test
    void givenGenome_whenMutatedWithZeroRank_thenMorphogenListFrozen() {
        GenomeMutator mutator = new GenomeMutator();
        Genome genome = genomeWithMorphogens(3);
        // When
        Genome mutated = mutator.mutate(genome, 0.0);
        // Then: same size, same ids, same ratios (drift/replace never fire at factor 0)
        List<Morphogen> original = genome.getMorphogens();
        List<Morphogen> result = mutated.getMorphogens();
        assertThat(result).hasSameSizeAs(original);
        for (int i = 0; i < original.size(); i++) {
            assertThat(result.get(i).getId()).isEqualTo(original.get(i).getId());
            assertThat(result.get(i).getDiffusionRatio()).isEqualTo(original.get(i).getDiffusionRatio());
            assertThat(result.get(i).getDecayRatio()).isEqualTo(original.get(i).getDecayRatio());
        }
        assertThat(result).isNotSameAs(original);
    }

    @DisplayName("Given genome with a single morphogen, when mutated at maximum rank many times, then at least one morphogen always survives and ids stay unique")
    @Test
    void givenSingleMorphogen_whenMutatedAtMaxRank_thenListNeverEmptiesAndIdsUnique() {
        GenomeMutator mutator = new GenomeMutator();
        for (int trial = 0; trial < 500; trial++) {
            Genome genome = genomeWithMorphogens(1);
            // When
            Genome mutated = mutator.mutate(genome, 1.0);
            // Then
            List<Morphogen> morphogens = mutated.getMorphogens();
            assertThat(morphogens).isNotEmpty();
            Set<Integer> ids = new HashSet<>();
            morphogens.forEach(m -> ids.add(m.getId()));
            assertThat(ids).hasSize(morphogens.size());
            morphogens.forEach(m -> assertThat(m.getId()).isPositive());
        }
    }

    @DisplayName("Given genome with two morphogens, when mutated at maximum rank many times, then structural changes occur and ids stay unique")
    @Test
    void givenTwoMorphogens_whenMutatedAtMaxRank_thenStructuralChangesOccur() {
        GenomeMutator mutator = new GenomeMutator();
        boolean sizeChanged = false;
        for (int trial = 0; trial < 200 && !sizeChanged; trial++) {
            Genome mutated = mutator.mutate(genomeWithMorphogens(2), 1.0);
            List<Morphogen> morphogens = mutated.getMorphogens();
            assertThat(morphogens).isNotEmpty();
            Set<Integer> ids = new HashSet<>();
            morphogens.forEach(m -> ids.add(m.getId()));
            assertThat(ids).hasSize(morphogens.size());
            sizeChanged = morphogens.size() != 2;
        }
        // At max rank deletion chance is 1.0 and addition chance is 1.0,
        // so some divergence in list size must happen at least once
        assertThat(sizeChanged).isTrue();
    }

    @DisplayName("Given genome with one morphogen, when mutated at low rank many times, then the list may only grow (addition more probable than deletion)")
    @Test
    void givenOneMorphogen_whenMutatedAtLowRank_thenListOnlyGrows() {
        GenomeMutator mutator = new GenomeMutator();
        for (int trial = 0; trial < 500; trial++) {
            Genome mutated = mutator.mutate(genomeWithMorphogens(1), 0.1);
            // Deletion requires size > 1; with a single morphogen the list can never shrink
            assertThat(mutated.getMorphogens().size()).isGreaterThanOrEqualTo(1);
            assertThat(mutated.getMorphogens().size()).isLessThanOrEqualTo(2);
        }
    }
}
