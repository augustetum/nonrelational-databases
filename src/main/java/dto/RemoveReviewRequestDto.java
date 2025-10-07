package dto;

import lombok.Data;

@Data
public class RemoveReviewRequestDto {
    public String revieweeId;
    public String reviewId;
}
