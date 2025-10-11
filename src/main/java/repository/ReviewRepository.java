package repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.springframework.stereotype.Repository;
import entity.Review;
import entity.ReviewId;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;

@Repository
public abstract class ReviewRepository {
    private final MongoCollection<Document> collection;

    public ReviewRepository(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public Optional<Review> getByReviewId(String revieweeId, String reviewId) {
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(Filters.eq("_id", revieweeId)),
            Aggregates.unwind("$reviews"),
            Aggregates.match(Filters.eq("reviews._id", reviewId)),
            Aggregates.replaceRoot("$reviews")
        );

        List<Document> documents = collection.aggregate(pipeline)
            .into(new ArrayList<>());

        if (documents.isEmpty()) {
            return Optional.empty();
        }

        Review review = convertDocumentToReview(revieweeId, documents.get(0));
        return Optional.of(review);
    }

    public Optional<Review> getByAuthorId(String revieweeId, String authorId) {
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(Filters.eq("_id", revieweeId)),
            Aggregates.unwind("$reviews"),
            Aggregates.match(Filters.eq("reviews.authorId", authorId)),
            Aggregates.replaceRoot("$reviews")
        );

        List<Document> documents = collection.aggregate(pipeline)
            .into(new ArrayList<>());

        if (documents.isEmpty()) {
            return Optional.empty();
        }

        Review review = convertDocumentToReview(revieweeId, documents.get(0));
        return Optional.of(review);
    }

    public List<Review> getAll(String revieweeId) {
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(Filters.eq("_id", revieweeId)),
            Aggregates.project(Projections.fields(
                Projections.include("reviews"),
                Projections.excludeId()
            )),
            Aggregates.unwind("$reviews"),
            Aggregates.replaceRoot("$reviews")
        );

        List<Review> reviews = new ArrayList<>();
        collection.aggregate(pipeline)
            .forEach(doc -> reviews.add(convertDocumentToReview(revieweeId, doc)));

        return reviews;
    }

    public void add(Review review) {
        Document reviewDocument = convertReviewToDocument(review);
        
        Bson filter = Filters.eq("_id", review.getId().revieweeId());
        Bson updates = Updates.push("reviews", reviewDocument);

        collection.updateOne(filter, updates);
    }

    public void update(Review review) {
        Bson filter = Filters.and(
            Filters.eq("_id", review.getId().revieweeId()),
            Filters.eq("reviews._id", review.getId().reviewId())
        );

        Bson updates = Updates.combine(
            Updates.set("reviews.$.rating", review.getRating()),
            Updates.set("reviews.$.details", review.getDetails())
        );

        collection.updateOne(filter, updates);
    }

    public void remove(ReviewId id) {
        Bson reviewToRemove = new Document("_id", id.reviewId());
        Bson updates = Updates.pull("reviews", reviewToRemove);
        
        Bson userFilter = Filters.eq("_id", id.revieweeId());

        collection.updateOne(userFilter, updates);
    }

    protected Document convertReviewToDocument(Review review) {
        Document document = new Document();
        document.append("_id", review.getId().reviewId());
        document.append("rating", review.getRating());
        document.append("details", review.getDetails());
        document.append("authorId", review.getAuthorId());

        return document;
    }

    protected Review convertDocumentToReview(String revieweeId, Document document) {
        Review review = new Review();

        String reviewId = document.getString("_id");
        ReviewId id = new ReviewId(revieweeId, reviewId);
        review.setId(id);

        Decimal128 ratingDecimal = document.get("rating", Decimal128.class);
        BigDecimal rating = ratingDecimal.bigDecimalValue();
        review.setRating(rating);

        String details = document.getString("details");
        review.setDetails(details);

        String authorId = document.getString("authorId");
        review.setAuthorId(authorId);

        return review;
    }
}
