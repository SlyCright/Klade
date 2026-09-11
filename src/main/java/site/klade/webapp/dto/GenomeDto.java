package site.klade.webapp.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenomeDto {

    private float initialAngle;

    private float fitness;

    private int speciesIndex;

    private String genomeDsl;

    // Default constructor for Jackson
    public GenomeDto() {
    }

    public GenomeDto(float initialAngle, float fitness, int speciesIndex, String genomeDsl) {
        this.initialAngle = initialAngle;
        this.fitness = fitness;
        this.speciesIndex = speciesIndex;
        this.genomeDsl = genomeDsl;
    }
}