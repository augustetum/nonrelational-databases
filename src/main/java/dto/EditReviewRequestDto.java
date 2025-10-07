package dto;

import lombok.Data;

@Data
public class EditReviewRequestDto {
    public String revieweeId;
    public String reviewId;
    public double rating;
    public String details;
}
