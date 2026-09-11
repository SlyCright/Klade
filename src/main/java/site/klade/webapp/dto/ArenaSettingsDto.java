package site.klade.webapp.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ArenaSettingsDto {

    private float initialDistance;

    private float frictionFactor;

    private float nodeSize;

    private float repulsionFactor;

    private int maxTicks;

    // Default constructor for Jackson
    public ArenaSettingsDto() {
    }

    public ArenaSettingsDto(
            float initialDistance,
            float frictionFactor,
            float nodeSize,
            float repulsionFactor,
            int maxTicks
    ) {
        this.initialDistance = initialDistance;
        this.frictionFactor = frictionFactor;
        this.nodeSize = nodeSize;
        this.repulsionFactor = repulsionFactor;
        this.maxTicks = maxTicks;
    }

}
