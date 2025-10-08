package repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import config.MongoDbContext;
import dto.FreelancerDetailsDto;

@Repository
public class FreelancerRepository {
    private final MongoCollection<Document> collection;

    public FreelancerRepository(MongoDbContext dbContext) {
        this.collection = dbContext.freelancers;
    }

    public Optional<FreelancerDetailsDto> getDetails(String freelancerId) {
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(Filters.eq("_id", freelancerId)),
            Aggregates.project(Projections.fields(
                Projections.computed("averageRating", new Document("$avg", "$reviews.rating")),
                Projections.include("firstName", "lastName", "city", "email", "phoneNumber")
            ))
        );

        List<Document> documents = collection.aggregate(pipeline)
            .into(new ArrayList<>());

        if (documents.isEmpty()) {
            return Optional.empty();
        }
        Document document = documents.get(0);
        
        FreelancerDetailsDto freelancerDetails = new FreelancerDetailsDto();
        freelancerDetails.setId(document.getString("_id"));
        freelancerDetails.setFirstName(document.getString("firstName"));
        freelancerDetails.setLastName(document.getString("lastName"));
        freelancerDetails.setRating(document.getDouble("averageRating"));
        freelancerDetails.setPhoneNumber(document.getLong("phoneNumber"));
        freelancerDetails.setCity(document.getString("city"));

        return Optional.of(freelancerDetails);
    }
}
