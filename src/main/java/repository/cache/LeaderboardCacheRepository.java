package repository.cache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.FreelancerRatingLeaderboardDto;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.resps.Tuple;

@Repository
public class LeaderboardCacheRepository extends CacheRepository {
    
    public LeaderboardCacheRepository(JedisPool jedisPool, ObjectMapper objectMapper) {
        super(jedisPool, objectMapper);
    }

    public List<FreelancerRatingLeaderboardDto> getAverageRatingLeaderboard(int limit, int skip, Jedis jedisConn) {
        // check if leaderboard exists in cache
        String leaderboardKey = buildLeaderboardKey("averageRating");
        if (!jedisConn.exists(leaderboardKey)) {
            return null;
        }
        
        // retrieve leaderboard
        List<Tuple> leaderboard = jedisConn.zrevrangeWithScores(leaderboardKey, skip, skip + limit - 1);
        
        // retrieve leaderboard details
        Pipeline pipeline = jedisConn.pipelined();

        Map<String, Response<Map<String, String>>> leaderboardEntries = new HashMap<>();

        for (Tuple pair : leaderboard) {
            String freelancerId = pair.getElement();
            String entryKey = buildLeaderboardEntryKey("averageRating", freelancerId);

            leaderboardEntries.put(freelancerId, pipeline.hgetAll(entryKey));
        }

        pipeline.sync();

        // combine data
        List<FreelancerRatingLeaderboardDto> leaderboardDetails = new ArrayList<>();

        for (Tuple pair : leaderboard) {            
            String freelancerId = pair.getElement();
            // retrieve entry details
            Response<Map<String, String>> entryResponse = leaderboardEntries.get(freelancerId);
            Map<String, String> entry = entryResponse.get();

            FreelancerRatingLeaderboardDto dto = buildLeaderboardDto(freelancerId, entry);
            leaderboardDetails.add(dto);
        }

        return leaderboardDetails;
    }

    public FreelancerRatingLeaderboardDto getLeaderboardEntry(String freelancerId, Jedis jedisConn) {
        String entryKey = buildLeaderboardEntryKey("averageRating", freelancerId);
        Map<String, String> entry = jedisConn.hgetAll(entryKey);

        return buildLeaderboardDto(freelancerId, entry);
    }

    public void setAverageRatingLeaderboard(List<FreelancerRatingLeaderboardDto> leaderboardDetails, Jedis jedisConn) {
        Map<String, Double> scores = new HashMap<>();
        
        for (FreelancerRatingLeaderboardDto dto : leaderboardDetails) {    
            // calculate composite score
            String freelancerId = dto.getId();
            double compositeScore = calculateCompositeScore(dto);

            scores.put(freelancerId, compositeScore);
            
            // create leaderboard entry details
            String entryKey = buildLeaderboardEntryKey("averageRating", freelancerId);
            Map<String, String> hash =  buildLeaderboardEntryHashMap(dto);
            
            jedisConn.hset(entryKey, hash);
        }

        // create leaderboard
        String leaderboardKey = buildLeaderboardKey("averageRating");
        jedisConn.zadd(leaderboardKey, scores);
    }

    public boolean leaderboardExists(Jedis jedisConn) {
        String leaderboardKey = buildLeaderboardKey("averageRating");
        return jedisConn.exists(leaderboardKey);
    }

    public void updateAverageRatingLeaderboard(FreelancerRatingLeaderboardDto dto, Jedis jedisConn) {
        // TODO: add transaction?

        // update leaderboard entry
        String entryKey = buildLeaderboardEntryKey("averageRating", dto.getId());
        Map<String, String> hash = buildLeaderboardEntryHashMap(dto);

        jedisConn.hset(entryKey, hash);

        // update leaderboard
        String leaderboardKey = buildLeaderboardKey("averageRating");
        String freelancerId = dto.getId();
        double compositeScore = calculateCompositeScore(dto);
        
        jedisConn.zadd(leaderboardKey, compositeScore, freelancerId);
    }

    private String buildLeaderboardKey(String sortBy) {
        return String.format("leaderboard:%s", sortBy);
    }

    private String buildLeaderboardEntryKey(String sortBy, String freelancerId) {
        return String.format("leaderboard:%s:%s", sortBy, freelancerId);
    }

    private Map<String, String> buildLeaderboardEntryHashMap(FreelancerRatingLeaderboardDto dto) {
        Map<String, String> hash = new HashMap<>();
        
        // retrieve rating
        BigDecimal ratingBigDecimal = dto.getRating();
        double rating = -1;

        if (ratingBigDecimal != null) {
            rating = ratingBigDecimal.doubleValue();
        }
        
        // create hash-map
        hash.put("firstName", dto.getFirstName());
        hash.put("lastName", dto.getLastName());
        hash.put("rating", String.valueOf(rating));
        hash.put("reviewNum", String.valueOf(dto.getReviewNum()));

        return hash;
    }

    private FreelancerRatingLeaderboardDto buildLeaderboardDto(String freelancerId, Map<String, String> entry) {
        FreelancerRatingLeaderboardDto dto = new FreelancerRatingLeaderboardDto();
        
        // set freelancer id
        dto.setId(freelancerId);

        // set freelancer first name
        String firstName = entry.get("firstName");
        dto.setFirstName(firstName);
        
        // set freelancer last name
        String lastName = entry.get("lastName");
        dto.setLastName(lastName);

        // set freelancer average rating
        String ratingStr = entry.get("rating");
        Double ratingDouble = Double.parseDouble(ratingStr);
        BigDecimal ratingBigDecimal = null;

        if (ratingDouble != -1) {
            ratingBigDecimal = BigDecimal.valueOf(ratingDouble);
        }

        dto.setRating(ratingBigDecimal);
        
        // set freelancer review number
        String reviewNumStr = entry.get("reviewNum");
        int reviewNum = Integer.valueOf(reviewNumStr);
        dto.setReviewNum(reviewNum);

        return dto;
    }

    private double calculateCompositeScore(FreelancerRatingLeaderboardDto dto) {
        BigDecimal ratingBigDecimal = dto.getRating();
        double rating = -1;

        if (ratingBigDecimal != null) {
            rating = ratingBigDecimal.doubleValue();
        }

        int reviewNum = dto.getReviewNum();
        double compositeScore = rating * 100 + reviewNum;

        return compositeScore;
    }
}
