package dto;

import lombok.Data;

@Data
public class CreateWorkfieldRequestDto {
    private String category;
    private String description;
    private int hourlyRate;
}
