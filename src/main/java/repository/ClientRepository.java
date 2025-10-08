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
import dto.ClientDetailsDto;

@Repository
public class ClientRepository {
        private final MongoCollection<Document> collection;

    public ClientRepository(MongoDbContext dbContext) {
        this.collection = dbContext.clients;
    }

    public Optional<ClientDetailsDto> getDetails(String freelancerId) {
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
        ClientDetailsDto clientDetails = convertDocumentToClientDetails(document);

        return Optional.of(clientDetails);
    }

    private ClientDetailsDto convertDocumentToClientDetails(Document document) {
        ClientDetailsDto clientDetails = new ClientDetailsDto();

        String id = document.getString("_id");
        clientDetails.setId(id);

        String firstName = document.getString("firstName");
        clientDetails.setFirstName(firstName);
        
        String lastName = document.getString("lastName");
        clientDetails.setLastName(lastName);
        
        double rating = document.getDouble("averageRating");
        clientDetails.setRating(rating);
        
        long phoneNumber = document.getLong("phoneNumber");
        clientDetails.setPhoneNumber(phoneNumber);
        
        String city = document.getString("city");
        clientDetails.setCity(city);
        
        return clientDetails;
    }
}
