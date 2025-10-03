package dto;

import lombok.Data;

@Data
public class EditReviewDto {
    public String id;
    public double rating;
    public String details;
    public String authorId;
    public String revieweeId;
}
