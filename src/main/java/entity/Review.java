package entity;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Review {
    public ReviewId id;
    public BigDecimal rating;
    public String details;
    public String authorId;
}
