package repository.cache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.LeaderboardDetailsDto;
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

    public List<LeaderboardDetailsDto> getLeaderboard(int limit, int skip, Jedis jedisConn) {
        String leaderboardKey = buildLeaderboardKey("averageRating");

        if (!jedisConn.exists(leaderboardKey)) {
            return null;
        }

        // get leaderboard components
        List<Tuple> leaderboard = jedisConn.zrevrangeWithScores(leaderboardKey, skip, skip + limit - 1);

        Pipeline pipeline = jedisConn.pipelined();
        Map<String, Response<Map<String, String>>> responses = new LinkedHashMap<>();

        for (Tuple pair : leaderboard) {
            String freelancerId = pair.getElement();
            String entryKey = buildLeaderboardEntryKey("averageRating", freelancerId);
            responses.put(freelancerId, pipeline.hgetAll(entryKey));
        }

        pipeline.sync();

        // get leaderboard details
        List<LeaderboardDetailsDto> leaderboardDetails = new ArrayList<>(responses.size());
        for (var entry : responses.entrySet()) {
            Map<String, String> details = entry.getValue().get();
            LeaderboardDetailsDto dto = LeaderboardDetailsMapper.toLeaderboardDetails(entry.getKey(), details);
            leaderboardDetails.add(dto);
        }

        return leaderboardDetails;
    }

    private LeaderboardDetailsDto getEntryDetails(String freelancerId, Jedis jedisConn) {
        String entryKey = buildLeaderboardEntryKey("averageRating", freelancerId);
        Map<String, String> entryDetails = jedisConn.hgetAll(entryKey);
        return LeaderboardDetailsMapper.toLeaderboardDetails(freelancerId, entryDetails);
    }

    public void updateEntryDetails(String freelancerId, BigDecimal oldRating, BigDecimal newRating, int reviewNumChange, Jedis jedisConn) {
        String leaderboardKey = buildLeaderboardKey("averageRating");
        String entryDetailsKey = buildLeaderboardEntryKey("averageRating", freelancerId);
        Map<String, String> updatedFields;

        jedisConn.watch(leaderboardKey, entryDetailsKey); // TODO: resolve
        
        if (!jedisConn.exists(leaderboardKey)) {
            return;
        }

        LeaderboardDetailsDto detailsDto = getEntryDetails(freelancerId, jedisConn);

        Transaction transaction = jedisConn.multi();
        try {
            // re-calculate rating
            BigDecimal oldAvgRating = detailsDto.getRating();
            int oldReviewNum = detailsDto.getReviewNum();
            int newReviewNum = oldReviewNum + reviewNumChange;

            BigDecimal newAvgRating = calculateRating(oldAvgRating, oldRating, oldReviewNum, newRating, newReviewNum);

            // update leaderboard entry details
            updatedFields = Map.of(
                "rating", newAvgRating.toString(),
                "reviewNum", String.valueOf(newReviewNum)
            );
            transaction.hset(entryDetailsKey, updatedFields);

            // update leaderboard
            double compositeScore = calculateCompositeScore(newAvgRating, newReviewNum);
            transaction.zadd(leaderboardKey, compositeScore, freelancerId);

            transaction.exec();
        }
        catch (Exception ex) {
            transaction.discard();
            throw ex;
        }
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

    private String buildLeaderboardKey(String sortBy) {
        return String.format("leaderboard:%s", sortBy);
    }

    private String buildLeaderboardEntryKey(String sortBy, String freelancerId) {
        return String.format("leaderboard:%s:%s", sortBy, freelancerId);
    }

    private BigDecimal calculateRating(BigDecimal oldAvgRating, BigDecimal oldRating, int oldReviewNum, BigDecimal newRating, int newReviewNum) {
        if (oldAvgRating == null) {
            oldAvgRating = BigDecimal.ZERO;
        }

        double oldAvgRatingDouble = oldAvgRating.doubleValue();
        double oldRatingDouble = oldRating.doubleValue();
        double newRatingDouble = newRating.doubleValue();
        
        double newAvgRatingDouble = (oldAvgRatingDouble * oldReviewNum - oldRatingDouble + newRatingDouble) / newReviewNum;
        return BigDecimal.valueOf(newAvgRatingDouble);
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
