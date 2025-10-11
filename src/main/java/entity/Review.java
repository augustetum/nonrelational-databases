package entity;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Review {
    private ReviewId id;
    private BigDecimal rating;
    private String details;
    private String authorId;
}
