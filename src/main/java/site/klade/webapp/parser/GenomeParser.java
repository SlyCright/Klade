package site.klade.webapp.parser;

import site.klade.simulation.*;

import java.lang.reflect.Field;
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
        MetaGenes metaGenes = parseMetaGenes(metaGeneLines);
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
        sb.append(serializeMetaGenes(genome.getMetaGenes())).append("\n");
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

    private MetaGenes parseMetaGenes(List<String> lines) {
        MetaGenes metaGenes = new MetaGenes();
        for (String line : lines) {
            // Format: FieldName: value (Type)
            // Example: InitialAngle: 45.0 (Float)
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String fieldName = parts[0].trim();
                String valuePart = parts[1].trim();
                
                // Remove type annotation if present
                String value = valuePart;
                int typeIndex = valuePart.indexOf("(");
                if (typeIndex != -1) {
                    value = valuePart.substring(0, typeIndex).trim();
                }
                
                try {
                    Field field = MetaGenes.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();
                    
                    Object parsedValue = parseValue(value, fieldType);
                    if (parsedValue != null) {
                        field.set(metaGenes, parsedValue);
                    }
                } catch (NoSuchFieldException e) {
                    // Skip unknown fields
                    continue;
                } catch (Exception e) {
                    // Skip malformed lines
                    continue;
                }
            }
        }
        return metaGenes;
    }
    
    private Object parseValue(String value, Class<?> type) {
        try {
            String normalized = value.replace(',', '.');
            if (type == float.class || type == Float.class) {
                return Float.parseFloat(normalized);
            } else if (type == double.class || type == Double.class) {
                return Double.parseDouble(normalized);
            } else if (type == int.class || type == Integer.class) {
                return Integer.parseInt(normalized);
            } else if (type == boolean.class || type == Boolean.class) {
                return Boolean.parseBoolean(normalized);
            } else if (type == String.class) {
                return value;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
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
            // or: become friction_node (without condition)
            // or: wait (action with no parameters)
            try {
                String lineToParse = line;
                String condition = "";
                
                // Check for conditional "if" prefix
                if (line.startsWith("if ")) {
                    lineToParse = line.substring(3).trim(); // Remove "if "
                }
                
                // Parse action and parameters
                Gene gene = parseGeneLine(lineToParse, condition);
                if (gene != null) {
                    genes.add(gene);
                }
            } catch (Exception e) {
                // Skip malformed lines
                continue;
            }
        }
        return genes;
    }
    
    private Gene parseGeneLine(String line, String condition) {
        // Get all possible actions from enum dynamically
        GeneAction[] possibleActions = GeneAction.values();
        
        // Find which action appears in the line
        for (GeneAction action : possibleActions) {
            String actionName = action.name().toLowerCase();
            int actionIndex = line.toLowerCase().indexOf(actionName);
            
            if (actionIndex != -1) {
                // Extract parameters after the action
                String parameters = line.substring(actionIndex + actionName.length()).trim();
                
                // For WAIT action, parameters should be empty
                if (action == GeneAction.WAIT) {
                    parameters = "";
                }
                
                return new Gene(condition, action, parameters);
            }
        }
        
        return null; // No action found
    }

    private String serializeMetaGenes(MetaGenes metaGenes) {
        StringBuilder sb = new StringBuilder();
        Field[] fields = MetaGenes.class.getDeclaredFields();
        
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = field.get(metaGenes);
                Class<?> fieldType = field.getType();
                
                if (value != null) {
                    String typeAnnotation = getTypeAnnotation(fieldType);
                    sb.append(fieldName).append(": ").append(value).append(" ").append(typeAnnotation).append("\n");
                }
            } catch (IllegalAccessException e) {
                // Skip inaccessible fields
                continue;
            }
        }
        
        return sb.toString();
    }
    
    private String getTypeAnnotation(Class<?> type) {
        if (type == float.class || type == Float.class) {
            return "(Float)";
        } else if (type == double.class || type == Double.class) {
            return "(Double)";
        } else if (type == int.class || type == Integer.class) {
            return "(Integer)";
        } else if (type == boolean.class || type == Boolean.class) {
            return "(Boolean)";
        } else if (type == String.class) {
            return "(String)";
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
        String actionName = gene.getAction().name().toLowerCase();
        
        // For WAIT action, just output the action name
        if (gene.getAction() == GeneAction.WAIT) {
            return actionName;
        }
        
        // For other actions, include parameters
        if (gene.getCondition() == null || gene.getCondition().isEmpty()) {
            // Unconditional gene - no "if" prefix
            return String.format("%s %s", actionName, gene.getParameters());
        } else {
            // Conditional gene with "if" prefix
            return String.format("if %s %s %s", gene.getCondition(), actionName, gene.getParameters());
        }
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
