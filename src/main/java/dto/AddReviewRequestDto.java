package dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AddReviewRequestDto {
    public String revieweeId;
    public BigDecimal rating;
    public String details;
}