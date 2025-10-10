package repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;

import config.MongoDbContext;
import dto.ClientDetailsDto;
import entity.Client;
import util.IdentifierGenerator;

@Repository
public class ClientRepository {
    private final MongoCollection<Document> collection;

    public ClientRepository(MongoDbContext dbContext) {
        this.collection = dbContext.clients;
    }

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public Optional<ClientDetailsDto> getDetails(String clientId) {
        List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.eq("_id", clientId)),
                Aggregates.project(Projections.fields(
                        Projections.computed("averageRating", new Document("$ifNull", Arrays.asList(
                                new Document("$avg", "$reviews.rating"),
                                0.0))),
                        Projections.include("firstName", "lastName", "city", "email", "phoneNumber"))));

        List<Document> documents = collection.aggregate(pipeline)
                .into(new ArrayList<>());

        if (documents.isEmpty()) {
            return Optional.empty();
        }

        Document document = documents.get(0);
        ClientDetailsDto clientDetails = convertDocumentToClientDetails(document);

        return Optional.of(clientDetails);
    }

    public Optional<Client> findByEmail(String email) {
        Optional<Client> maybeClient = collection.find()
                .into(new ArrayList<Document>())
                .stream()
                .map(this::convertDocumentToClient)
                .toList().stream()
                .filter(client -> client.getEmail().equals(email))
                .findFirst();
        return maybeClient;
    }

    public boolean exists(String clientId) {
        Optional<ClientDetailsDto> client = getDetails(clientId);
        if (client.isPresent())
            return true;
        else
            return false;
    }

    public void add(Client client) {
        client.setId(IdentifierGenerator.generateId());
        Document document = convertClientToDocument(client);
        collection.insertOne(document);
    }

    public void update(String clientId, Client client) {
        Bson filter = Filters.eq("_id", clientId);
        Bson updates = Updates.combine(
                Updates.set("firstName", client.getFirstName()),
                Updates.set("lastName", client.getLastName()),
                Updates.set("city", client.getCity()),
                Updates.set("phoneNumber", client.getPhoneNumber()));
        collection.updateOne(filter, updates);
    }

    public void delete(String clientId) {
        Bson filter = Filters.eq("_id", clientId);
        collection.deleteOne(filter);
    }

    private Document convertClientToDocument(Client client) {
        Document document = new Document();
        document.append("_id", client.getId());
        document.append("firstName", client.getFirstName());
        document.append("lastName", client.getLastName());
        document.append("password", client.getPassword());
        document.append("email", client.getEmail());
        document.append("phoneNumber", client.getPhoneNumber());
        document.append("city", client.getCity());
        return document;
    }

        private Client convertDocumentToClient(Document document) {
        return Client.builder()
                .id(document.getString("_id"))
                .firstName(document.getString("firstName"))
                .lastName(document.getString("lastName"))
                .email(document.getString("email"))
                .password(document.getString("password"))
                .rating(0)
                .phoneNumber(document.getLong("phoneNumber"))
                .city(document.getString("city"))
                .build();
    }

    private ClientDetailsDto convertDocumentToClientDetails(Document document) {
        ClientDetailsDto clientDetails = new ClientDetailsDto();

        String id = document.getString("_id");
        clientDetails.setId(id);

        String firstName = document.getString("firstName");
        clientDetails.setFirstName(firstName);

        String lastName = document.getString("lastName");
        clientDetails.setLastName(lastName);

        String email = document.getString("email");
        clientDetails.setEmail(email);

        double rating = document.getDouble("averageRating");
        clientDetails.setRating(rating);

        long phoneNumber = document.getLong("phoneNumber");
        clientDetails.setPhoneNumber(phoneNumber);

        String city = document.getString("city");
        clientDetails.setCity(city);

        return clientDetails;
    }
}
