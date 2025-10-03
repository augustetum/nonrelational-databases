package entity;

import lombok.Data;

@Data
public class Review {
    public String id;
    public double rating;
    public String details;
    public String authorId;
}
