package repository;

import org.springframework.stereotype.Repository;
import config.MongoDbContext;

@Repository
public class FreelancerReviewRepository extends ReviewRepository {
    public FreelancerReviewRepository(MongoDbContext dbContext) {
        super(dbContext.freelancers);
    }
}
