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
        FreelancerDetailsDto freelancerDetails = convertDocumentToFreelancerDetails(document);

        return Optional.of(freelancerDetails);
    }

    private FreelancerDetailsDto convertDocumentToFreelancerDetails(Document document) {
        FreelancerDetailsDto freelancerDetails = new FreelancerDetailsDto();

        String id = document.getString("_id");
        freelancerDetails.setId(id);

        String firstName = document.getString("firstName");
        freelancerDetails.setFirstName(firstName);
        
        String lastName = document.getString("lastName");
        freelancerDetails.setLastName(lastName);
        
        double rating = document.getDouble("averageRating");
        freelancerDetails.setRating(rating);
        
        long phoneNumber = document.getLong("phoneNumber");
        freelancerDetails.setPhoneNumber(phoneNumber);
        
        String city = document.getString("city");
        freelancerDetails.setCity(city);
        
        return freelancerDetails;
    }
}
