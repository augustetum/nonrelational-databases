package dto;

import lombok.Data;

@Data
public class RemoveReviewRequestDto {
    public String reviewId;
    public String revieweeId;
}
