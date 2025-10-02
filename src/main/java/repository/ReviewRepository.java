package repository;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;
import config.MongoDbContext;
import entity.Review;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

@Repository
public class ReviewRepository {
    private final MongoDbContext dbContext;

    public ReviewRepository(MongoDbContext dbContext) {
        this.dbContext = dbContext;
    }

    public List<Review> getByRevieweeId(String revieweeId) {
        Bson filter = Filters.eq("_id", new ObjectId(revieweeId));
        Bson projections = Projections.include("reviews");

        List<Review> reviews = dbContext.freelancers.find(filter)
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
        
        UpdateResult result = dbContext.freelancers.updateOne(filter, updates);
        System.out.println("result: " + result);
    }

    private Document convertReviewToDocument(Review review) {
        Document document = new Document();

        document.append("_id", new ObjectId());
        document.append("rating", review.rating);
        document.append("details", review.details);
        document.append("authorId", review.authorId);

        return document;
    }

    private Review convertDocumentToReview(Document document) {
        Review review = new Review();

        review.setId(document.getString("_id"));
        review.setRating(document.getDouble("rating"));
        review.setDetails(document.getString("details"));
        review.setAuthorId(document.getString("authorId"));

        return review;
    }
}
