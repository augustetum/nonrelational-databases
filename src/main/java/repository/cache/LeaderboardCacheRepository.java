package repository.cache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.LeaderboardDetailsDto;
import entity.Review;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.resps.Tuple;
import util.mappers.LeaderboardDetailsMapper;

@Repository
public class LeaderboardCacheRepository extends CacheRepository {
    
    public LeaderboardCacheRepository(JedisPool jedisPool, ObjectMapper objectMapper) {
        super(jedisPool, objectMapper);
    }

    public List<LeaderboardDetailsDto> getAverageRatingLeaderboard(int limit, int skip, Jedis jedisConn) {
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
        List<LeaderboardDetailsDto> leaderboardDetails = new ArrayList<>();

        for (Tuple pair : leaderboard) {            
            String freelancerId = pair.getElement();
            // retrieve entry details
            Response<Map<String, String>> entryResponse = leaderboardEntries.get(freelancerId);
            Map<String, String> entry = entryResponse.get();

            LeaderboardDetailsDto dto = LeaderboardDetailsMapper.toLeaderboardDetails(freelancerId, entry);
            leaderboardDetails.add(dto);
        }

        return leaderboardDetails;
    }

    public LeaderboardDetailsDto getEntryDetails(String freelancerId, Jedis jedisConn) {
        String entryKey = buildLeaderboardEntryKey("averageRating", freelancerId);
        Map<String, String> entryDetails = jedisConn.hgetAll(entryKey);
        return LeaderboardDetailsMapper.toLeaderboardDetails(freelancerId, entryDetails);
    }

    public LeaderboardDetailsDto getEntryDetails(String freelancerId, Transaction transaction) {
        String entryKey = buildLeaderboardEntryKey("averageRating", freelancerId);
        Map<String, String> entryDetails = transaction.hgetAll(entryKey).get();
        return LeaderboardDetailsMapper.toLeaderboardDetails(freelancerId, entryDetails);
    }

    public void setLeaderboard(List<LeaderboardDetailsDto> leaderboardDetails, Jedis jedisConn) {
        String leaderboardKey = buildLeaderboardKey("averageRating");
        Map<String, Double> leaderboard = new HashMap<>();

        jedisConn.watch(leaderboardKey);
        Transaction transaction = jedisConn.multi();
        try {
            for (LeaderboardDetailsDto detailsDto : leaderboardDetails) {
                // add to leaderboard data structure
                String freelancerId = detailsDto.getId();
                double compositeScore = calculateCompositeScore(detailsDto);
                leaderboard.put(freelancerId, compositeScore);

                // add leaderboard entry details
                String entryDetailsKey = buildLeaderboardEntryKey("averageRating", freelancerId);
                Map<String, String> entryDetails = LeaderboardDetailsMapper.toMap(detailsDto);
                transaction.hset(entryDetailsKey, entryDetails);
            }

            transaction.zadd(leaderboardKey, leaderboard);
            transaction.exec();
        }
        catch (Exception ex) {
            transaction.discard();
            throw ex;
        }
    }

    public boolean leaderboardExists(Jedis jedisConn) {
        String leaderboardKey = buildLeaderboardKey("averageRating");
        return jedisConn.exists(leaderboardKey);
    }

    public void updateEntryDetails(Review review, Jedis jedisConn) {
        String freelancerId = review.getId().revieweeId();
        String leaderboardKey = buildLeaderboardKey("averageRating");
        String entryDetailsKey = buildLeaderboardEntryKey("averageRating", freelancerId);
        Map<String, String> updatedFields;

        jedisConn.watch(leaderboardKey); // TODO: resolve
        Transaction transaction = jedisConn.multi();
        try {
            if (!jedisConn.exists(leaderboardKey)) {
                return;
            }

            LeaderboardDetailsDto detailsDto = getEntryDetails(leaderboardKey, transaction);

            // convert fields to double
            BigDecimal reviewRating = review.getRating();
            BigDecimal avgRating = detailsDto.getRating();

            if (avgRating == null) {
                avgRating = BigDecimal.ZERO;
            }

            double reviewRatingDouble = reviewRating.doubleValue();
            double avgRatingDouble = avgRating.doubleValue();
            int reviewNum = detailsDto.getReviewNum();
            
            // re-calculate rating
            int updatedReviewNum = reviewNum + 1;
            double updatedAvgRating = (avgRatingDouble * reviewNum + reviewRatingDouble) / updatedReviewNum;
            
            // update leaderboard entry details
            avgRating = BigDecimal.valueOf(updatedAvgRating);

            updatedFields = Map.of(
                "rating", avgRating.toString(),
                "reviewNum", String.valueOf(reviewNum)
            );

            transaction.hset(entryDetailsKey, updatedFields);

            // re-calculate new composite score
            double compositeScore = calculateCompositeScore(avgRating, updatedReviewNum);
            transaction.zadd(leaderboardKey, compositeScore, freelancerId);

            transaction.exec();
        }
        catch (Exception ex) {
            transaction.discard();
            throw ex;
        }
    }

    public void updateAverageRatingLeaderboard(String freelancerId, BigDecimal avgRating, int reviewNum, Jedis jedisConn) {
        String entryKey = buildLeaderboardEntryKey("averageRating", freelancerId);
        String leaderboardKey = buildLeaderboardKey("averageRating");
        
        Map<String, String> entryFields = new HashMap<>();
        entryFields.put("rating", String.valueOf(avgRating));
        entryFields.put("reviewNum", String.valueOf(reviewNum));

        double compositeScore = calculateCompositeScore(avgRating, reviewNum);

        // apply changes
        Transaction transaction = jedisConn.multi();
        try {
            transaction.hset(entryKey, entryFields);
            transaction.zadd(leaderboardKey, compositeScore, freelancerId);
            transaction.exec();
        }
        catch (Exception ex){
            transaction.discard();
            throw ex;
        }       
    }

    private String buildLeaderboardKey(String sortBy) {
        return String.format("leaderboard:%s", sortBy);
    }

    private String buildLeaderboardEntryKey(String sortBy, String freelancerId) {
        return String.format("leaderboard:%s:%s", sortBy, freelancerId);
    }

    private double calculateCompositeScore(LeaderboardDetailsDto dto) {
        return calculateCompositeScore(dto.getRating(), dto.getReviewNum());
    }

    private double calculateCompositeScore(BigDecimal ratingBigDecimal, int reviewNum) {
        double rating = -1;

        if (ratingBigDecimal != null) {
            rating = ratingBigDecimal.doubleValue();
        }

        return rating * 100 + reviewNum;
    }
}
