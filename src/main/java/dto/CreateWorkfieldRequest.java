package dto;

import lombok.Data;

@Data
public class CreateWorkfieldRequest {
    private String categoryId;
    private String description;
    private int hourlyRate;
}
