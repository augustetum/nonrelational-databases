package dto;

import lombok.Data;

@Data
public class RemoveReviewDto {
    public String reviewId;
    public String authorId;
    public boolean isClient;
    public String revieweeId;
}
