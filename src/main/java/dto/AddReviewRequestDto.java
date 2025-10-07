package dto;

import lombok.Data;

@Data
public class AddReviewRequestDto {
    public String revieweeId;
    public double rating;
    public String details;
}