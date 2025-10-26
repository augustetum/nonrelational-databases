package repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;
import entity.Review;
import entity.ReviewId;
import util.DateConverter;
import util.ReviewMapper;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

@Repository
public abstract class ReviewRepository {
    private final MongoCollection<Document> collection;

    public ReviewRepository(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public Review getByReviewId(String revieweeId, String reviewId) {
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(Filters.eq("_id", revieweeId)),
            Aggregates.unwind("$reviews"),
            Aggregates.match(Filters.eq("reviews._id", reviewId)),
            Aggregates.replaceRoot("$reviews")
        );

        List<Document> documents = collection.aggregate(pipeline).into(new ArrayList<>());

        if (documents.isEmpty()) {
            return null;
        }

        return ReviewMapper.toReview(revieweeId, documents.get(0));
    }

    public Review getByAuthorId(String revieweeId, String authorId) {
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(Filters.eq("_id", revieweeId)),
            Aggregates.unwind("$reviews"),
            Aggregates.match(Filters.eq("reviews.authorId", authorId)),
            Aggregates.replaceRoot("$reviews")
        );

        List<Document> documents = collection.aggregate(pipeline).into(new ArrayList<>());

        if (documents.isEmpty()) {
            return null;
        }

        return ReviewMapper.toReview(revieweeId, documents.get(0));
    }

    public List<Review> getAll(String revieweeId) {
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(Filters.eq("_id", revieweeId)),
            Aggregates.project(Projections.fields(
                Projections.include("reviews"),
                Projections.excludeId()
            )),
            Aggregates.unwind("$reviews"),
            Aggregates.sort(Sorts.descending("reviews.date")),
            Aggregates.replaceRoot("$reviews")
        );

        List<Review> reviews = new ArrayList<>();
        collection.aggregate(pipeline)
            .forEach(doc -> reviews.add(ReviewMapper.toReview(revieweeId, doc)));

        return reviews;
    }

    public void add(Review review) {
        review.setDate(LocalDate.now());
        Document reviewDocument = ReviewMapper.toDocument(review);
        
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
            Updates.set("reviews.$.date", DateConverter.localDateToDate(LocalDate.now())),
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
}
