package site.klade.webapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class GenomesDto {

    private List<GenomeDto> genomes;

    // Default constructor for Jackson
    public GenomesDto() {
        this.genomes = new ArrayList<>();
    }

    public GenomesDto(List<GenomeDto> genomes) {
        this.genomes = genomes;
    }
}
