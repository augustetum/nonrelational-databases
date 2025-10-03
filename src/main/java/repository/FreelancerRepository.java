package repository;

import config.MongoDbContext;
import entity.Freelancer;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class FreelancerRepository {
    private final MongoDbContext dbContext;

    public FreelancerRepository(MongoDbContext dbContext) {
        this.dbContext = dbContext;
    }

    public List<Freelancer> getAllFreelancers() {
        return dbContext.freelancers.find()
                .into(new ArrayList<Document>())
                .stream()
                .map(this::documentToFreelancer)
                .toList();
    }

    public Freelancer documentToFreelancer(Document document) {
        Freelancer freelancer = new Freelancer();

        freelancer.setId(document.getString("_id"));


        return freelancer;
    }
}
