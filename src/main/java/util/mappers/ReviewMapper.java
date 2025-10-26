package util.mappers;

import org.bson.Document;
import org.bson.types.Decimal128;

import entity.Review;
import entity.ReviewId;
import util.DateConverter;

public class ReviewMapper {
    public static Document toDocument(Review review) {
        return new Document()
            .append("_id", review.getId().reviewId())
            .append("date", DateConverter.localDateToDate(review.getDate()))
            .append("rating", review.getRating())
            .append("details", review.getDetails())
            .append("authorId", review.getAuthorId());
    }

    public static Review toReview(String revieweeId, Document document) {
        Review review = new Review();

        review.setId(new ReviewId(revieweeId, document.getString("_id")));
        review.setDate(DateConverter.dateToLocalDate(document.getDate("date")));

        Decimal128 ratingDecimal = document.get("rating", Decimal128.class);
        review.setRating(ratingDecimal != null ? ratingDecimal.bigDecimalValue() : null);

        review.setDetails(document.getString("details"));
        review.setAuthorId(document.getString("authorId"));

        return review;
    } 
}
