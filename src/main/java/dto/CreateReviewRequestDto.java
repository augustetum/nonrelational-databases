package dto;

import lombok.Data;

@Data
public class CreateReviewRequestDto {
    public double rating;
    public String details;
    public String revieweeId;
}