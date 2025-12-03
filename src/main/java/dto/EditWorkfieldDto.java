package dto;

import lombok.Data;

@Data
public class EditWorkfieldDto {
    private String categoryId;
    private String description;
    private int hourlyRate;
}
