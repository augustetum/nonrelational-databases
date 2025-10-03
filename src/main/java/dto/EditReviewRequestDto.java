package dto;

import lombok.Data;

@Data
public class EditReviewRequestDto {
    public String id;
    public double rating;
    public String details;
    public String revieweeId;
}
