package repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import entity.Review;
import entity.Workfield;
import entity.Freelancer;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.springframework.stereotype.Repository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import config.MongoDbContext;
import dto.FreelancerDetailsDto;
import util.IdentifierGenerator;

@Repository
public class FreelancerRepository {
    private final MongoCollection<Document> collection;

    public FreelancerRepository(MongoDbContext dbContext) {
        this.collection = dbContext.freelancers;
    }

    public Optional<Freelancer> findByEmail(String email) {
        Optional<Freelancer> maybeFreelancer = collection.find()
                .into(new ArrayList<Document>())
                .stream()
                .map(this::convertDocumentToFreelancer)
                .toList().stream()
                .filter(freelancer -> freelancer.getEmail().equals(email))
                .findFirst();
        return maybeFreelancer;
    }


    public Optional<FreelancerDetailsDto> getDetails(String freelancerId) {
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(Filters.eq("_id", freelancerId)),
            Aggregates.project(Projections.fields(
                Projections.computed("averageRating", 
                    new Document("$ifNull", Arrays.asList(
                        new Document("$avg", "$reviews.rating"), 
                        BigDecimal.ZERO
                    ))
                ),
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

    public void add(Freelancer freelancer) {
        freelancer.setId(IdentifierGenerator.generateId());
        Document document = convertFreelancerToDocument(freelancer);
        collection.insertOne(document);
    }

    private Document convertFreelancerToDocument(Freelancer freelancer) {
        Document document = new Document();
        List<Review> reviews = new ArrayList<>();
        List<Workfield> workfields = new ArrayList<>();
        document.append("_id", freelancer.getId());
        document.append("firstName", freelancer.getFirstName());
        document.append("lastName", freelancer.getLastName());
        document.append("password", freelancer.getPassword());
        document.append("email", freelancer.getEmail());
        document.append("phoneNumber", freelancer.getPhoneNumber());
        document.append("city", freelancer.getCity());
        document.append("workfields", workfields);
        document.append("reviews", reviews);
        return document;
    }

    private FreelancerDetailsDto convertDocumentToFreelancerDetails(Document document) {
        FreelancerDetailsDto freelancerDetails = new FreelancerDetailsDto();

        String id = document.getString("_id");
        freelancerDetails.setId(id);

        String firstName = document.getString("firstName");
        freelancerDetails.setFirstName(firstName);
        
        String lastName = document.getString("lastName");
        freelancerDetails.setLastName(lastName);

        Decimal128 ratingDecimal = document.get("averageRating", Decimal128.class);
        BigDecimal rating = ratingDecimal.bigDecimalValue();
        freelancerDetails.setRating(rating);
        
        long phoneNumber = document.getLong("phoneNumber");
        freelancerDetails.setPhoneNumber(phoneNumber);
        
        String city = document.getString("city");
        freelancerDetails.setCity(city);
        
        return freelancerDetails;
    }

    private Freelancer convertDocumentToFreelancer(Document document) {
        Long phoneNumber;
        Object phoneNumberObj = document.get("phoneNumber");
        if (phoneNumberObj instanceof String) {
            phoneNumber = Long.parseLong((String) phoneNumberObj);
        } else if (phoneNumberObj instanceof Number) {
            phoneNumber = ((Number) phoneNumberObj).longValue();
        } else {
            phoneNumber = 0L; // Default value
        }
        return Freelancer.builder()
                .id(document.getString("_id"))
                .firstName(document.getString("firstName"))
                .lastName(document.getString("lastName"))
                .email(document.getString("email"))
                .password(document.getString("password"))
                .rating(0)
                .phoneNumber(phoneNumber)
                .city(document.getString("city"))
                .build();
    }



}
