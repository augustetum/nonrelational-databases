package repository;

import org.springframework.stereotype.Repository;
import config.MongoDbContext;

@Repository
public class ClientReviewRepository extends ReviewRepository {
    public ClientReviewRepository(MongoDbContext dbContext) {
        super(dbContext.clients);
    }
}
