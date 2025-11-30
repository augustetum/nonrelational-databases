package dto;

import lombok.Data;

@Data
public class CreateWorkfieldCategoryRequest {
    String parentId;
    String name;
}
