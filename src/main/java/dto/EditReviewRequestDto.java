package dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class EditReviewRequestDto {
    public String revieweeId;
    public String reviewId;
    public BigDecimal rating;
    public String details;
}
