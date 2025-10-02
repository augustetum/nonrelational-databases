package dto;

import lombok.Data;

@Data
public class AddReviewRequestDto {
    public double rating;
    public String details;
    public String revieweeId;
}