package util.mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

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

    public static LeaderboardDetailsDto toLeaderboardDetails(String freelancerId, Map<String, String> entryDetails) {
        LeaderboardDetailsDto dto = new LeaderboardDetailsDto();

        dto.setId(freelancerId);
        dto.setFirstName(entryDetails.get("firstName"));
        dto.setLastName(entryDetails.get("lastName"));

        String ratingStr = entryDetails.get("rating");
        BigDecimal rating = null;
        if (ratingStr != null) {
            double ratingDouble = Double.parseDouble(ratingStr);
            if (ratingDouble != -1) {
                rating = BigDecimal.valueOf(ratingDouble);
            }
        }
        dto.setRating(rating);

        String reviewNumStr = entryDetails.get("reviewNum");
        int reviewNum = reviewNumStr != null ? Integer.parseInt(reviewNumStr) : 0;
        dto.setReviewNum(reviewNum);

        return dto;
    }

    public static Map<String, String> toMap(LeaderboardDetailsDto detailsDto) {
        Map<String, String> entryDetails = new HashMap<>();

        entryDetails.put("firstName", detailsDto.getFirstName());
        entryDetails.put("lastName", detailsDto.getLastName());
        entryDetails.put("rating", detailsDto.getRating() != null 
            ? detailsDto.getRating().setScale(2, RoundingMode.HALF_UP).toString() 
            : "-1");
        entryDetails.put("reviewNum", String.valueOf(detailsDto.getReviewNum()));

        return entryDetails;
    }

}
