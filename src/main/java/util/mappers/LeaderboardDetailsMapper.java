package util.mappers;

import java.math.RoundingMode;

import org.bson.Document;
import org.bson.types.Decimal128;

import dto.LeaderboardDetailsDto;

public class LeaderboardDetailsMapper {
    public static LeaderboardDetailsDto toLeaderboardDetails(Document document) {              
        LeaderboardDetailsDto dto = new LeaderboardDetailsDto();

        dto.setId(document.getString("_id"));
        dto.setFirstName(document.getString("firstName"));
        dto.setLastName(document.getString("lastName"));

        Decimal128 ratingDecimal = document.get("averageRating", Decimal128.class);
        dto.setRating(ratingDecimal != null 
            ? ratingDecimal.bigDecimalValue().setScale(2, RoundingMode.HALF_UP) 
            : null);

        dto.setReviewNum(document.getInteger("reviewNum", 0));

        return dto;
    }
}
