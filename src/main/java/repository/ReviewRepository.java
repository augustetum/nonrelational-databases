package repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;
import entity.Review;
import utils.IdentifierGenerator;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;

@Repository
public abstract class ReviewRepository {
    private final MongoCollection<Document> collection;

    public ReviewRepository(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public List<Review> get(String revieweeId) {
        Bson filter = Filters.eq("_id", revieweeId);
        Bson projection = Projections.include("reviews");

        Document document = collection.find(filter)
            .projection(projection)
            .first();

        List<Document> reviewDocuments = (List<Document>) document.get("reviews");

        return reviewDocuments.stream()
            .map(this::convertDocumentToReview)
            .toList();
    }

    public void add(String revieweeId, Review review) {
        String reviewId = IdentifierGenerator.generateId();
        review.setId(reviewId);

        Document reviewDocument = convertReviewToDocument(review);
        
        Bson filter = Filters.eq("_id", revieweeId);
        Bson updates = Updates.push("reviews", reviewDocument);

        collection.updateOne(filter, updates);
    }

    public void update(String revieweeId, Review review) {
        Bson filter = Filters.and(
            Filters.eq("_id", revieweeId),
            Filters.eq("reviews._id", review.id)
        );

        Bson updates = Updates.combine(
            Updates.set("reviews.$.rating", review.rating),
            Updates.set("reviews.$.details", review.details)
        );

        collection.updateOne(filter, updates);
    }

    public void remove(String revieweeId, String reviewId) {
        Bson reviewFilter = Filters.eq("_id", reviewId);
        Bson updates = Updates.pull("reviews", reviewFilter);

        Bson userFilter = Filters.eq("_id", revieweeId);
        
        collection.updateOne(userFilter, updates);
    }

    protected Document convertReviewToDocument(Review review) {
        Document document = new Document();
        document.append("_id", review.id);
        document.append("rating", review.rating);
        document.append("details", review.details);
        document.append("authorId", review.authorId);

        return document;
    }

    protected Review convertDocumentToReview(Document document) {
        Review review = new Review();

        String id = document.getString("_id");
        review.setId(id);

        Double rating = document.getDouble("rating");
        review.setRating(rating);

        String details = document.getString("details");
        review.setDetails(details);

        String authorId = document.getString("authorId");
        review.setAuthorId(authorId);

        return review;
    }
}
