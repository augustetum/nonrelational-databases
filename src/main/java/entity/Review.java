package entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class Review {
    private ReviewId id;
    private LocalDate date;
    private BigDecimal rating;
    private String details;
    private String authorId;
}
