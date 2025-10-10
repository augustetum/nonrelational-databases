package dto;

import entity.WorkfieldCategory;
import lombok.Data;

@Data
public class WorkfieldDto {
    private String id;
    private WorkfieldCategory category;
    private String description;
    private int hourlyRate;
}
