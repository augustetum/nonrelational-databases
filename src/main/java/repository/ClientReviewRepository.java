package repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import config.MongoDbContext;
import entity.Review;

@Repository
public class ClientReviewRepository extends ReviewRepository {
    public ClientReviewRepository(MongoDbContext dbContext) {
        super(dbContext);
    }

    public List<Review> get(String revieweeId) {
        return get(revieweeId, dbContext.clients);
    }

    public void add(String revieweeId, Review review) {
        add(revieweeId, review, dbContext.clients);
    }

    public void update(String revieweeId, Review review) {
        update(revieweeId, review, dbContext.clients);
    }

    public void remove(String revieweeId, String reviewId) {
        remove(revieweeId, reviewId, dbContext.clients);
    }
}
