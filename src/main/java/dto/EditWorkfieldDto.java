package dto;

import entity.WorkfieldCategory;
import lombok.Data;

@Data
public class EditWorkfieldDto {
    private WorkfieldCategory category;
    private String description;
    private int hourlyRate;
}
