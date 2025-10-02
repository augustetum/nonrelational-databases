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

@Repository
public class ReviewRepository {
    private final MongoDbContext dbContext;

    public ReviewRepository(MongoDbContext dbContext) {
        this.dbContext = dbContext;
    }

    public List<Review> getByRevieweeId(String id) {
        Bson filter = Filters.eq("_id", new ObjectId(id));
        Bson projections = Projections.include("reviews");

        List<Review> reviews = dbContext.freelancers.find(filter)
            .projection(projections)
            .into(new ArrayList<Document>())
            .stream()
            .map(this::documentToReview)
            .toList();

        return reviews;
    }

    public Review documentToReview(Document document) {
        Review review = new Review();

        review.setStars(document.getDouble("stars"));
        review.setDetails(document.getString("reviews"));

        return review;
    }
}
