package repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import config.MongoDbContext;
import dto.LeaderboardDetailsDto;

@Repository
public class LeaderboardRepository {
    private final MongoCollection<Document> collection;

    public LeaderboardRepository(MongoDbContext dbContext) {
        this.collection = dbContext.freelancers;
    }

    public List<LeaderboardDetailsDto> getRatingLeaderboard() {
        List<Bson> pipeline = new ArrayList<>();

        // project fields with calculated metrics
        pipeline.add(Aggregates.project(Projections.fields(
            Projections.computed("averageRating",
                new Document("$ifNull", Arrays.asList(
                    new Document("$avg", "$reviews.rating"),
                    null
                ))
            ),
            Projections.computed("reviewNum",
                new Document("$ifNull", Arrays.asList(
                    new Document("$size", "$reviews"),
                    0
                ))
            ),
            Projections.include("firstName", "lastName")
        )));

        // sort entries
        pipeline.add(Aggregates.sort(Sorts.orderBy(
            Sorts.descending("averageRating"),
            Sorts.descending("reviewNum")
        )));

        return collection.aggregate(pipeline)
            .into(new ArrayList<>())
            .stream()
            .map(this::convertDocumentToRatingLeaderboardDto)
            .toList();
    }

    private LeaderboardDetailsDto convertDocumentToRatingLeaderboardDto(Document document) {
        LeaderboardDetailsDto dto = new LeaderboardDetailsDto();

        String id = document.getString("_id");
        dto.setId(id);

        String firstName = document.getString("firstName");
        dto.setFirstName(firstName);

        String lastName = document.getString("lastName");
        dto.setLastName(lastName);

        Decimal128 ratingDecimal = document.get("averageRating", Decimal128.class);
        BigDecimal rating = null;

        if (ratingDecimal != null) {
            rating = ratingDecimal.bigDecimalValue().setScale(2, RoundingMode.HALF_UP);
        }
        
        dto.setRating(rating);

        int reviewNum = document.getInteger("reviewNum");
        dto.setReviewNum(reviewNum);

        return dto;
    }
}
