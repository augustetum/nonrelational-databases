package repository;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;
import entity.Review;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

@Repository
public abstract class ReviewRepository {
    private final MongoCollection<Document> collection;

    public ReviewRepository(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public List<Review> get(String revieweeId) {
        Bson filter = Filters.eq("_id", new ObjectId(revieweeId));
        Bson projections = Projections.include("reviews");

        List<Review> reviews = collection.find(filter)
            .projection(projections)
            .into(new ArrayList<Document>())
            .stream()
            .map(this::convertDocumentToReview)
            .toList();

        return reviews;
    }

    public void add(String revieweeId, Review review) {
        Document reviewDocument = convertReviewToDocument(review);
        
        Bson filter = Filters.eq("_id", new ObjectId(revieweeId));
        Bson updates = Updates.push("reviews", reviewDocument);

        collection.updateOne(filter, updates);
    }

    public void update(String revieweeId, Review review) {
        Bson filter = Filters.and(
            Filters.eq("_id", new ObjectId(revieweeId)),
            Filters.eq("reviews._id", review.id)
        );

        Bson updates = Updates.combine(
            Updates.set("reviews.$.rating", review.rating),
            Updates.set("reviews.$.details", review.details)
        );

        UpdateResult result = collection.updateOne(filter, updates);
        System.out.println(result);
    }

    public void remove(String revieweeId, String reviewId) {
        Bson reviewFilter = Filters.eq("id", reviewId);
        Bson updates = Updates.pull("reviews", reviewFilter);

        Bson userFilter = Filters.eq("_id", new ObjectId(revieweeId));
        
        collection.updateOne(userFilter, updates);
    }

    protected Document convertReviewToDocument(Review review) {
        ObjectId reviewId = new ObjectId();

        Document document = new Document();
        document.append("id", reviewId.toString());
        document.append("rating", review.rating);
        document.append("details", review.details);
        document.append("authorId", review.authorId);

        return document;
    }

    protected Review convertDocumentToReview(Document document) {
        Review review = new Review();
        review.setId(document.getString("id"));
        review.setRating(document.getDouble("rating"));
        review.setDetails(document.getString("details"));
        review.setAuthorId(document.getString("authorId"));

        return review;
    }
}
