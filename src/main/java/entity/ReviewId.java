package entity;

import util.IdentifierGenerator;

public record ReviewId(String revieweeId, String reviewId) {
    public ReviewId(String revieweeId, String reviewId) {
        this.revieweeId = revieweeId;
        this.reviewId = reviewId;
    }

    public ReviewId(String revieweeId) {
        this(revieweeId, IdentifierGenerator.generateId());
    }
}