package dto;

import lombok.Data;

@Data
public class AddReviewDto {
    public double rating;
    public String details;
    public String authorId;
    public String revieweeId;
}