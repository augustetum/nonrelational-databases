package entity;

import lombok.Data;

@Data
public class Review {
    public ReviewId id;
    public double rating;
    public String details;
    public String authorId;
}
