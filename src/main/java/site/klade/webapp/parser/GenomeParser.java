package site.klade.webapp.parser;

import org.springframework.stereotype.Component;
import site.klade.simulation.Genome;
import site.klade.simulation.GenomeCodec;

/**
 * Backend-facing DNA parser/serialiser. Holds no logic: parsing lives in the shared
 * {@link GenomeCodec} (which the client also needs) and serialisation in {@link GenomeWriter} (which is
 * persistence-only).
 *
 * <p>This wrapper survives because existing services inject it, and because it keeps the public name
 * stable while the implementation moved. Its previous private helpers were deleted along with the
 * reflection-based meta-gene handling they depended on — see {@code MetaGeneRegistry} for why that
 * mattered.</p>
 *
 * <p>TODO(mvp-deferred): `spreadingConditions` is not part of the morphogen line yet; see
 * {@code site.klade.simulation.Morphogen}.</p>
 */
@Component
public class GenomeParser {

    private final GenomeWriter writer = new GenomeWriter();

    /**
     * Parses DNA text into a genome.
     *
     * @throws IllegalArgumentException if the text is null or blank
     * @throws site.klade.simulation.DnaParseException if any line is malformed
     */
    public Genome parse(String dsl) {
        return GenomeCodec.parse(dsl);
    }

    /**
     * Serialises a genome into canonical DNA text.
     *
     * @throws IllegalArgumentException if the genome is null
     */
    public String serialize(Genome genome) {
        return writer.write(genome);
    }
}
