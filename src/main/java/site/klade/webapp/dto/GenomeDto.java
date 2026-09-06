package site.klade.webapp.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenomeDto {

    private float startX;

    private float startY;

    private float impulseX;

    private float impulseY;

    private float fitness;

    private int speciesIndex;

    // Default constructor for Jackson
    public GenomeDto() {
    }

    public GenomeDto(float startX, float startY, float impulseX, float impulseY, float fitness, int speciesIndex) {
        this.startX = startX;
        this.startY = startY;
        this.impulseX = impulseX;
        this.impulseY = impulseY;
        this.fitness = fitness;
        this.speciesIndex = speciesIndex;
    }
}