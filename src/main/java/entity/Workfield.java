package entity;

import lombok.Data;

@Data
public class Workfield {
    private String id;
    private String categoryId;
    private String description;
    private int hourlyRate;
}
