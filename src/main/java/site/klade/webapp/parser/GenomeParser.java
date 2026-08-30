package site.klade.webapp.parser;

import site.klade.simulation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parser for converting between DSL string format and Genome objects.
 * Handles the genome DSL format with sections for Meta genes, Morphogens, and Genes.
 */
public class GenomeParser {

    /**
     * Parses a DSL string into a Genome object.
     *
     * @param dslString the DSL string to parse
     * @return the parsed Genome object
     * @throws IllegalArgumentException if the DSL string is invalid
     */
    public Genome parse(String dslString) {
        if (dslString == null || dslString.trim().isEmpty()) {
            throw new IllegalArgumentException("DSL string cannot be null or empty");
        }

        String[] lines = dslString.split("\n");
        List<String> metaGeneLines = new ArrayList<>();
        List<String> morphogenLines = new ArrayList<>();
        List<String> geneLines = new ArrayList<>();

        Section currentSection = Section.NONE;

        for (String line : lines) {
            String trimmedLine = line.trim();

            // Skip empty lines and comments
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                continue;
            }

            // Check for section headers
            if (trimmedLine.equals("--- Meta genes")) {
                currentSection = Section.META_GENES;
                continue;
            } else if (trimmedLine.equals("--- Morphogens")) {
                currentSection = Section.MORPHOGENS;
                continue;
            } else if (trimmedLine.equals("--- Genes")) {
                currentSection = Section.GENES;
                continue;
            }

            // Add line to current section
            switch (currentSection) {
                case META_GENES -> metaGeneLines.add(trimmedLine);
                case MORPHOGENS -> morphogenLines.add(trimmedLine);
                case GENES -> geneLines.add(trimmedLine);
                case NONE -> {
                } // Lines before first section are ignored
            }
        }

        // Parse each section
        List<MetaGene> metaGenes = parseMetaGenes(metaGeneLines);
        List<Morphogen> morphogens = parseMorphogens(morphogenLines);
        List<Gene> genes = parseGenes(geneLines);

        return new Genome(metaGenes, morphogens, genes);
    }

    /**
     * Serializes a Genome object into DSL string format.
     *
     * @param genome the Genome object to serialize
     * @return the DSL string representation
     */
    public String serialize(Genome genome) {
        if (genome == null) {
            throw new IllegalArgumentException("Genome cannot be null");
        }

        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        // Serialize Meta genes section
        sb.append("--- Meta genes\n");
        for (MetaGene metaGene : genome.getMetaGenes()) {
            sb.append(serializeMetaGene(metaGene)).append("\n");
        }
        sb.append("\n");

        // Serialize Morphogens section
        sb.append("--- Morphogens\n");
        for (Morphogen morphogen : genome.getMorphogens()) {
            sb.append(serializeMorphogen(morphogen)).append("\n");
        }
        sb.append("\n");

        // Serialize Genes section
        sb.append("--- Genes\n");
        for (Gene gene : genome.getGenes()) {
            sb.append(serializeGene(gene)).append("\n");
        }

        return sb.toString();
    }

    private List<MetaGene> parseMetaGenes(List<String> lines) {
        List<MetaGene> metaGenes = new ArrayList<>();
        for (String line : lines) {
            // Format: Scale: 1.0
            if (line.startsWith("Scale:")) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    float parameter = parseFloat(parts[1].trim());
                    metaGenes.add(new MetaGene(MetaGeneType.SCALE, parameter));
                }
            }
        }
        return metaGenes;
    }

    private List<Morphogen> parseMorphogens(List<String> lines) {
        List<Morphogen> morphogens = new ArrayList<>();
        for (String line : lines) {
            // Format: Morphogen[1], 0.80, 0.1, everywhere
            try {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    // Parse id from Morphogen[id]
                    String idPart = parts[0].trim();
                    int id = Integer.parseInt(idPart.substring(idPart.indexOf('[') + 1, idPart.indexOf(']')));

                    float diffusionRatio = parseFloat(parts[1].trim());
                    float decayRatio = parseFloat(parts[2].trim());
                    String spreadingConditions = parts[3].trim();

                    morphogens.add(new Morphogen(id, diffusionRatio, decayRatio, spreadingConditions));
                }
            } catch (Exception e) {
                // Skip malformed lines
                continue;
            }
        }
        return morphogens;
    }

    private List<Gene> parseGenes(List<String> lines) {
        List<Gene> genes = new ArrayList<>();
        for (String line : lines) {
            // Format: if Mrph[1] < 1.0 become friction_node
            // or: wait
            try {
                if (line.equals("wait")) {
                    genes.add(new Gene("", GeneAction.WAIT, ""));
                } else if (line.startsWith("if ")) {
                    // Parse conditional gene
                    String conditionPart = line.substring(3).trim(); // Remove "if "

                    // Find the action keyword
                    String[] possibleActions = {"become", "lay_segment", "express"};
                    String foundAction = null;
                    int actionIndex = -1;

                    for (String action : possibleActions) {
                        int idx = conditionPart.indexOf(action);
                        if (idx != -1) {
                            foundAction = action;
                            actionIndex = idx;
                            break;
                        }
                    }

                    if (foundAction != null && actionIndex > 0) {
                        String condition = conditionPart.substring(0, actionIndex).trim();
                        String rest = conditionPart.substring(actionIndex + foundAction.length()).trim();

                        // Normalize action to enum
                        GeneAction normalizedAction = normalizeAction(foundAction);
                        genes.add(new Gene(condition, normalizedAction, rest));
                    }
                }
            } catch (Exception e) {
                // Skip malformed lines
                continue;
            }
        }
        return genes;
    }

    private String serializeMetaGene(MetaGene metaGene) {
        if (metaGene.getType() == MetaGeneType.SCALE) {
            return "Scale: " + metaGene.getParameter();
        }
        return "";
    }

    private String serializeMorphogen(Morphogen morphogen) {
        return String.format(Locale.US, "Morphogen[%d], %.2f, %.1f, %s",
                morphogen.getId(),
                morphogen.getDiffusionRatio(),
                morphogen.getDecayRatio(),
                morphogen.getSpreadingConditions());
    }

    private String serializeGene(Gene gene) {
        if (gene.getAction() == GeneAction.WAIT) {
            return "wait";
        } else {
            return String.format("if %s %s %s",
                    gene.getCondition(),
                    gene.getAction().toString().toLowerCase(),
                    gene.getParameters());
        }
    }

    private GeneAction normalizeAction(String action) {
        return switch (action.toLowerCase()) {
            case "become" -> GeneAction.BECOME;
            case "lay_segment" -> GeneAction.LAY_SEGMENT;
            case "express" -> GeneAction.EXPRESS;
            case "wait" -> GeneAction.WAIT;
            default -> GeneAction.valueOf(action.toUpperCase());
        };
    }

    /**
     * Parses a float string in a locale-independent way, handling both comma and period decimal separators.
     */
    private float parseFloat(String value) {
        // Replace comma with period for parsing
        String normalized = value.replace(',', '.');
        return Float.parseFloat(normalized);
    }

    private enum Section {
        NONE, META_GENES, MORPHOGENS, GENES
    }
}
