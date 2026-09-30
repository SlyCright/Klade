package site.klade.webapp.parser;

import site.klade.simulation.Genome;
import site.klade.simulation.Morphogen;
import site.klade.simulation.gene.Gene;

/**
 * Writes a {@link Genome} back to DNA text. The {@code Genome -> String} direction, which is
 * persistence-only and therefore belongs to the backend rather than the shared simulation library.
 *
 * <p><b>Why this direction is not shared.</b> The client never serialises a genome; only
 * {@code GenerationPersistenceService} does, when it stores a specimen. Keeping the writer here means the
 * shared library needs only the parsing direction, which the client does need once ontogenesis runs
 * there. See the plan's §2.3.</p>
 *
 * <p><b>No reflection.</b> Meta-genes are emitted by iterating {@link MetaGeneRegistry}, so field order is
 * explicit and stable. The previous implementation used {@code getDeclaredFields()}, whose order is not
 * guaranteed by the JVM — which would make a stored genome's canonical form depend on the runtime, and
 * would undermine the project's determinism requirement.</p>
 *
 * <p><b>Canonical form.</b> Sections are emitted in the order {@code Meta genes, Genes, Morphogens}, the
 * explanatory comment header is always included so any copy of a DNA is self-describing, and genes and
 * morphogens are emitted in dotted-index order.</p>
 */
public class GenomeWriter {

    /**
     * The comment header emitted with every genome.
     *
     * <p>Kept here rather than generated, because it documents the format for whoever reads the DNA later —
     * including someone who has never seen the project, which is the point of the plain-text format.</p>
     */
    private static final String GENE_HEADER =
            "# <index> [if <condition>] <action> [arguments]\n"
            + "# An index alone on its line is an empty gene: the element does nothing this tick.\n"
            + "# Every numeric argument is either followed by its unit (30°) or introduced by its label (length 40%).\n"
            + "# An element type may take its own function arguments: rhythm_node period <ticks>, muscle length <percent>.";

    private static final String MORPHOGEN_HEADER =
            "# Morphogen[<index>]: <diffusion ratio>, <decay ratio>\n"
            + "# The set of morphogens is derived from the references in the Genes section.";

    private static final String META_HEADER = "# <name>: <value> (<type>)";

    /**
     * Serialises a genome into canonical DNA text.
     *
     * <p>The writer only <b>assembles sections</b>: each piece of the genome already knows its own
     * canonical spelling ({@link site.klade.simulation.gene.Gene#toString()}, {@link Morphogen#toString()},
     * {@link site.klade.simulation.MetaGenes#toString()}), so the format is defined in exactly one place
     * per line kind. Only the section markers and the explanatory comment headers — which are document
     * structure rather than genome content — live here.</p>
     */
    public String write(Genome genome) {
        if (genome == null) throw new IllegalArgumentException("Genome cannot be null");
        StringBuilder out = new StringBuilder();
        out.append("--- Meta genes\n").append(META_HEADER).append('\n')
                .append(genome.getMetaGenes()).append('\n');
        out.append("--- Genes\n").append(GENE_HEADER).append("\n\n");
        for (Gene gene : genome.getGenes()) {
            out.append(gene).append('\n');
        }
        out.append('\n');
        out.append("--- Morphogens\n").append(MORPHOGEN_HEADER).append('\n');
        for (Morphogen morphogen : genome.getMorphogens()) {
            out.append(morphogen).append('\n');
        }
        return out.toString();
    }
}
