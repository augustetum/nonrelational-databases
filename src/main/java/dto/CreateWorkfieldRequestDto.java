package dto;

import lombok.Data;

@Data
public class CreateWorkfieldRequestDto {
    private String categoryId;
    private String description;
    private int hourlyRate;
}
