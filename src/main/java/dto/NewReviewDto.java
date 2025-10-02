package dto;

import lombok.Data;

@Data
public class NewReviewDto {
    public double rating;
    public String details;
    public String authorId;
    public String revieweeId;
}